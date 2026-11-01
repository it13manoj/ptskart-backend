package com.ptskart.api.controller;

import com.ptskart.api.dto.ApiResponse;
import com.ptskart.api.entity.Vendor;
import com.ptskart.api.repository.VendorRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vendors")
public class VendorController {

    private final VendorRepository vendorRepository;

    public VendorController(VendorRepository vendorRepository) {
        this.vendorRepository = vendorRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Vendor>>> getAllVendors() {
        return ResponseEntity.ok(ApiResponse.ok("Vendors retrieved successfully", vendorRepository.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Vendor>> getVendorById(@PathVariable Long id) {
        return vendorRepository.findById(id)
                .map(v -> ResponseEntity.ok(ApiResponse.ok("Vendor found", v)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Vendor>> createVendor(@RequestBody Vendor vendor) {
        return ResponseEntity.ok(ApiResponse.ok("Vendor registered successfully", vendorRepository.save(vendor)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Vendor>> updateVendor(@PathVariable Long id, @RequestBody Vendor details) {
        return vendorRepository.findById(id).map(v -> {
            v.setStoreName(details.getStoreName());
            v.setOwnerName(details.getOwnerName());
            v.setEmail(details.getEmail());
            v.setPhone(details.getPhone());
            v.setCommissionRate(details.getCommissionRate());
            v.setStatus(details.getStatus());
            return ResponseEntity.ok(ApiResponse.ok("Vendor updated", vendorRepository.save(v)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVendor(@PathVariable Long id) {
        vendorRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("Vendor deleted successfully"));
    }
}

