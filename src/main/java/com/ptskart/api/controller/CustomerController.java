package com.ptskart.api.controller;

import com.ptskart.api.dto.ApiResponse;
import com.ptskart.api.entity.Role;
import com.ptskart.api.entity.User;
import com.ptskart.api.repository.RoleRepository;
import com.ptskart.api.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomerController(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getAllCustomers() {
        List<User> customers = userRepository.findAll();
        // Mask passwords in response
        customers.forEach(u -> u.setPassword("••••••••"));
        return ResponseEntity.ok(ApiResponse.ok("Customers list retrieved", customers));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getCustomerById(@PathVariable Long id) {
        return userRepository.findById(id).map(u -> {
            u.setPassword("••••••••");
            return ResponseEntity.ok(ApiResponse.ok("Customer profile found", u));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> updateCustomer(@PathVariable Long id, @RequestBody User details) {
        return userRepository.findById(id).map(user -> {
            user.setName(details.getName());
            user.setPhone(details.getPhone());
            if (details.getLoyaltyPoints() != null) {
                user.setLoyaltyPoints(details.getLoyaltyPoints());
            }
            if (details.getRole() != null && !details.getRole().isBlank()) {
                String targetRole = details.getRole().startsWith("ROLE_") ? details.getRole() : "ROLE_" + details.getRole();
                roleRepository.findByName(targetRole).ifPresent(user::setRole);
            }
            User saved = userRepository.save(user);
            saved.setPassword("••••••••");
            return ResponseEntity.ok(ApiResponse.ok("Customer updated successfully", saved));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/points")
    public ResponseEntity<ApiResponse<User>> updatePoints(@PathVariable Long id, @RequestParam Integer delta) {
        return userRepository.findById(id).map(user -> {
            int current = user.getLoyaltyPoints() != null ? user.getLoyaltyPoints() : 0;
            user.setLoyaltyPoints(Math.max(0, current + delta));
            User saved = userRepository.save(user);
            saved.setPassword("••••••••");
            return ResponseEntity.ok(ApiResponse.ok("Loyalty points updated", saved));
        }).orElse(ResponseEntity.notFound().build());
    }
}
