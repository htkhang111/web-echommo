package com.echommo.controller;

import com.echommo.dto.AuthRequest;
import com.echommo.dto.AuthResponse;
import com.echommo.dto.CharacterRequest;
import com.echommo.entity.User;
import com.echommo.entity.Wallet;
import com.echommo.enums.Role;
import com.echommo.repository.UserRepository;
import com.echommo.security.JwtUtils;
import com.echommo.service.CharacterService;
import com.echommo.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder encoder;
    @Autowired private JwtUtils jwtUtils;
    @Autowired private CharacterService characterService;
    @Autowired private EmailService emailService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody AuthRequest loginRequest) {
        User user = userRepository.findByUsername(loginRequest.getUsername()).orElse(null);

        if (user != null && Boolean.FALSE.equals(user.getIsActive())) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "BANNED");
            response.put("message", "Tài khoản đã bị khóa!");
            response.put("reason", user.getBanReason() != null ? user.getBanReason() : "Vi phạm quy định");

            if (user.getBannedAt() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
                response.put("bannedAt", user.getBannedAt().format(formatter));
            } else {
                response.put("bannedAt", "Không xác định");
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateToken((UserDetails) authentication.getPrincipal());
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String roleStr = user != null ? user.getRole().name() : "USER";
        return ResponseEntity.ok(new AuthResponse(jwt, userDetails.getUsername(), roleStr));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody AuthRequest signUpRequest) {
        // [FIX] Trả về JSON Object thay vì String thô để Frontend dễ bắt lỗi
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return responseError("Tên đăng nhập đã tồn tại!");
        }
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return responseError("Email này đã được sử dụng!");
        }

        // 1. Tạo User
        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setPasswordHash(encoder.encode(signUpRequest.getPassword()));
        user.setPassword(signUpRequest.getPassword());
        user.setFullName(signUpRequest.getFullName());
        user.setAvatarUrl("🐲");
        user.setIsActive(true);
        user.setRole(Role.USER);

        // 2. Tạo Wallet
        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setGold(new BigDecimal("1000.00"));
        user.setWallet(wallet);

        userRepository.save(user);

        // 3. Auto Login & Tạo Character
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(signUpRequest.getUsername(), signUpRequest.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            CharacterRequest charReq = new CharacterRequest();
            charReq.setName(signUpRequest.getUsername());
            characterService.createCharacter(charReq);

            String jwt = jwtUtils.generateToken((UserDetails) authentication.getPrincipal());
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            return ResponseEntity.ok(new AuthResponse(jwt, userDetails.getUsername(), "USER"));

        } catch (Exception e) {
            return responseError("Đăng ký thành công nhưng lỗi khởi tạo: " + e.getMessage());
        }
    }

    // [HELPER] Hàm trả về lỗi dạng JSON
    private ResponseEntity<?> responseError(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return ResponseEntity.badRequest().body(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) return responseError("Email không tồn tại trong hệ thống.");

        String otp = String.format("%06d", new Random().nextInt(999999));
        user.setOtpCode(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);

        try {
            emailService.sendOtpEmail(email, otp);
            return ResponseEntity.ok("Đã gửi mã OTP qua email."); // Trả String ở đây OK vì logic đơn giản, nhưng trả JSON sẽ tốt hơn
        } catch (Exception e) {
            return responseError("Lỗi gửi mail: " + e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String otp = payload.get("otp");
        String newPassword = payload.get("newPassword");

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return responseError("User không tồn tại");

        if (user.getOtpCode() == null || !user.getOtpCode().equals(otp)) {
            return responseError("Mã OTP không đúng.");
        }

        if (user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            return responseError("Mã OTP đã hết hạn.");
        }

        user.setPasswordHash(encoder.encode(newPassword));
        user.setOtpCode(null);
        user.setOtpExpiry(null);
        userRepository.save(user);

        return ResponseEntity.ok("Đổi mật khẩu thành công! Hãy đăng nhập lại.");
    }
}