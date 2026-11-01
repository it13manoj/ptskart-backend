package com.ptskart.api.controller;

import com.ptskart.api.dto.ApiResponse;
import com.ptskart.api.entity.Coupon;
import com.ptskart.api.repository.CouponRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/coupons")
public class CouponController {

    private final CouponRepository couponRepository;

    public CouponController(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Coupon>>> getAllCoupons() {
        return ResponseEntity.ok(ApiResponse.ok("Coupons retrieved successfully", couponRepository.findAll()));
    }

    @GetMapping("/validate/{code}")
    public ResponseEntity<ApiResponse<Coupon>> validateCoupon(@PathVariable String code, @RequestParam(defaultValue = "0") Double orderAmount) {
        return couponRepository.findByCodeIgnoreCase(code).map(coupon -> {
            if (!Boolean.TRUE.equals(coupon.getActive())) {
                return ResponseEntity.badRequest().body(ApiResponse.<Coupon>error("Coupon is inactive"));
            }
            if (coupon.getValidUntil() != null && coupon.getValidUntil().isBefore(LocalDate.now())) {
                return ResponseEntity.badRequest().body(ApiResponse.<Coupon>error("Coupon has expired"));
            }
            if (coupon.getMinOrderValue() != null && orderAmount < coupon.getMinOrderValue()) {
                return ResponseEntity.badRequest().body(ApiResponse.<Coupon>error("Minimum order value for this coupon is $" + coupon.getMinOrderValue()));
            }
            return ResponseEntity.ok(ApiResponse.ok("Coupon code applied successfully", coupon));
        }).orElse(ResponseEntity.badRequest().body(ApiResponse.error("Invalid coupon code: " + code)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Coupon>> createCoupon(@RequestBody Coupon coupon) {
        coupon.setCode(coupon.getCode().toUpperCase().trim());
        return ResponseEntity.ok(ApiResponse.ok("Coupon created successfully", couponRepository.save(coupon)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCoupon(@PathVariable Long id) {
        couponRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("Coupon deleted successfully"));
    }
}

