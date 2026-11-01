package com.ptskart.api.service;

import com.ptskart.api.dto.EmailSendRequest;
import com.ptskart.api.dto.OrderStatusUpdateRequest;
import com.ptskart.api.entity.Order;
import com.ptskart.api.entity.OrderItem;
import com.ptskart.api.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final DynamicEmailService emailService;

    public OrderService(OrderRepository orderRepository, DynamicEmailService emailService) {
        this.orderRepository = orderRepository;
        this.emailService = emailService;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
    }

    public Order getOrderByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found with number: " + orderNumber));
    }

    public Order getOrderByTrackingNumber(String trackingNumber) {
        return orderRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new RuntimeException("Order not found with tracking number: " + trackingNumber));
    }

    public List<Order> getOrdersByCustomer(String email) {
        return orderRepository.findByCustomerEmailOrderByCreatedAtDesc(email);
    }

    @Transactional
    public Order createOrder(Order order) {
        if (order.getOrderNumber() == null || order.getOrderNumber().isBlank()) {
            int randomCode = 10000 + new Random().nextInt(90000);
            order.setOrderNumber("#VX" + randomCode);
        }
        if (order.getTrackingNumber() == null || order.getTrackingNumber().isBlank()) {
            long trackingDigits = 1000000000L + (long)(new Random().nextDouble() * 9000000000L);
            order.setTrackingNumber("TRK" + trackingDigits);
        }
        if (order.getCarrier() == null || order.getCarrier().isBlank()) {
            order.setCarrier("DHL Express Premium");
        }
        if (order.getEstimatedDelivery() == null || order.getEstimatedDelivery().isBlank()) {
            order.setEstimatedDelivery("Within 2-3 business days");
        }
        if (order.getCreatedAt() == null) {
            order.setCreatedAt(LocalDateTime.now());
        }

        // Link child items
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                item.setOrder(order);
            }
        }

        Order saved = orderRepository.save(order);

        // Send official order invoice email using the dynamic Invoice Template
        try {
            if (saved.getCustomerEmail() != null && !saved.getCustomerEmail().isBlank()) {
                emailService.sendInvoiceEmail(saved.getCustomerEmail().trim(), saved);
            }
        } catch (Exception ex) {
            System.err.println("[OrderService] Could not send invoice email: " + ex.getMessage());
        }

        return saved;
    }

    @Transactional
    public Order updateOrderStatus(Long id, OrderStatusUpdateRequest req) {
        Order order = getOrderById(id);
        if (req.getOrderStatus() != null && !req.getOrderStatus().isBlank()) {
            order.setOrderStatus(req.getOrderStatus());
        }
        if (req.getCarrier() != null && !req.getCarrier().isBlank()) {
            order.setCarrier(req.getCarrier());
        }
        if (req.getTrackingNumber() != null && !req.getTrackingNumber().isBlank()) {
            order.setTrackingNumber(req.getTrackingNumber());
        }
        Order saved = orderRepository.save(order);

        // Send status email notification
        try {
            if (saved.getCustomerEmail() != null && !saved.getCustomerEmail().isBlank()) {
                String body = """
                    <div style="font-family: -apple-system, sans-serif; background-color: #FAF8F5; padding: 30px;">
                        <div style="max-width: 560px; margin: 0 auto; background: #fff; border-radius: 16px; border: 1px solid rgba(226, 178, 97, 0.3); padding: 28px;">
                            <h2 style="color: #1E2022; margin-top: 0;">Order Status Updated: %s</h2>
                            <p style="color: #7B8089; font-size: 14px;">Hi %s, your order %s has changed status to: <strong style="color: #D97706;">%s</strong>.</p>
                            <p style="font-size: 13px; color: #4A4F57;">Tracking Number: <strong>%s</strong> (%s)</p>
                        </div>
                    </div>
                """.formatted(saved.getOrderStatus(), saved.getCustomerName(), saved.getOrderNumber(), saved.getOrderStatus(), saved.getTrackingNumber(), saved.getCarrier());

                EmailSendRequest emailReq = new EmailSendRequest(
                        saved.getCustomerEmail(),
                        "Status Update for Order " + saved.getOrderNumber(),
                        body
                );
                emailService.sendEmail(emailReq);
            }
        } catch (Exception ignored) {}

        return saved;
    }
}

