package com.ptskart.api.controller;

import com.ptskart.api.dto.ApiResponse;
import com.ptskart.api.dto.EmailSendRequest;
import com.ptskart.api.entity.EmailConfig;
import com.ptskart.api.entity.EmailLog;
import com.ptskart.api.entity.EmailTemplate;
import com.ptskart.api.service.DynamicEmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    private final DynamicEmailService emailService;

    public EmailController(DynamicEmailService emailService) {
        this.emailService = emailService;
    }

    // -------------------------------------------------------------
    // MULTI-VENDOR SMTP PROVIDER ENDPOINTS (Brevo, Gmail, SendGrid, Custom)
    // -------------------------------------------------------------
    @GetMapping("/providers")
    public ResponseEntity<ApiResponse<List<EmailConfig>>> getAllProviders() {
        List<EmailConfig> providers = emailService.getAllProviders();
        return ResponseEntity.ok(ApiResponse.ok("SMTP Providers loaded", providers));
    }

    @GetMapping("/providers/{key}")
    public ResponseEntity<ApiResponse<EmailConfig>> getProvider(@PathVariable String key) {
        return emailService.getProvider(key)
                .map(p -> ResponseEntity.ok(ApiResponse.ok("Provider loaded", p)))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("Provider not found: " + key)));
    }

    @PostMapping("/providers")
    public ResponseEntity<ApiResponse<EmailConfig>> saveProvider(@RequestBody EmailConfig providerConfig) {
        if (providerConfig.getProviderKey() == null || providerConfig.getProviderKey().isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Provider key is required (e.g. brevo, gmail, sendgrid)"));
        }
        EmailConfig saved = emailService.saveProvider(providerConfig);
        return ResponseEntity.ok(ApiResponse.ok("Provider (" + saved.getName() + ") configuration saved", saved));
    }

    @PostMapping("/providers/{key}/activate")
    public ResponseEntity<ApiResponse<EmailConfig>> activateProvider(@PathVariable String key) {
        EmailConfig activated = emailService.activateProvider(key);
        return ResponseEntity.ok(ApiResponse.ok("Provider '" + activated.getName() + "' is now the ACTIVE mail engine for all emails!", activated));
    }

    @PostMapping("/providers/{key}/test")
    public ResponseEntity<ApiResponse<Boolean>> testProvider(@PathVariable String key, @RequestBody Map<String, String> payload) {
        String to = payload.get("to");
        if (to == null || to.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Recipient 'to' email address is required"));
        }
        EmailConfig cfg = emailService.getProvider(key).orElse(null);
        if (cfg == null) {
            return ResponseEntity.status(404).body(ApiResponse.error("Provider not found: " + key));
        }

        // Allow overriding password/host from payload if testing unsaved form
        if (payload.containsKey("password") && !payload.get("password").isBlank()) {
            cfg.setPassword(payload.get("password"));
        }
        if (payload.containsKey("username") && !payload.get("username").isBlank()) {
            cfg.setUsername(payload.get("username"));
        }
        if (payload.containsKey("host") && !payload.get("host").isBlank()) {
            cfg.setHost(payload.get("host"));
        }
        if (payload.containsKey("port") && !payload.get("port").isBlank()) {
            try { cfg.setPort(Integer.parseInt(payload.get("port"))); } catch (Exception ignored) {}
        }
        if (payload.containsKey("fromEmail") && !payload.get("fromEmail").isBlank()) {
            cfg.setFromEmail(payload.get("fromEmail"));
        }

        boolean success = emailService.sendTestEmailForProvider(cfg, to.trim());
        if (success) {
            return ResponseEntity.ok(ApiResponse.ok("Test email dispatched successfully via " + cfg.getName() + " to " + to, true));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to deliver test email via " + cfg.getName() + ". Please check credentials and port."));
        }
    }

    // -------------------------------------------------------------
    // ACTIVE SMTP CONFIGURATION ENDPOINTS (Legacy compatibility)
    // -------------------------------------------------------------
    @GetMapping("/config")
    public ResponseEntity<ApiResponse<EmailConfig>> getConfig() {
        EmailConfig config = emailService.getCurrentConfig();
        return ResponseEntity.ok(ApiResponse.ok("Active SMTP Configuration loaded", config));
    }

    @PostMapping("/config")
    public ResponseEntity<ApiResponse<EmailConfig>> updateConfig(@RequestBody EmailConfig newConfig) {
        EmailConfig saved = emailService.saveProvider(newConfig);
        return ResponseEntity.ok(ApiResponse.ok("Active SMTP Configuration saved successfully", saved));
    }

    @PostMapping("/test")
    public ResponseEntity<ApiResponse<Boolean>> sendTestEmail(@RequestBody Map<String, String> payload) {
        String to = payload.get("to");
        if (to == null || to.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Recipient 'to' email address is required"));
        }
        boolean success = emailService.sendTestEmail(to.trim());
        if (success) {
            return ResponseEntity.ok(ApiResponse.ok("Test email sent successfully via active mailer to " + to, true));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to send test email. Check active SMTP credentials and logs."));
        }
    }

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<Boolean>> sendCustomEmail(@RequestBody EmailSendRequest request) {
        if (request.getTo() == null || request.getTo().isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Recipient 'to' is required"));
        }
        boolean success = emailService.sendEmail(request);
        if (success) {
            return ResponseEntity.ok(ApiResponse.ok("Email delivered successfully", true));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error("Email delivery failed. Please review active SMTP settings."));
        }
    }

    @GetMapping("/logs")
    public ResponseEntity<ApiResponse<List<EmailLog>>> getLogs() {
        List<EmailLog> logs = emailService.getEmailLogs();
        return ResponseEntity.ok(ApiResponse.ok("Email audit logs retrieved", logs));
    }

    // -------------------------------------------------------------
    // EMAIL TEMPLATES ENDPOINTS
    // -------------------------------------------------------------
    @GetMapping("/templates")
    public ResponseEntity<ApiResponse<List<EmailTemplate>>> getTemplates() {
        List<EmailTemplate> list = emailService.getAllTemplates();
        return ResponseEntity.ok(ApiResponse.ok("Email templates retrieved", list));
    }

    @GetMapping("/templates/{key}")
    public ResponseEntity<ApiResponse<EmailTemplate>> getTemplate(@PathVariable String key) {
        return emailService.getTemplateByKey(key)
                .map(t -> ResponseEntity.ok(ApiResponse.ok("Template retrieved", t)))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("Template not found: " + key)));
    }

    @PutMapping("/templates/{key}")
    public ResponseEntity<ApiResponse<EmailTemplate>> updateTemplate(@PathVariable String key, @RequestBody EmailTemplate updated) {
        EmailTemplate existing = emailService.getTemplateByKey(key).orElse(null);
        if (existing == null) {
            return ResponseEntity.status(404).body(ApiResponse.error("Template not found: " + key));
        }

        if (updated.getName() != null) existing.setName(updated.getName());
        if (updated.getSubject() != null) existing.setSubject(updated.getSubject());
        if (updated.getBodyHtml() != null) existing.setBodyHtml(updated.getBodyHtml());
        if (updated.getDescription() != null) existing.setDescription(updated.getDescription());
        if (updated.getVariables() != null) existing.setVariables(updated.getVariables());
        if (updated.getActive() != null) existing.setActive(updated.getActive());

        EmailTemplate saved = emailService.saveTemplate(existing);
        return ResponseEntity.ok(ApiResponse.ok("Template updated successfully", saved));
    }

    @PostMapping("/templates/{key}/preview")
    public ResponseEntity<ApiResponse<Map<String, String>>> previewTemplate(@PathVariable String key, @RequestBody(required = false) Map<String, Object> sampleVars) {
        EmailTemplate tpl = emailService.getTemplateByKey(key).orElse(null);
        if (tpl == null) {
            return ResponseEntity.status(404).body(ApiResponse.error("Template not found: " + key));
        }

        Map<String, Object> vars = getSampleVariables(key);
        if (sampleVars != null && !sampleVars.isEmpty()) {
            vars.putAll(sampleVars);
        }

        String renderedSubject = emailService.renderString(tpl.getSubject(), vars);
        String renderedHtml = emailService.renderString(tpl.getBodyHtml(), vars);

        Map<String, String> res = new HashMap<>();
        res.put("templateKey", key);
        res.put("subject", renderedSubject);
        res.put("html", renderedHtml);

        return ResponseEntity.ok(ApiResponse.ok("Preview rendered", res));
    }

    @PostMapping("/templates/{key}/test")
    public ResponseEntity<ApiResponse<Boolean>> sendTemplateTestEmail(@PathVariable String key, @RequestBody Map<String, Object> payload) {
        String to = (String) payload.get("to");
        if (to == null || to.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Recipient 'to' email address is required"));
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> customVars = (Map<String, Object>) payload.get("variables");

        Map<String, Object> vars = getSampleVariables(key);
        if (customVars != null && !customVars.isEmpty()) {
            vars.putAll(customVars);
        }

        boolean success = emailService.sendTemplateEmail(to.trim(), key, vars);
        if (success) {
            return ResponseEntity.ok(ApiResponse.ok("Test template email delivered to " + to, true));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to deliver test email. Check SMTP configuration and logs."));
        }
    }

    @PostMapping("/trigger")
    public ResponseEntity<ApiResponse<Boolean>> triggerTransactionalEmail(@RequestBody Map<String, Object> payload) {
        String templateKey = (String) payload.get("templateKey");
        String to = (String) payload.get("to");

        if (templateKey == null || templateKey.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("'templateKey' is required (otp, registration, invoice, etc.)"));
        }
        if (to == null || to.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Recipient 'to' is required"));
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> vars = (Map<String, Object>) payload.get("variables");
        if (vars == null) vars = new HashMap<>();

        boolean success = emailService.sendTemplateEmail(to.trim(), templateKey, vars);
        if (success) {
            return ResponseEntity.ok(ApiResponse.ok("Email triggered and delivered successfully", true));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to trigger email. Check email logs for details."));
        }
    }

    private Map<String, Object> getSampleVariables(String key) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("customerName", "Alexandre Dupont");
        vars.put("customerEmail", "alexandre.d@client.com");
        vars.put("storeName", "Ptskart Commerce");
        vars.put("shopUrl", "http://localhost:5173/");
        vars.put("loginUrl", "http://localhost:5173/");
        vars.put("bonusPoints", "250");
        vars.put("otpCode", "582914");
        vars.put("expiryMinutes", "10");
        vars.put("actionName", "secure account access");
        vars.put("orderNumber", "#VX66610");
        vars.put("orderDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        vars.put("shippingAddress", "42 Regent Street, Apt 4B, London, United Kingdom");
        vars.put("paymentMethod", "Stripe (Card / Apple Pay)");
        vars.put("carrier", "DHL Express Premium");
        vars.put("trackingNumber", "TRK-9842109842");
        vars.put("subtotal", "24.99");
        vars.put("shippingFee", "FREE");
        vars.put("totalAmount", "24.99");
        vars.put("trackOrderUrl", "http://localhost:5173/");

        String sampleItems = """
            <table width='100%' cellpadding='8' style='border-collapse:collapse;border:1px solid #ECE7DE;border-radius:8px;'>
              <tr style='background:#FAF8F5;text-align:left;font-size:12px;color:#8A92A0;'>
                <th style='padding:10px;'>Item</th>
                <th style='padding:10px;text-align:center;'>Qty</th>
                <th style='padding:10px;text-align:right;'>Price</th>
                <th style='padding:10px;text-align:right;'>Total</th>
              </tr>
              <tr style='border-top:1px solid #ECE7DE;font-size:13px;color:#1E2022;'>
                <td style='padding:10px;'><strong>Ptskart Basics Everyday Heavyweight Organic Tee (220 GSM)</strong><br><small style='color:#8A92A0;'>Charcoal Heather &bull; M</small></td>
                <td style='padding:10px;text-align:center;'>1</td>
                <td style='padding:10px;text-align:right;'>$24.99</td>
                <td style='padding:10px;text-align:right;font-weight:700;'>$24.99</td>
              </tr>
            </table>
        """;
        vars.put("itemsTable", sampleItems);

        vars.put("headline", "The Autumn Artisan Collection");
        vars.put("discountNotice", "Enjoy 15% off all knitwear and free carbon-neutral express shipping this week.");
        vars.put("couponCode", "HARMONY10");
        vars.put("discountAmount", "$10.00");
        vars.put("minSpend", "$50.00");
        vars.put("expiryDate", "Nov 30, 2026");
        vars.put("festivalName", "Holiday Season");
        vars.put("festiveMessage", "Wishing you joy, warmth, and memorable celebrations with family and friends. Thank you for choosing Ptskart as your home for thoughtful design.");
        vars.put("giftCoupon", "FESTIVE25");

        return vars;
    }
}
