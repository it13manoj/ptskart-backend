package com.ptskart.api.service;

import com.ptskart.api.entity.Role;
import com.ptskart.api.entity.User;
import com.ptskart.api.repository.RoleRepository;
import com.ptskart.api.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public RoleService(RoleRepository roleRepository, UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Optional<Role> getRoleById(Long id) {
        return roleRepository.findById(id);
    }

    public Optional<Role> getRoleByName(String name) {
        return roleRepository.findByName(name);
    }

    @Transactional
    public Role createRole(Role role) {
        String name = role.getName();
        if (name != null && !name.startsWith("ROLE_")) {
            role.setName("ROLE_" + name.toUpperCase());
        } else if (name != null) {
            role.setName(name.toUpperCase());
        }

        if (roleRepository.existsByName(role.getName())) {
            throw new IllegalArgumentException("Role with name " + role.getName() + " already exists");
        }

        return roleRepository.save(role);
    }

    @Transactional
    public Role updateRole(Long id, Role details) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + id));

        if (details.getDescription() != null) {
            role.setDescription(details.getDescription());
        }
        if (details.getName() != null && !details.getName().isBlank()) {
            String name = details.getName();
            if (!name.startsWith("ROLE_")) {
                name = "ROLE_" + name.toUpperCase();
            }
            role.setName(name);
        }

        return roleRepository.save(role);
    }

    @Transactional
    public boolean deleteRole(Long id) {
        return roleRepository.findById(id).map(role -> {
            // Prevent deletion of primary system roles
            if ("ROLE_ADMIN".equalsIgnoreCase(role.getName()) || "ROLE_CUSTOMER".equalsIgnoreCase(role.getName())) {
                throw new IllegalStateException("System critical role " + role.getName() + " cannot be deleted");
            }
            roleRepository.delete(role);
            return true;
        }).orElse(false);
    }

    @Transactional
    public User assignRoleToUser(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        String lookup = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
        Role role = roleRepository.findByName(lookup)
                .orElseThrow(() -> new RuntimeException("Role not found: " + lookup));

        user.addRole(role);
        return userRepository.save(user);
    }

    @Transactional
    public User removeRoleFromUser(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        String lookup = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
        roleRepository.findByName(lookup).ifPresent(user::removeRole);

        return userRepository.save(user);
    }
}

