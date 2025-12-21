package com.echommo.service;

import com.echommo.dto.AuthRequest;
import com.echommo.dto.ChangePasswordRequest;
import com.echommo.dto.UpdateProfileRequest;
import com.echommo.entity.User;
import com.echommo.entity.Wallet;
import com.echommo.enums.Role;
import com.echommo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class UserService {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    // Tên thư mục lưu ảnh (nằm ngay tại root của project)
    private final String UPLOAD_DIR = "uploads";

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    // Hàm tiện ích lấy User đang đăng nhập hiện tại
    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return findByUsername(username);
    }

    @Transactional
    public User registerUser(AuthRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại!");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email này đã được sử dụng!");
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setPassword(req.getPassword()); // Dev only
        user.setFullName(req.getFullName());
        // AvatarUrl mặc định là skin game, ProfileImageUrl là ảnh upload
        user.setAvatarUrl("🐲");
        user.setIsActive(true);
        user.setRole(Role.USER);

        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setGold(1000L);
        user.setWallet(wallet);

        return userRepository.save(user);
    }

    @Transactional
    public User updateProfile(UpdateProfileRequest request) {
        User user = getCurrentUser();

        // 1. Cập nhật FullName
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }

        // 2. Cập nhật Username (Check trùng)
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new RuntimeException("Tên đăng nhập đã tồn tại!");
            }
            user.setUsername(request.getUsername());
        }

        // 3. Cập nhật Email (Check trùng)
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email đã được sử dụng!");
            }
            user.setEmail(request.getEmail());
        }

        // 4. Cập nhật Password
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            user.setPassword(request.getPassword());
        }

        // 5. Cập nhật Skin Game (Emoji/Icon)
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }

        // 6. Cập nhật Ảnh Upload (Nếu client gửi link string)
        if (request.getProfileImageUrl() != null) {
            user.setProfileImageUrl(request.getProfileImageUrl());
        }

        return userRepository.save(user);
    }

    /**
     * MỚI THÊM: Xử lý upload ảnh đại diện thực tế (File)
     * Hàm này sẽ tự động tạo thư mục uploads và lưu file vào đó.
     */
    @Transactional
    public User uploadAvatar(MultipartFile file) {
        User user = getCurrentUser(); // Lấy user đang đăng nhập để sửa

        if (file.isEmpty()) {
            throw new RuntimeException("File không được để trống");
        }

        try {
            // 1. Định nghĩa đường dẫn folder uploads
            Path uploadPath = Paths.get(UPLOAD_DIR);

            // 2. Kiểm tra và tạo thư mục nếu chưa tồn tại
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 3. Tạo tên file mới (UUID) để tránh trùng lặp
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFileName = UUID.randomUUID().toString() + fileExtension;

            // 4. Lưu file vào ổ cứng
            Path filePath = uploadPath.resolve(newFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 5. Cập nhật đường dẫn vào Database (Lưu vào ProfileImageUrl)
            // Đường dẫn lưu db: /uploads/ten-file-uuid.png
            String dbFilePath = "/uploads/" + newFileName;
            user.setProfileImageUrl(dbFilePath);

            // Lưu và trả về user mới
            return userRepository.save(user);

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi hệ thống khi lưu file: " + e.getMessage());
        }
    }

    public String changePassword(ChangePasswordRequest request) {
        User user = getCurrentUser();
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Mật khẩu hiện tại không đúng!");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setPassword(request.getNewPassword());
        userRepository.save(user);
        return "Đổi mật khẩu thành công!";
    }
}