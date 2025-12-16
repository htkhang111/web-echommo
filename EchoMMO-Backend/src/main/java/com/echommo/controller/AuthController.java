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
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest().body("Lỗi: Username đã tồn tại!");
        }
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body("Lỗi: Email đã được sử dụng!");
        }

        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setPasswordHash(encoder.encode(signUpRequest.getPassword()));
        user.setPassword(signUpRequest.getPassword());
        user.setFullName(signUpRequest.getFullName());
        user.setAvatarUrl("🐲");
        user.setIsActive(true);
        user.setRole(Role.USER);

        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setGold(new BigDecimal("100.00"));
        user.setWallet(wallet);

        userRepository.save(user);

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(signUpRequest.getUsername(), signUpRequest.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            CharacterRequest charReq = new CharacterRequest();
            charReq.setName(signUpRequest.getUsername());
            characterService.createCharacter(charReq);
        } catch (Exception e) {
            return ResponseEntity.ok("Đăng ký thành công tài khoản, nhưng lỗi tạo nhân vật: " + e.getMessage());
        }

        return ResponseEntity.ok("Đăng ký thành công! Đã tạo nhân vật và tặng quà tân thủ.");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return ResponseEntity.badRequest().body("Email không tồn tại trong hệ thống.");
        }

        String otp = String.format("%06d", new Random().nextInt(999999));
        user.setOtpCode(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);

        try {
            emailService.sendOtpEmail(email, otp);
            return ResponseEntity.ok("Đã gửi mã OTP qua email.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi gửi mail: " + e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String otp = payload.get("otp");
        String newPassword = payload.get("newPassword");

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return ResponseEntity.badRequest().body("User không tồn tại");

        if (user.getOtpCode() == null || !user.getOtpCode().equals(otp)) {
            return ResponseEntity.badRequest().body("Mã OTP không đúng.");
        }

        if (user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Mã OTP đã hết hạn.");
        }

        user.setPasswordHash(encoder.encode(newPassword));
        user.setOtpCode(null);
        user.setOtpExpiry(null);
        userRepository.save(user);

        return ResponseEntity.ok("Đổi mật khẩu thành công! Hãy đăng nhập lại.");
    }
}
