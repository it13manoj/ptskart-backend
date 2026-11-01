package com.ptskart.api.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_gateways")
public class PaymentGatewayConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String gatewayKey; // razorpay, stripe, paypal, ccavenue

    @Column(nullable = false)
    private String name; // Razorpay, Stripe, PayPal, CCAvenue

    private boolean enabled;

    private String mode; // test, live, sandbox

    @Column(columnDefinition = "TEXT")
    private String apiKey; // Key ID / Publishable Key / Client ID / Merchant ID

    @Column(columnDefinition = "TEXT")
    private String apiSecret; // Key Secret / Secret Key / Secret / Working Key

    private String merchantId; // CCAvenue Merchant ID / PayPal Merchant ID

    private String accessCode; // CCAvenue Access Code

    private String webhookSecret;

    private String currency; // INR, USD, EUR, etc.

    private String description;

    private String supportedRails;

    private LocalDateTime updatedAt;

    public PaymentGatewayConfig() {
        this.updatedAt = LocalDateTime.now();
    }

    public PaymentGatewayConfig(String gatewayKey, String name, boolean enabled, String mode,
                                String apiKey, String apiSecret, String merchantId, String accessCode,
                                String webhookSecret, String currency, String description, String supportedRails) {
        this.gatewayKey = gatewayKey;
        this.name = name;
        this.enabled = enabled;
        this.mode = mode;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.merchantId = merchantId;
        this.accessCode = accessCode;
        this.webhookSecret = webhookSecret;
        this.currency = currency;
        this.description = description;
        this.supportedRails = supportedRails;
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getGatewayKey() { return gatewayKey; }
    public void setGatewayKey(String gatewayKey) { this.gatewayKey = gatewayKey; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public String getApiSecret() { return apiSecret; }
    public void setApiSecret(String apiSecret) { this.apiSecret = apiSecret; }

    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }

    public String getAccessCode() { return accessCode; }
    public void setAccessCode(String accessCode) { this.accessCode = accessCode; }

    public String getWebhookSecret() { return webhookSecret; }
    public void setWebhookSecret(String webhookSecret) { this.webhookSecret = webhookSecret; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSupportedRails() { return supportedRails; }
    public void setSupportedRails(String supportedRails) { this.supportedRails = supportedRails; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

