package com.ptskart.api.dto;

import java.util.List;
import java.util.Map;

public class PaymentProcessRequest {

    private String gatewayKey; // "stripe", "razorpay", "paypal", "ccavenue", "cod"
    private String orderNumber;
    private Double amount;
    private String currency; // "USD", "INR", etc.
    private String customerName;
    private String customerEmail;
    private String shippingAddress;

    // Card Details (for Stripe / Card)
    private String cardHolder;
    private String cardNumber;
    private String expiry; // "MM/YY" or "MM/YYYY"
    private String cvc;
    private String stripeToken;

    // UPI / NetBanking Details (for Razorpay / CCAvenue)
    private String upiId;
    private String selectedBank;

    // Line items summary
    private List<Map<String, Object>> items;

    public PaymentProcessRequest() {}

    public String getGatewayKey() { return gatewayKey; }
    public void setGatewayKey(String gatewayKey) { this.gatewayKey = gatewayKey; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public String getCardHolder() { return cardHolder; }
    public void setCardHolder(String cardHolder) { this.cardHolder = cardHolder; }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    public String getExpiry() { return expiry; }
    public void setExpiry(String expiry) { this.expiry = expiry; }

    public String getCvc() { return cvc; }
    public void setCvc(String cvc) { this.cvc = cvc; }

    public String getStripeToken() { return stripeToken; }
    public void setStripeToken(String stripeToken) { this.stripeToken = stripeToken; }

    public String getUpiId() { return upiId; }
    public void setUpiId(String upiId) { this.upiId = upiId; }

    public String getSelectedBank() { return selectedBank; }
    public void setSelectedBank(String selectedBank) { this.selectedBank = selectedBank; }

    public List<Map<String, Object>> getItems() { return items; }
    public void setItems(List<Map<String, Object>> items) { this.items = items; }
}

