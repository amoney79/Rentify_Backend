package com.amoney.rentify_backend.controller;

import com.amoney.rentify_backend.model.Role;
import com.amoney.rentify_backend.model.User;
import com.amoney.rentify_backend.repository.UserRepository;
import com.amoney.rentify_backend.security.JwtTokenProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");
        String fullName = request.get("fullName");
        String phone = request.get("phone");
        String roleStr = request.getOrDefault("role", "ROLE_TENANT");

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email and password are required"));
        }

        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already registered"));
        }

        Role role;
        try {
            role = Role.valueOf(roleStr.toUpperCase().startsWith("ROLE_") ? roleStr.toUpperCase() : "ROLE_" + roleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            role = Role.ROLE_TENANT;
        }

        User user = User.builder()
                .email(email.toLowerCase().trim())
                .password(passwordEncoder.encode(password))
                .fullName(fullName != null ? fullName : email.split("@")[0])
                .phone(phone)
                .role(role)
                .build();

        User saved = userRepository.save(user);
        String token = tokenProvider.generateTokenFromEmail(saved.getEmail(), saved.getId(), saved.getRole().name());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", toUserDto(saved));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");

        if (email == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email and password are required"));
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email.toLowerCase().trim(), password)
            );

            User user = userRepository.findByEmail(email.toLowerCase().trim())
                    .orElseThrow(() -> new IllegalStateException("User not found"));

            String token = tokenProvider.generateToken(authentication, user.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", toUserDto(user));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid email or password"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthenticated"));
        }
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        return ResponseEntity.ok(toUserDto(user));
    }

    private Map<String, Object> toUserDto(User user) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", user.getId());
        dto.put("email", user.getEmail());
        dto.put("fullName", user.getFullName());
        dto.put("phone", user.getPhone());
        dto.put("role", user.getRole().name());
        dto.put("identityStatus", user.getIdentityStatus().name());
        dto.put("agentStatus", user.getAgentStatus().name());
        dto.put("avatarUrl", user.getAvatarUrl());
        dto.put("county", user.getCounty());
        dto.put("agencyName", user.getAgencyName());
        dto.put("agencyLicenseNumber", user.getAgencyLicenseNumber());
        return dto;
    }
}
