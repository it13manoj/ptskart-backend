package com.ptskart.api.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "gift_cards")
public class GiftCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code; // e.g. GIFT-HARMONY-8841

    @Column(nullable = false)
    private Double initialBalance;

    @Column(nullable = false)
    private Double currentBalance;

    private String recipientEmail;

    private String recipientName;

    private LocalDate expiryDate = LocalDate.now().plusYears(1);

    private Boolean active = true;

    public GiftCard() {}

    public GiftCard(String code, Double initialBalance, Double currentBalance, String recipientEmail, String recipientName) {
        this.code = code;
        this.initialBalance = initialBalance;
        this.currentBalance = currentBalance;
        this.recipientEmail = recipientEmail;
        this.recipientName = recipientName;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Double getInitialBalance() { return initialBalance; }
    public void setInitialBalance(Double initialBalance) { this.initialBalance = initialBalance; }

    public Double getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(Double currentBalance) { this.currentBalance = currentBalance; }

    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}

