package com.ptskart.api.service;

import com.ptskart.api.dto.AuthRequest;
import com.ptskart.api.dto.AuthResponse;
import com.ptskart.api.dto.RegisterRequest;
import com.ptskart.api.entity.Role;
import com.ptskart.api.entity.User;
import com.ptskart.api.repository.RoleRepository;
import com.ptskart.api.repository.UserRepository;
import com.ptskart.api.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final DynamicEmailService emailService;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider,
                       DynamicEmailService emailService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.emailService = emailService;
    }

    @Transactional(readOnly = true)
    public AuthResponse signin(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found: " + request.getEmail()));

        String token = tokenProvider.generateToken(user.getId(), user.getEmail(), user.getName(), user.getRole());

        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole());
    }

    @Transactional
    public AuthResponse signup(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already registered: " + request.getEmail());
        }

        String requestedRole = request.getRole() != null && !request.getRole().isBlank()
                ? request.getRole()
                : "ROLE_CUSTOMER";

        if (!requestedRole.startsWith("ROLE_")) {
            requestedRole = "ROLE_" + requestedRole;
        }

        final String finalRoleName = requestedRole;
        Role assignedRole = roleRepository.findByName(finalRoleName)
                .orElseGet(() -> roleRepository.save(new Role(finalRoleName, "Auto-created role tier")));

        User user = new User(
                request.getName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                assignedRole
        );
        user.setPhone(request.getPhone());
        user.setLoyaltyPoints(100); // Welcome bonus points

        user = userRepository.save(user);

        // Send Welcome Registration Email
        try {
            emailService.sendRegistrationEmail(user.getEmail(), user.getName());
        } catch (Exception ex) {
            System.err.println("[AuthService] Welcome email notice: " + ex.getMessage());
        }

        String token = tokenProvider.generateToken(user.getId(), user.getEmail(), user.getName(), user.getRole());

        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole());
    }

    @Transactional(readOnly = true)
    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }
}
