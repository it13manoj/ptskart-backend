package com.ptskart.api.controller;

import com.ptskart.api.dto.ApiResponse;
import com.ptskart.api.entity.PaymentGatewayConfig;
import com.ptskart.api.repository.PaymentGatewayRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/payment-gateways")
@CrossOrigin(origins = "*")
public class PaymentGatewayController {

    private final PaymentGatewayRepository repository;

    public PaymentGatewayController(PaymentGatewayRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    public void initDefaults() {
        if (repository.count() == 0) {
            List<PaymentGatewayConfig> defaults = List.of(
                new PaymentGatewayConfig(
                    "razorpay",
                    "Razorpay",
                    true,
                    "test",
                    "rzp_test_9A12B34C56D78E",
                    "sec_rzp_test_secret_99827",
                    "rzp_merch_veylix",
                    null,
                    "whsec_rzp_sample_hook",
                    "INR",
                    "Accept payments via UPI, Google Pay, PhonePe, Paytm, Credit/Debit cards & NetBanking.",
                    "UPI, Cards, Netbanking, Wallets"
                ),
                new PaymentGatewayConfig(
                    "stripe",
                    "Stripe",
                    true,
                    "test",
                    "pk_test_51MzSampleStripeKey9900",
                    "sk_test_51MzSecretKeySample9900",
                    null,
                    null,
                    "whsec_stripe_test_sample",
                    "USD",
                    "Global payments with credit cards, debit cards, Apple Pay, and Google Pay.",
                    "Visa, Mastercard, Amex, Apple Pay"
                ),
                new PaymentGatewayConfig(
                    "paypal",
                    "PayPal",
                    true,
                    "sandbox",
                    "sb-clientid-veylix-live-preview",
                    "sb-secret-paypal-sample-key",
                    "merch_paypal_veylix",
                    null,
                    null,
                    "USD",
                    "Pay easily and securely using PayPal balance, bank accounts, or Pay in 4.",
                    "PayPal, Pay in 4, Venmo"
                ),
                new PaymentGatewayConfig(
                    "ccavenue",
                    "CCAvenue",
                    true,
                    "test",
                    "CCAV_MERCH_29841",
                    "CCAV_WORKING_KEY_9981881",
                    "CCAV_MERCH_29841",
                    "AVNE9892019283",
                    null,
                    "INR",
                    "India's premier gateway with 200+ payment options and 58+ Net Banking banks.",
                    "58+ Netbanking, RuPay, Visa, Wallets"
                )
            );
            repository.saveAll(defaults);
        }
    }

    // 1. Get all gateway configurations (for Admin Panel)
    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentGatewayConfig>>> getAllGateways() {
        return ResponseEntity.ok(ApiResponse.ok("Payment gateways fetched", repository.findAll()));
    }

    // 2. Get active/enabled gateways (for Storefront Checkout)
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getActiveGateways() {
        List<PaymentGatewayConfig> active = repository.findByEnabledTrue();
        List<Map<String, Object>> safeList = new ArrayList<>();

        for (PaymentGatewayConfig p : active) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", p.getId());
            map.put("gatewayKey", p.getGatewayKey());
            map.put("name", p.getName());
            map.put("enabled", p.isEnabled());
            map.put("mode", p.getMode());
            map.put("apiKey", p.getApiKey());
            map.put("merchantId", p.getMerchantId());
            map.put("accessCode", p.getAccessCode());
            map.put("currency", p.getCurrency());
            map.put("description", p.getDescription());
            map.put("supportedRails", p.getSupportedRails());
            safeList.add(map);
        }

        return ResponseEntity.ok(ApiResponse.ok("Active gateways fetched for checkout", safeList));
    }

    // 3. Update gateway configuration details
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentGatewayConfig>> updateGateway(
            @PathVariable Long id,
            @RequestBody PaymentGatewayConfig payload) {

        return repository.findById(id).map(existing -> {
            if (payload.getName() != null) existing.setName(payload.getName());
            existing.setEnabled(payload.isEnabled());
            if (payload.getMode() != null) existing.setMode(payload.getMode());
            if (payload.getApiKey() != null) existing.setApiKey(payload.getApiKey());
            if (payload.getApiSecret() != null) existing.setApiSecret(payload.getApiSecret());
            if (payload.getMerchantId() != null) existing.setMerchantId(payload.getMerchantId());
            if (payload.getAccessCode() != null) existing.setAccessCode(payload.getAccessCode());
            if (payload.getWebhookSecret() != null) existing.setWebhookSecret(payload.getWebhookSecret());
            if (payload.getCurrency() != null) existing.setCurrency(payload.getCurrency());
            if (payload.getDescription() != null) existing.setDescription(payload.getDescription());
            if (payload.getSupportedRails() != null) existing.setSupportedRails(payload.getSupportedRails());

            PaymentGatewayConfig saved = repository.save(existing);
            return ResponseEntity.ok(ApiResponse.ok("Payment gateway updated successfully", saved));
        }).orElse(ResponseEntity.notFound().build());
    }

    // 4. Quick toggle enabled state
    @PostMapping("/{key}/toggle")
    public ResponseEntity<ApiResponse<PaymentGatewayConfig>> toggleGateway(
            @PathVariable String key,
            @RequestParam(required = false) Boolean enabled) {

        return repository.findByGatewayKey(key.toLowerCase()).map(gw -> {
            boolean newState = (enabled != null) ? enabled : !gw.isEnabled();
            gw.setEnabled(newState);
            PaymentGatewayConfig saved = repository.save(gw);
            return ResponseEntity.ok(ApiResponse.ok(
                    gw.getName() + " is now " + (newState ? "ENABLED (active on frontstore)" : "DISABLED"),
                    saved
            ));
        }).orElse(ResponseEntity.notFound().build());
    }

    // 5. Add new gateway / merchant configuration
    @PostMapping
    public ResponseEntity<ApiResponse<PaymentGatewayConfig>> createGateway(@RequestBody PaymentGatewayConfig payload) {
        if (payload.getGatewayKey() == null || payload.getGatewayKey().trim().isEmpty()) {
            payload.setGatewayKey("gw_" + System.currentTimeMillis());
        }
        if (payload.getName() == null || payload.getName().trim().isEmpty()) {
            payload.setName("Payment Gateway");
        }
        PaymentGatewayConfig saved = repository.save(payload);
        return ResponseEntity.ok(ApiResponse.ok("Payment gateway added successfully", saved));
    }

    // 6. Delete gateway
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteGateway(@PathVariable Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return ResponseEntity.ok(ApiResponse.ok("Payment gateway removed", null));
        }
        return ResponseEntity.notFound().build();
    }
}

