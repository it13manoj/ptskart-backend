package com.ptskart.api.controller;

import com.ptskart.api.dto.ApiResponse;
import com.ptskart.api.entity.Role;
import com.ptskart.api.entity.User;
import com.ptskart.api.service.RoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/roles")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Role>>> getAllRoles() {
        List<Role> roles = roleService.getAllRoles();
        return ResponseEntity.ok(ApiResponse.ok("Roles fetched successfully from roles table", roles));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Role>> getRoleById(@PathVariable Long id) {
        return roleService.getRoleById(id)
                .map(role -> ResponseEntity.ok(ApiResponse.ok("Role details retrieved", role)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Role>> createRole(@RequestBody Role role) {
        try {
            Role created = roleService.createRole(role);
            return ResponseEntity.ok(ApiResponse.ok("Role created successfully in roles table", created));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Role>> updateRole(@PathVariable Long id, @RequestBody Role details) {
        try {
            Role updated = roleService.updateRole(id, details);
            return ResponseEntity.ok(ApiResponse.ok("Role updated successfully", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable Long id) {
        try {
            boolean deleted = roleService.deleteRole(id);
            if (deleted) {
                return ResponseEntity.ok(ApiResponse.ok("Role removed from roles table", null));
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/assign")
    public ResponseEntity<ApiResponse<User>> assignRole(@RequestBody Map<String, Object> body) {
        try {
            Long userId = Long.valueOf(body.get("userId").toString());
            String roleName = body.get("roleName").toString();
            User user = roleService.assignRoleToUser(userId, roleName);
            user.setPassword("••••••••");
            return ResponseEntity.ok(ApiResponse.ok("Role assigned to user successfully", user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/revoke")
    public ResponseEntity<ApiResponse<User>> revokeRole(@RequestBody Map<String, Object> body) {
        try {
            Long userId = Long.valueOf(body.get("userId").toString());
            String roleName = body.get("roleName").toString();
            User user = roleService.removeRoleFromUser(userId, roleName);
            user.setPassword("••••••••");
            return ResponseEntity.ok(ApiResponse.ok("Role revoked from user successfully", user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}

