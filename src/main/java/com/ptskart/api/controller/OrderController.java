package com.ptskart.api.controller;

import com.ptskart.api.dto.ApiResponse;
import com.ptskart.api.dto.OrderStatusUpdateRequest;
import com.ptskart.api.entity.Order;
import com.ptskart.api.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Order>>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        return ResponseEntity.ok(ApiResponse.ok("Orders fetched successfully", orders));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Order>> getOrderById(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponse.ok("Order fetched successfully", order));
    }

    @GetMapping("/track/{trackingNumber}")
    public ResponseEntity<ApiResponse<Order>> trackOrder(@PathVariable String trackingNumber) {
        try {
            Order order = orderService.getOrderByTrackingNumber(trackingNumber);
            return ResponseEntity.ok(ApiResponse.ok("Order tracked successfully", order));
        } catch (Exception e) {
            try {
                // Fallback to orderNumber
                Order order = orderService.getOrderByOrderNumber(trackingNumber);
                return ResponseEntity.ok(ApiResponse.ok("Order found by order number", order));
            } catch (Exception ex2) {
                return ResponseEntity.badRequest().body(ApiResponse.error("No shipment found for code: " + trackingNumber));
            }
        }
    }

    @GetMapping("/my-orders")
    public ResponseEntity<ApiResponse<List<Order>>> getMyOrders(@RequestParam(required = false) String email,
                                                                 Authentication authentication) {
        String queryEmail = email;
        if ((queryEmail == null || queryEmail.isBlank()) && authentication != null) {
            queryEmail = authentication.getName();
        }
        if (queryEmail == null || queryEmail.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Email required to retrieve customer orders"));
        }
        List<Order> orders = orderService.getOrdersByCustomer(queryEmail);
        return ResponseEntity.ok(ApiResponse.ok("Customer orders retrieved", orders));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Order>> createOrder(@RequestBody Order order) {
        Order created = orderService.createOrder(order);
        return ResponseEntity.ok(ApiResponse.ok("Order created and confirmation sent", created));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Order>> updateStatus(@PathVariable Long id,
                                                           @RequestBody OrderStatusUpdateRequest req) {
        Order updated = orderService.updateOrderStatus(id, req);
        return ResponseEntity.ok(ApiResponse.ok("Order status updated and customer notified", updated));
    }
}

