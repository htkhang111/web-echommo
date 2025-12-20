package com.echommo.controller;

import com.echommo.dto.AuthRequest;
import com.echommo.dto.AuthResponse;
import com.echommo.dto.CharacterRequest;
import com.echommo.entity.User;
import com.echommo.entity.Wallet;
import com.echommo.repository.UserRepository;
import com.echommo.repository.WalletRepository;
import com.echommo.security.JwtUtils;
import com.echommo.service.CharacterService;
import com.echommo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final JwtUtils jwtUtils;
    private final CharacterService characterService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody AuthRequest loginRequest) {
        User user = userRepository.findByUsername(loginRequest.getUsername()).orElse(null);

        if (user != null && Boolean.FALSE.equals(user.getIsActive())) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "BANNED");
            response.put("message", "Tài khoản đã bị khóa!");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateToken((UserDetails) authentication.getPrincipal());
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        Wallet wallet = walletRepository.findByUser(user).orElse(new Wallet());

        String roleStr = user != null ? user.getRole().name() : "USER";

        // [FIX] AuthResponse giờ đã nhận Integer id
        return ResponseEntity.ok(new AuthResponse(
                jwt,
                user.getUserId(),
                userDetails.getUsername(),
                user.getEmail(),
                roleStr,
                wallet.getEchoCoin(),
                wallet.getGold(),
                user.getAvatarUrl()
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody AuthRequest signUpRequest) {
        try {
            userService.registerUser(signUpRequest);

            // Auto login
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(signUpRequest.getUsername(), signUpRequest.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Auto create character
            try {
                CharacterRequest charReq = new CharacterRequest();
                charReq.setName(signUpRequest.getUsername());
                characterService.createCharacter(charReq);
            } catch (Exception ignored) {}

            String jwt = jwtUtils.generateToken((UserDetails) authentication.getPrincipal());
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userRepository.findByUsername(signUpRequest.getUsername()).orElseThrow();
            Wallet wallet = user.getWallet();

            return ResponseEntity.ok(new AuthResponse(
                    jwt,
                    user.getUserId(),
                    userDetails.getUsername(),
                    user.getEmail(),
                    "USER",
                    wallet.getEchoCoin(),
                    wallet.getGold(),
                    user.getAvatarUrl()
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}