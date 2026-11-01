package com.ptskart.api.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transactions")
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String transactionId; // e.g. TXN-RZP-83921

    private String orderNumber; // e.g. #VX10294

    private String customerName;

    private String customerEmail;

    private String gatewayKey; // razorpay, stripe, paypal, ccavenue, cod

    private String gatewayName; // Razorpay, Stripe, PayPal, CCAvenue, Cash on Delivery

    private String paymentMethodType; // UPI (Google Pay), Credit Card (Visa), NetBanking (HDFC), etc.

    private Double amount = 0.0;

    private String currency = "USD";

    private String status = "SUCCESS"; // SUCCESS, PENDING, AUTHORIZED, FAILED, REFUNDED

    private String environmentMode = "TEST"; // TEST, LIVE

    private String gatewayPaymentId; // External gateway transaction ID (e.g. pay_N8912389, ch_3Mz981)

    private String gatewaySignature;

    private String errorMessage;

    private String receiptUrl;

    @Column(columnDefinition = "TEXT")
    private String metadataJson;

    private LocalDateTime createdAt = LocalDateTime.now();

    public PaymentTransaction() {}

    public PaymentTransaction(String transactionId, String orderNumber, String customerName,
                              String customerEmail, String gatewayKey, String gatewayName,
                              String paymentMethodType, Double amount, String currency,
                              String status, String environmentMode, String gatewayPaymentId) {
        this.transactionId = transactionId;
        this.orderNumber = orderNumber;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.gatewayKey = gatewayKey;
        this.gatewayName = gatewayName;
        this.paymentMethodType = paymentMethodType;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.environmentMode = environmentMode;
        this.gatewayPaymentId = gatewayPaymentId;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getGatewayKey() { return gatewayKey; }
    public void setGatewayKey(String gatewayKey) { this.gatewayKey = gatewayKey; }

    public String getGatewayName() { return gatewayName; }
    public void setGatewayName(String gatewayName) { this.gatewayName = gatewayName; }

    public String getPaymentMethodType() { return paymentMethodType; }
    public void setPaymentMethodType(String paymentMethodType) { this.paymentMethodType = paymentMethodType; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getEnvironmentMode() { return environmentMode; }
    public void setEnvironmentMode(String environmentMode) { this.environmentMode = environmentMode; }

    public String getGatewayPaymentId() { return gatewayPaymentId; }
    public void setGatewayPaymentId(String gatewayPaymentId) { this.gatewayPaymentId = gatewayPaymentId; }

    public String getGatewaySignature() { return gatewaySignature; }
    public void setGatewaySignature(String gatewaySignature) { this.gatewaySignature = gatewaySignature; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public String getReceiptUrl() { return receiptUrl; }
    public void setReceiptUrl(String receiptUrl) { this.receiptUrl = receiptUrl; }

    public String getMetadataJson() { return metadataJson; }
    public void setMetadataJson(String metadataJson) { this.metadataJson = metadataJson; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

