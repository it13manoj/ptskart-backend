package com.ptskart.api.service;

import com.ptskart.api.dto.EmailSendRequest;
import com.ptskart.api.entity.EmailConfig;
import com.ptskart.api.entity.EmailLog;
import com.ptskart.api.entity.EmailTemplate;
import com.ptskart.api.entity.Order;
import com.ptskart.api.entity.OrderItem;
import com.ptskart.api.repository.EmailConfigRepository;
import com.ptskart.api.repository.EmailLogRepository;
import com.ptskart.api.repository.EmailTemplateRepository;
import jakarta.annotation.PostConstruct;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class DynamicEmailService {

    public static final String DEFAULT_BREVO_HOST = "smtp-relay.brevo.com";
    public static final int DEFAULT_BREVO_PORT = 587;
    public static final String DEFAULT_BREVO_USER = System.getenv().getOrDefault("BREVO_USER", "bbd375001@smtp-brevo.com");
    public static final String DEFAULT_BREVO_KEY = System.getenv().getOrDefault("BREVO_SMTP_KEY", "");
    public static final String DEFAULT_SENDER_EMAIL = System.getenv().getOrDefault("SENDER_EMAIL", "info@batohidriver.com");
    public static final String DEFAULT_SENDER_NAME = "Ptskart Commerce";

    private final EmailConfigRepository emailConfigRepository;
    private final EmailLogRepository emailLogRepository;
    private final EmailTemplateRepository emailTemplateRepository;

    public DynamicEmailService(EmailConfigRepository emailConfigRepository,
                               EmailLogRepository emailLogRepository,
                               EmailTemplateRepository emailTemplateRepository) {
        this.emailConfigRepository = emailConfigRepository;
        this.emailLogRepository = emailLogRepository;
        this.emailTemplateRepository = emailTemplateRepository;
    }

    @PostConstruct
    public void init() {
        initDefaultConfig();
        initDefaultTemplates();
    }

    // -------------------------------------------------------------
    // PROVIDER MANAGEMENT & INITIALIZATION (Brevo, Gmail, SendGrid)
    // -------------------------------------------------------------
    @Transactional
    public synchronized void initDefaultConfig() {
        // 1. Brevo (Default Active Provider)
        if (!emailConfigRepository.existsByProviderKey("brevo")) {
            EmailConfig brevo = new EmailConfig(
                    "brevo",
                    "Brevo (Sendinblue)",
                    DEFAULT_BREVO_HOST,
                    DEFAULT_BREVO_PORT,
                    DEFAULT_BREVO_USER,
                    DEFAULT_BREVO_KEY,
                    DEFAULT_SENDER_EMAIL,
                    DEFAULT_SENDER_NAME,
                    true // Active by default
            );
            emailConfigRepository.save(brevo);
        } else {
            EmailConfig brevo = emailConfigRepository.findByProviderKey("brevo").orElse(null);
            if (brevo != null && (brevo.getPassword() == null || brevo.getPassword().isBlank() || !brevo.getPassword().contains("xsmtpsib"))) {
                brevo.setPassword(DEFAULT_BREVO_KEY);
                brevo.setUsername(DEFAULT_BREVO_USER);
                brevo.setFromEmail(DEFAULT_SENDER_EMAIL);
                brevo.setHost(DEFAULT_BREVO_HOST);
                brevo.setActive(true);
                emailConfigRepository.save(brevo);
            }
        }

        // 2. Google Gmail SMTP
        if (!emailConfigRepository.existsByProviderKey("gmail")) {
            EmailConfig gmail = new EmailConfig(
                    "gmail",
                    "Google Gmail SMTP",
                    "smtp.gmail.com",
                    587,
                    "notifications@ptskart.com",
                    "",
                    "noreply@ptskart.com",
                    DEFAULT_SENDER_NAME,
                    false
            );
            emailConfigRepository.save(gmail);
        }

        // 3. Twilio SendGrid
        if (!emailConfigRepository.existsByProviderKey("sendgrid")) {
            EmailConfig sendgrid = new EmailConfig(
                    "sendgrid",
                    "Twilio SendGrid",
                    "smtp.sendgrid.net",
                    587,
                    "apikey",
                    "",
                    DEFAULT_SENDER_EMAIL,
                    DEFAULT_SENDER_NAME,
                    false
            );
            emailConfigRepository.save(sendgrid);
        }

        // 4. Custom Corporate SMTP
        if (!emailConfigRepository.existsByProviderKey("custom")) {
            EmailConfig custom = new EmailConfig(
                    "custom",
                    "Custom Corporate SMTP",
                    "mail.ptskart.com",
                    587,
                    "smtp@ptskart.com",
                    "",
                    "concierge@ptskart.com",
                    DEFAULT_SENDER_NAME,
                    false
            );
            emailConfigRepository.save(custom);
        }
    }

    public List<EmailConfig> getAllProviders() {
        return emailConfigRepository.findAllByOrderByIdAsc();
    }

    public Optional<EmailConfig> getProvider(String providerKey) {
        return emailConfigRepository.findByProviderKey(providerKey.toLowerCase());
    }

    public EmailConfig getCurrentConfig() {
        return emailConfigRepository.findFirstByActiveTrueOrderByIdDesc()
                .orElseGet(() -> emailConfigRepository.findByProviderKey("brevo")
                .orElseGet(() -> emailConfigRepository.findFirstByOrderByIdDesc()
                .orElseGet(() -> {
                    EmailConfig def = new EmailConfig(
                            "brevo",
                            "Brevo (Sendinblue)",
                            DEFAULT_BREVO_HOST,
                            DEFAULT_BREVO_PORT,
                            DEFAULT_BREVO_USER,
                            DEFAULT_BREVO_KEY,
                            DEFAULT_SENDER_EMAIL,
                            DEFAULT_SENDER_NAME,
                            true
                    );
                    return emailConfigRepository.save(def);
                })));
    }

    @Transactional
    public EmailConfig saveProvider(EmailConfig cfg) {
        if (Boolean.TRUE.equals(cfg.getActive())) {
            // Deactivate all others so only 1 provider is actively sending
            List<EmailConfig> all = emailConfigRepository.findAll();
            for (EmailConfig other : all) {
                if (!Objects.equals(other.getId(), cfg.getId())) {
                    other.setActive(false);
                    emailConfigRepository.save(other);
                }
            }
        }
        cfg.setUpdatedAt(LocalDateTime.now());
        return emailConfigRepository.save(cfg);
    }

    @Transactional
    public EmailConfig activateProvider(String providerKey) {
        List<EmailConfig> all = emailConfigRepository.findAll();
        EmailConfig activated = null;
        for (EmailConfig item : all) {
            if (providerKey.equalsIgnoreCase(item.getProviderKey())) {
                item.setActive(true);
                item.setUpdatedAt(LocalDateTime.now());
                activated = emailConfigRepository.save(item);
            } else {
                item.setActive(false);
                emailConfigRepository.save(item);
            }
        }
        return activated != null ? activated : getCurrentConfig();
    }

    public List<EmailLog> getEmailLogs() {
        return emailLogRepository.findAllByOrderBySentAtDesc();
    }

    // -------------------------------------------------------------
    // EMAIL SENDING ENGINE (Mailsender based on active or specific config)
    // -------------------------------------------------------------
    private JavaMailSenderImpl buildMailSender(EmailConfig cfg) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(cfg.getHost() != null ? cfg.getHost().trim() : DEFAULT_BREVO_HOST);
        sender.setPort(cfg.getPort() != null ? cfg.getPort() : DEFAULT_BREVO_PORT);
        if (cfg.getUsername() != null && !cfg.getUsername().isBlank()) {
            sender.setUsername(cfg.getUsername().trim());
        }
        if (cfg.getPassword() != null && !cfg.getPassword().isBlank()) {
            sender.setPassword(cfg.getPassword().trim());
        }

        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", String.valueOf(Boolean.TRUE.equals(cfg.getSmtpAuth())));
        props.put("mail.smtp.starttls.enable", String.valueOf(Boolean.TRUE.equals(cfg.getStartTls())));
        props.put("mail.smtp.ssl.enable", String.valueOf(Boolean.TRUE.equals(cfg.getSslEnable())));
        props.put("mail.smtp.connectiontimeout", "8000");
        props.put("mail.smtp.timeout", "8000");
        props.put("mail.smtp.writetimeout", "8000");

        return sender;
    }

    public boolean sendEmail(EmailSendRequest request) {
        return sendEmailWithProvider(getCurrentConfig(), request);
    }

    public boolean sendEmailWithProvider(EmailConfig cfg, EmailSendRequest request) {
        EmailLog log = new EmailLog();
        log.setRecipient(request.getTo());
        log.setSubject(request.getSubject());
        log.setContent(request.getBody());
        log.setProvider(cfg.getName() != null ? cfg.getName() : cfg.getProviderKey());
        log.setSentAt(LocalDateTime.now());

        if (!Boolean.TRUE.equals(cfg.getActive())) {
            log.setStatus("SKIPPED");
            log.setErrorMessage("Provider (" + cfg.getName() + ") is marked inactive.");
            emailLogRepository.save(log);
            return false;
        }

        try {
            JavaMailSenderImpl sender = buildMailSender(cfg);
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String fromEmail = (cfg.getFromEmail() != null && !cfg.getFromEmail().isBlank())
                    ? cfg.getFromEmail().trim()
                    : DEFAULT_SENDER_EMAIL;
            String fromName = (cfg.getFromName() != null && !cfg.getFromName().isBlank())
                    ? cfg.getFromName().trim()
                    : DEFAULT_SENDER_NAME;

            helper.setFrom(fromEmail, fromName);
            helper.setTo(request.getTo());
            helper.setSubject(request.getSubject());
            helper.setText(request.getBody(), request.isHtml());

            sender.send(message);

            log.setStatus("SENT");
            log.setErrorMessage(null);
            emailLogRepository.save(log);
            return true;
        } catch (Exception ex) {
            log.setStatus("FAILED");
            String err = ex.getMessage() != null ? ex.getMessage() : ex.toString();
            log.setErrorMessage(err);
            emailLogRepository.save(log);
            System.err.println("[DynamicEmailService Error via " + cfg.getName() + "] " + err);
            return false;
        }
    }

    public boolean sendTestEmail(String toEmail) {
        return sendTestEmailForProvider(getCurrentConfig(), toEmail);
    }

    public boolean sendTestEmailForProvider(EmailConfig cfg, String toEmail) {
        String htmlBody = """
            <div style="margin: 0; padding: 36px 16px; background-color: #F8F6F0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; color: #1E2022;">
              <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%%" style="max-width: 600px; margin: 0 auto; background: #FFFFFF; border-radius: 16px; overflow: hidden; border: 1px solid #EFEAE0; box-shadow: 0 12px 32px rgba(0,0,0,0.05);">
                <tr>
                  <td style="background: linear-gradient(135deg, #1E2022 0%%, #2D3139 100%%); padding: 28px 32px; text-align: left;">
                    <span style="font-size: 24px; font-weight: 800; color: #E2B261; letter-spacing: -0.5px;">Ptskart</span>
                    <span style="display: block; font-size: 13px; color: #C5CBD3; margin-top: 4px;">%s SMTP Engine Verified</span>
                  </td>
                </tr>
                <tr>
                  <td style="padding: 32px;">
                    <div style="display: inline-block; padding: 6px 14px; background: #E8F5E9; color: #2E7D32; font-size: 12px; font-weight: 700; border-radius: 20px; text-transform: uppercase; letter-spacing: 0.5px;">
                      ✓ Connection Live & Verified
                    </div>
                    <h2 style="margin: 16px 0 10px; font-size: 20px; color: #1E2022;">SMTP Test Successful</h2>
                    <p style="margin: 0 0 20px; font-size: 14px; line-height: 1.6; color: #5B626E;">
                      Your <strong>%s</strong> SMTP credentials are communicating properly with the mail server. Transactional OTP codes, registration greetings, and order invoices will now be delivered via this active mailer.
                    </p>
                    <table style="width: 100%%; background: #FAF8F5; border-radius: 10px; border: 1px solid #ECE7DE; padding: 16px; margin-bottom: 24px;" cellpadding="6">
                      <tr><td style="font-size: 12px; color: #8A92A0;">Active Provider:</td><td style="font-size: 13px; font-weight: 700; color: #1E2022;">%s</td></tr>
                      <tr><td style="font-size: 12px; color: #8A92A0;">SMTP Host:</td><td style="font-size: 13px; font-weight: 600;">%s:%d</td></tr>
                      <tr><td style="font-size: 12px; color: #8A92A0;">Sender Address:</td><td style="font-size: 13px; font-weight: 600;">%s</td></tr>
                      <tr><td style="font-size: 12px; color: #8A92A0;">Delivered At:</td><td style="font-size: 13px; font-weight: 600;">%s</td></tr>
                    </table>
                    <p style="margin: 0; font-size: 12px; color: #8A92A0; text-align: center;">
                      &copy; 2026 Ptskart E-Commerce Inc. All rights reserved.
                    </p>
                  </td>
                </tr>
              </table>
            </div>
        """.formatted(
                cfg.getName(),
                cfg.getName(),
                cfg.getName(),
                cfg.getHost(),
                cfg.getPort(),
                cfg.getFromEmail(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss"))
        );

        EmailSendRequest req = new EmailSendRequest(toEmail, "✅ Ptskart SMTP Integration Test - " + cfg.getName(), htmlBody);
        req.setHtml(true);

        // Temporarily ensure active for test if needed
        boolean wasActive = Boolean.TRUE.equals(cfg.getActive());
        cfg.setActive(true);
        boolean result = sendEmailWithProvider(cfg, req);
        cfg.setActive(wasActive);
        return result;
    }

    // -------------------------------------------------------------
    // TEMPLATE MANAGEMENT & SEEDING (6 Core Templates)
    // -------------------------------------------------------------
    public List<EmailTemplate> getAllTemplates() {
        return emailTemplateRepository.findAllByOrderByIdAsc();
    }

    public Optional<EmailTemplate> getTemplateByKey(String key) {
        return emailTemplateRepository.findByTemplateKey(key);
    }

    public EmailTemplate saveTemplate(EmailTemplate template) {
        template.setUpdatedAt(LocalDateTime.now());
        return emailTemplateRepository.save(template);
    }

    public synchronized void initDefaultTemplates() {
        // 1. OTP Verification
        if (!emailTemplateRepository.existsByTemplateKey("otp")) {
            emailTemplateRepository.save(new EmailTemplate(
                    "otp",
                    "OTP Verification Code",
                    "Security",
                    "🔐 Your Verification Passcode: {{otpCode}} - Ptskart",
                    """
                    <div style="margin:0;padding:36px 16px;background-color:#F8F6F0;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif;color:#1E2022;">
                      <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="max-width:540px;margin:0 auto;background:#FFFFFF;border-radius:18px;overflow:hidden;border:1px solid #ECE7DE;box-shadow:0 12px 32px rgba(0,0,0,0.04);">
                        <tr>
                          <td style="background:linear-gradient(135deg,#1E2022 0%,#2C3038 100%);padding:28px 32px;text-align:center;">
                            <span style="font-size:24px;font-weight:800;color:#E2B261;letter-spacing:-0.5px;">Ptskart</span>
                            <span style="display:block;font-size:12px;color:#A9B0BC;margin-top:4px;letter-spacing:1px;text-transform:uppercase;">Identity Verification</span>
                          </td>
                        </tr>
                        <tr>
                          <td style="padding:36px 32px;text-align:center;">
                            <div style="font-size:42px;margin-bottom:12px;">🛡️</div>
                            <h2 style="margin:0 0 8px;font-size:22px;color:#1E2022;font-weight:700;">One-Time Passcode</h2>
                            <p style="margin:0 0 24px;font-size:14px;color:#5B626E;line-height:1.6;">
                              Hi <strong>{{customerName}}</strong>, please use the secure 6-digit code below to complete your <strong>{{actionName}}</strong>.
                            </p>
                            <div style="margin:24px auto;padding:18px 28px;background:#FAF8F5;border:2px dashed #E2B261;border-radius:14px;display:inline-block;">
                              <span style="font-family:'Courier New',Courier,monospace;font-size:36px;font-weight:800;letter-spacing:8px;color:#1E2022;">{{otpCode}}</span>
                            </div>
                            <p style="margin:16px 0 0;font-size:13px;color:#8A92A0;">
                              ⏱️ Valid for <strong>{{expiryMinutes}} minutes</strong>. For your security, never share this code.
                            </p>
                            <div style="margin-top:28px;padding-top:20px;border-top:1px solid #ECE7DE;font-size:12px;color:#A9B0BC;line-height:1.5;">
                              If you did not request this verification, please ignore this email or contact support immediately.
                            </div>
                          </td>
                        </tr>
                      </table>
                    </div>
                    """,
                    "Instant 6-digit OTP code sent for customer login, password reset, or checkout verification.",
                    "{{customerName}}, {{otpCode}}, {{expiryMinutes}}, {{actionName}}"
            ));
        }

        // 2. Customer Registration / Welcome
        if (!emailTemplateRepository.existsByTemplateKey("registration")) {
            emailTemplateRepository.save(new EmailTemplate(
                    "registration",
                    "Customer Registration Welcome",
                    "Onboarding",
                    "🎉 Welcome to Ptskart, {{customerName}}! Your curated journey begins",
                    """
                    <div style="margin:0;padding:36px 16px;background-color:#F8F6F0;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif;color:#1E2022;">
                      <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="max-width:580px;margin:0 auto;background:#FFFFFF;border-radius:18px;overflow:hidden;border:1px solid #ECE7DE;box-shadow:0 12px 32px rgba(0,0,0,0.04);">
                        <tr>
                          <td style="background:linear-gradient(135deg,#1E2022 0%,#2C3038 100%);padding:36px 32px;text-align:center;">
                            <span style="font-size:26px;font-weight:800;color:#E2B261;letter-spacing:-0.5px;">Ptskart</span>
                            <h1 style="margin:12px 0 0;font-size:24px;color:#FFFFFF;font-weight:700;">Welcome to the Family!</h1>
                            <p style="margin:6px 0 0;font-size:14px;color:#C5CBD3;">Premium Goods, Thoughtful Design & Fast Delivery</p>
                          </td>
                        </tr>
                        <tr>
                          <td style="padding:36px 32px;">
                            <p style="margin:0 0 16px;font-size:16px;line-height:1.6;color:#1E2022;">
                              Hi <strong>{{customerName}}</strong>,
                            </p>
                            <p style="margin:0 0 24px;font-size:14px;line-height:1.6;color:#5B626E;">
                              Thank you for creating an account with <strong>Ptskart</strong>! Your profile (<strong>{{customerEmail}}</strong>) is now active and ready for an effortless shopping experience.
                            </p>

                            <div style="background:#FAF8F5;border-radius:14px;border:1px solid #ECE7DE;padding:20px;margin-bottom:28px;">
                              <div style="font-size:13px;font-weight:700;color:#D97706;text-transform:uppercase;letter-spacing:0.5px;margin-bottom:12px;">Your Member Privileges</div>
                              <div style="margin-bottom:10px;font-size:13px;color:#4A4F57;">✨ <strong>Complimentary Express Shipping</strong> on orders above $50.</div>
                              <div style="margin-bottom:10px;font-size:13px;color:#4A4F57;">🎁 <strong>Welcome Bonus:</strong> {{bonusPoints}} Rewards Points credited to your account.</div>
                              <div style="margin-bottom:10px;font-size:13px;color:#4A4F57;">🛡️ <strong>Fast One-Click Checkout</strong> with encrypted card & UPI storage.</div>
                              <div style="font-size:13px;color:#4A4F57;">📦 <strong>Live GPS Dispatch Tracking</strong> for every domestic & international order.</div>
                            </div>

                            <div style="text-align:center;margin:32px 0;">
                              <a href="{{loginUrl}}" style="display:inline-block;background:#E2B261;color:#1E2022;font-size:15px;font-weight:700;padding:14px 36px;border-radius:100px;text-decoration:none;box-shadow:0 6px 18px rgba(226,178,97,0.35);">
                                Start Shopping Now &rarr;
                              </a>
                            </div>

                            <p style="margin:0;font-size:12px;color:#8A92A0;text-align:center;line-height:1.5;">
                              Questions? Reach our concierge team at support@batohidriver.com anytime.
                            </p>
                          </td>
                        </tr>
                      </table>
                    </div>
                    """,
                    "Warm onboarding greeting sent immediately when a new customer registers.",
                    "{{customerName}}, {{customerEmail}}, {{storeName}}, {{loginUrl}}, {{bonusPoints}}"
            ));
        }

        // 3. Order Invoice & Tax Receipt
        if (!emailTemplateRepository.existsByTemplateKey("invoice")) {
            emailTemplateRepository.save(new EmailTemplate(
                    "invoice",
                    "Order Invoice & Receipt",
                    "Transactional",
                    "🧾 Order Confirmed & Tax Invoice: {{orderNumber}} - Ptskart",
                    """
                    <div style="margin:0;padding:36px 16px;background-color:#F8F6F0;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif;color:#1E2022;">
                      <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="max-width:620px;margin:0 auto;background:#FFFFFF;border-radius:18px;overflow:hidden;border:1px solid #ECE7DE;box-shadow:0 12px 32px rgba(0,0,0,0.04);">
                        <tr>
                          <td style="background:linear-gradient(135deg,#1E2022 0%,#2C3038 100%);padding:32px;color:#FFFFFF;">
                            <table width="100%" border="0" cellpadding="0" cellspacing="0">
                              <tr>
                                <td>
                                  <span style="font-size:24px;font-weight:800;color:#E2B261;letter-spacing:-0.5px;">Ptskart</span>
                                  <span style="display:block;font-size:12px;color:#A9B0BC;margin-top:2px;">Official Tax Invoice & Receipt</span>
                                </td>
                                <td style="text-align:right;">
                                  <span style="display:inline-block;padding:6px 14px;background:#2E7D32;color:#FFFFFF;font-size:11px;font-weight:800;border-radius:20px;text-transform:uppercase;letter-spacing:0.5px;">✓ PAID IN FULL</span>
                                </td>
                              </tr>
                            </table>
                          </td>
                        </tr>
                        <tr>
                          <td style="padding:32px;">
                            <div style="display:flex;justify-content:space-between;margin-bottom:24px;border-bottom:1px solid #ECE7DE;padding-bottom:16px;">
                              <div>
                                <div style="font-size:12px;color:#8A92A0;text-transform:uppercase;">Billed To:</div>
                                <div style="font-size:15px;font-weight:700;color:#1E2022;margin-top:2px;">{{customerName}}</div>
                                <div style="font-size:13px;color:#5B626E;">{{shippingAddress}}</div>
                              </div>
                              <div style="text-align:right;">
                                <div style="font-size:12px;color:#8A92A0;text-transform:uppercase;">Order ID:</div>
                                <div style="font-size:15px;font-weight:700;color:#1E2022;margin-top:2px;">{{orderNumber}}</div>
                                <div style="font-size:13px;color:#5B626E;">{{orderDate}}</div>
                              </div>
                            </div>

                            <div style="margin-bottom:20px;">
                              <div style="font-size:13px;font-weight:700;color:#1E2022;margin-bottom:10px;text-transform:uppercase;letter-spacing:0.5px;">Itemized Purchase Summary</div>
                              {{itemsTable}}
                            </div>

                            <table width="100%" cellpadding="6" style="margin-top:16px;border-top:1px solid #ECE7DE;padding-top:12px;">
                              <tr>
                                <td style="font-size:13px;color:#5B626E;">Subtotal:</td>
                                <td style="font-size:13px;font-weight:600;text-align:right;color:#1E2022;">${{subtotal}}</td>
                              </tr>
                              <tr>
                                <td style="font-size:13px;color:#5B626E;">Shipping & Handling:</td>
                                <td style="font-size:13px;font-weight:600;text-align:right;color:#1E2022;">{{shippingFee}}</td>
                              </tr>
                              <tr style="border-top:2px solid #1E2022;">
                                <td style="font-size:16px;font-weight:800;color:#1E2022;padding-top:12px;">Total Paid:</td>
                                <td style="font-size:20px;font-weight:800;text-align:right;color:#D97706;padding-top:12px;">${{totalAmount}}</td>
                              </tr>
                            </table>

                            <div style="margin-top:28px;background:#FAF8F5;border-radius:12px;border:1px solid #ECE7DE;padding:16px;">
                              <table width="100%" cellpadding="4">
                                <tr><td style="font-size:12px;color:#8A92A0;">Payment Rail:</td><td style="font-size:13px;font-weight:700;color:#1E2022;">{{paymentMethod}}</td></tr>
                                <tr><td style="font-size:12px;color:#8A92A0;">Carrier & Tracking:</td><td style="font-size:13px;font-weight:700;color:#1E2022;">{{carrier}} &bull; {{trackingNumber}}</td></tr>
                              </table>
                            </div>

                            <div style="text-align:center;margin:30px 0 10px;">
                              <a href="{{trackOrderUrl}}" style="display:inline-block;background:#1E2022;color:#FFFFFF;font-size:14px;font-weight:700;padding:12px 32px;border-radius:100px;text-decoration:none;">
                                Track Live Package &rarr;
                              </a>
                            </div>

                            <p style="margin:20px 0 0;font-size:12px;color:#8A92A0;text-align:center;">
                              Thank you for supporting Ptskart. A PDF copy is saved in your customer account.
                            </p>
                          </td>
                        </tr>
                      </table>
                    </div>
                    """,
                    "Official purchase confirmation and tax invoice dispatched right after completing checkout.",
                    "{{customerName}}, {{orderNumber}}, {{orderDate}}, {{shippingAddress}}, {{paymentMethod}}, {{carrier}}, {{trackingNumber}}, {{itemsTable}}, {{subtotal}}, {{shippingFee}}, {{totalAmount}}, {{trackOrderUrl}}"
            ));
        }

        // 4. New Advertisement / Product Showcase
        if (!emailTemplateRepository.existsByTemplateKey("advertisement")) {
            emailTemplateRepository.save(new EmailTemplate(
                    "advertisement",
                    "New Advertisement & Product Showcase",
                    "Marketing",
                    "✨ New Drop Alert: {{headline}} is finally here at Ptskart!",
                    """
                    <div style="margin:0;padding:36px 16px;background-color:#F8F6F0;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif;color:#1E2022;">
                      <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="max-width:600px;margin:0 auto;background:#FFFFFF;border-radius:18px;overflow:hidden;border:1px solid #ECE7DE;box-shadow:0 12px 32px rgba(0,0,0,0.04);">
                        <tr>
                          <td style="background:#1E2022;padding:32px;text-align:center;">
                            <span style="font-size:24px;font-weight:800;color:#E2B261;letter-spacing:-0.5px;">Ptskart Studio</span>
                            <div style="display:inline-block;margin-top:10px;padding:4px 12px;background:rgba(226,178,97,0.15);color:#E2B261;border-radius:100px;font-size:11px;font-weight:700;text-transform:uppercase;letter-spacing:1px;">New Seasonal Release</div>
                            <h1 style="margin:14px 0 0;font-size:24px;color:#FFFFFF;font-weight:700;line-height:1.3;">{{headline}}</h1>
                          </td>
                        </tr>
                        <tr>
                          <td style="padding:32px;">
                            <p style="margin:0 0 16px;font-size:15px;color:#1E2022;line-height:1.6;">
                              Hi {{customerName}},
                            </p>
                            <p style="margin:0 0 24px;font-size:14px;color:#5B626E;line-height:1.6;">
                              Our newest curation of artisan essentials, premium organic cotton apparel, and precision lifestyle goods has officially launched. Each piece is crafted for timeless longevity and everyday comfort.
                            </p>

                            <div style="background:#FAF8F5;border-radius:14px;border:1px solid #ECE7DE;padding:20px;margin-bottom:28px;">
                              <div style="font-size:14px;font-weight:700;color:#1E2022;margin-bottom:8px;">🔥 Special Launch Offer</div>
                              <p style="margin:0;font-size:13px;color:#5B626E;line-height:1.5;">
                                {{discountNotice}}
                              </p>
                            </div>

                            <div style="text-align:center;margin:32px 0;">
                              <a href="{{shopUrl}}" style="display:inline-block;background:#E2B261;color:#1E2022;font-size:15px;font-weight:700;padding:14px 38px;border-radius:100px;text-decoration:none;box-shadow:0 6px 18px rgba(226,178,97,0.35);">
                                Discover The New Collection &rarr;
                              </a>
                            </div>

                            <p style="margin:0;font-size:12px;color:#8A92A0;text-align:center;">
                              You received this because you are an active member of Ptskart.
                            </p>
                          </td>
                        </tr>
                      </table>
                    </div>
                    """,
                    "Marketing announcement showcasing new collections, featured items, and seasonal promotions.",
                    "{{customerName}}, {{headline}}, {{discountNotice}}, {{shopUrl}}"
            ));
        }

        // 5. Coupon & Promo Voucher
        if (!emailTemplateRepository.existsByTemplateKey("coupon")) {
            emailTemplateRepository.save(new EmailTemplate(
                    "coupon",
                    "Discount Coupon & Voucher",
                    "Promotions",
                    "🏷️ Special Gift: Use coupon {{couponCode}} for {{discountAmount}} off your next order!",
                    """
                    <div style="margin:0;padding:36px 16px;background-color:#F8F6F0;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif;color:#1E2022;">
                      <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="max-width:560px;margin:0 auto;background:#FFFFFF;border-radius:18px;overflow:hidden;border:1px solid #ECE7DE;box-shadow:0 12px 32px rgba(0,0,0,0.04);">
                        <tr>
                          <td style="background:linear-gradient(135deg,#D97706 0%,#B45309 100%);padding:36px 32px;text-align:center;color:#FFFFFF;">
                            <span style="font-size:24px;font-weight:800;letter-spacing:-0.5px;">Ptskart Exclusive</span>
                            <h1 style="margin:8px 0 0;font-size:26px;font-weight:800;">A Special Treat For You!</h1>
                            <p style="margin:6px 0 0;font-size:14px;color:#FEF3C7;">Save {{discountAmount}} on your favorite pieces</p>
                          </td>
                        </tr>
                        <tr>
                          <td style="padding:36px 32px;text-align:center;">
                            <p style="margin:0 0 20px;font-size:14px;color:#5B626E;line-height:1.6;">
                              Hi <strong>{{customerName}}</strong>, to celebrate your membership with us, here is an exclusive single-use voucher for your next checkout:
                            </p>

                            <div style="margin:20px auto;padding:24px;background:#FFFDF9;border:2px dashed #D97706;border-radius:16px;max-width:400px;">
                              <div style="font-size:11px;font-weight:800;color:#D97706;text-transform:uppercase;letter-spacing:1px;margin-bottom:6px;">Your Promo Code</div>
                              <div style="font-family:'Courier New',Courier,monospace;font-size:28px;font-weight:800;color:#1E2022;letter-spacing:4px;">{{couponCode}}</div>
                              <div style="font-size:14px;font-weight:700;color:#B45309;margin-top:10px;">{{discountAmount}} OFF (Min. spend: {{minSpend}})</div>
                              <div style="font-size:12px;color:#8A92A0;margin-top:4px;">Expires on {{expiryDate}}</div>
                            </div>

                            <div style="text-align:center;margin:30px 0 10px;">
                              <a href="{{shopUrl}}" style="display:inline-block;background:#1E2022;color:#FFFFFF;font-size:14px;font-weight:700;padding:14px 36px;border-radius:100px;text-decoration:none;">
                                Apply Coupon & Shop &rarr;
                              </a>
                            </div>

                            <p style="margin:16px 0 0;font-size:12px;color:#8A92A0;">
                              Simply paste this code at checkout to automatically apply your savings.
                            </p>
                          </td>
                        </tr>
                      </table>
                    </div>
                    """,
                    "Voucher card featuring a discount coupon code with expiration details and min spend terms.",
                    "{{customerName}}, {{couponCode}}, {{discountAmount}}, {{minSpend}}, {{expiryDate}}, {{shopUrl}}"
            ));
        }

        // 6. Celebration & Festive Greetings
        if (!emailTemplateRepository.existsByTemplateKey("celebration")) {
            emailTemplateRepository.save(new EmailTemplate(
                    "celebration",
                    "Celebration & Festive Greetings",
                    "Seasonal",
                    "🎊 Festive Celebrations from Ptskart! Special {{festivalName}} Wishes",
                    """
                    <div style="margin:0;padding:36px 16px;background-color:#F8F6F0;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif;color:#1E2022;">
                      <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="max-width:580px;margin:0 auto;background:#FFFFFF;border-radius:18px;overflow:hidden;border:1px solid #ECE7DE;box-shadow:0 12px 32px rgba(0,0,0,0.04);">
                        <tr>
                          <td style="background:linear-gradient(135deg,#9333EA 0%,#4F46E5 100%);padding:40px 32px;text-align:center;color:#FFFFFF;">
                            <div style="font-size:36px;margin-bottom:8px;">✨ 🎊 🎁</div>
                            <span style="font-size:22px;font-weight:800;letter-spacing:-0.5px;color:#FDE047;">Happy {{festivalName}}!</span>
                            <h1 style="margin:10px 0 0;font-size:24px;font-weight:700;">Warm Wishes to You & Your Loved Ones</h1>
                          </td>
                        </tr>
                        <tr>
                          <td style="padding:36px 32px;">
                            <p style="margin:0 0 16px;font-size:16px;color:#1E2022;line-height:1.6;">
                              Dearest <strong>{{customerName}}</strong>,
                            </p>
                            <p style="margin:0 0 24px;font-size:14px;color:#5B626E;line-height:1.7;">
                              {{festiveMessage}}
                            </p>

                            <div style="background:#FAF5FF;border-radius:14px;border:1px solid #E9D5FF;padding:24px;text-align:center;margin-bottom:28px;">
                              <div style="font-size:12px;font-weight:800;color:#9333EA;text-transform:uppercase;letter-spacing:1px;margin-bottom:6px;">Our Festive Gift To You</div>
                              <div style="font-size:18px;font-weight:800;color:#1E2022;margin-bottom:8px;">Complimentary Festive Voucher</div>
                              <div style="font-family:'Courier New',Courier,monospace;display:inline-block;background:#FFFFFF;padding:8px 20px;border-radius:8px;font-size:20px;font-weight:800;color:#9333EA;border:1px solid #D8B4FE;">
                                {{giftCoupon}}
                              </div>
                              <p style="margin:10px 0 0;font-size:12px;color:#6B7280;">Use this coupon code on any purchase throughout the festive week.</p>
                            </div>

                            <div style="text-align:center;margin:28px 0 10px;">
                              <a href="{{shopUrl}}" style="display:inline-block;background:#9333EA;color:#FFFFFF;font-size:14px;font-weight:700;padding:14px 36px;border-radius:100px;text-decoration:none;box-shadow:0 6px 18px rgba(147,51,234,0.35);">
                                Celebrate & Explore Gifts &rarr;
                              </a>
                            </div>

                            <p style="margin:20px 0 0;font-size:12px;color:#8A92A0;text-align:center;">
                              From everyone in the Ptskart Studio family, thank you for being with us!
                            </p>
                          </td>
                        </tr>
                      </table>
                    </div>
                    """,
                    "Festive holiday / birthday / festival celebration greeting with promotional festive gift voucher.",
                    "{{customerName}}, {{festivalName}}, {{festiveMessage}}, {{giftCoupon}}, {{shopUrl}}"
            ));
        }
    }

    // -------------------------------------------------------------
    // TEMPLATE RENDERING & DYNAMIC VARIABLES
    // -------------------------------------------------------------
    public String renderString(String template, Map<String, Object> variables) {
        if (template == null) return "";
        if (variables == null || variables.isEmpty()) return template;

        String rendered = template;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            rendered = rendered.replace(placeholder, value);
        }
        return rendered;
    }

    public boolean sendTemplateEmail(String toEmail, String templateKey, Map<String, Object> variables) {
        EmailTemplate template = emailTemplateRepository.findByTemplateKey(templateKey)
                .orElse(null);

        if (template == null) {
            System.err.println("[DynamicEmailService] Template not found: " + templateKey);
            return false;
        }

        if (!Boolean.TRUE.equals(template.getActive())) {
            System.out.println("[DynamicEmailService] Template disabled: " + templateKey);
            return false;
        }

        String renderedSubject = renderString(template.getSubject(), variables);
        String renderedBody = renderString(template.getBodyHtml(), variables);

        EmailSendRequest req = new EmailSendRequest(toEmail, renderedSubject, renderedBody);
        req.setHtml(true);
        return sendEmail(req);
    }

    // -------------------------------------------------------------
    // AUTOMATIC HIGH-LEVEL HOOKS
    // -------------------------------------------------------------
    public boolean sendOtpEmail(String toEmail, String customerName, String otpCode, int expiryMinutes) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("customerName", customerName != null && !customerName.isBlank() ? customerName : "Valued Customer");
        vars.put("otpCode", otpCode);
        vars.put("expiryMinutes", String.valueOf(expiryMinutes));
        vars.put("actionName", "secure account access");

        return sendTemplateEmail(toEmail, "otp", vars);
    }

    public boolean sendRegistrationEmail(String toEmail, String customerName) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("customerName", customerName != null && !customerName.isBlank() ? customerName : "Shopper");
        vars.put("customerEmail", toEmail);
        vars.put("storeName", "Ptskart Commerce");
        vars.put("loginUrl", "http://localhost:5173/");
        vars.put("bonusPoints", "250");

        return sendTemplateEmail(toEmail, "registration", vars);
    }

    public boolean sendInvoiceEmail(String toEmail, Order order) {
        if (order == null || toEmail == null || toEmail.isBlank()) return false;

        StringBuilder itemsHtml = new StringBuilder();
        itemsHtml.append("<table width='100%' cellpadding='8' style='border-collapse:collapse;border:1px solid #ECE7DE;border-radius:8px;'>");
        itemsHtml.append("<tr style='background:#FAF8F5;text-align:left;font-size:12px;color:#8A92A0;'>")
                 .append("<th style='padding:10px;'>Item</th>")
                 .append("<th style='padding:10px;text-align:center;'>Qty</th>")
                 .append("<th style='padding:10px;text-align:right;'>Price</th>")
                 .append("<th style='padding:10px;text-align:right;'>Total</th>")
                 .append("</tr>");

        if (order.getItems() != null && !order.getItems().isEmpty()) {
            for (OrderItem it : order.getItems()) {
                double totalLine = (it.getUnitPrice() != null ? it.getUnitPrice() : 0.0) * (it.getQuantity() != null ? it.getQuantity() : 1);
                itemsHtml.append("<tr style='border-top:1px solid #ECE7DE;font-size:13px;color:#1E2022;'>")
                         .append("<td style='padding:10px;'><strong>").append(it.getProductName() != null ? it.getProductName() : "Product").append("</strong>")
                         .append(it.getVariant() != null ? "<br><small style='color:#8A92A0;'>" + it.getVariant() + "</small>" : "")
                         .append("</td>")
                         .append("<td style='padding:10px;text-align:center;'>").append(it.getQuantity() != null ? it.getQuantity() : 1).append("</td>")
                         .append("<td style='padding:10px;text-align:right;'>$").append(String.format("%.2f", it.getUnitPrice() != null ? it.getUnitPrice() : 0.0)).append("</td>")
                         .append("<td style='padding:10px;text-align:right;font-weight:700;'>$").append(String.format("%.2f", totalLine)).append("</td>")
                         .append("</tr>");
            }
        } else {
            itemsHtml.append("<tr><td colspan='4' style='padding:10px;font-size:13px;color:#5B626E;'>Order summary items</td></tr>");
        }
        itemsHtml.append("</table>");

        Map<String, Object> vars = new HashMap<>();
        vars.put("customerName", order.getCustomerName() != null ? order.getCustomerName() : "Customer");
        vars.put("orderNumber", order.getOrderNumber() != null ? order.getOrderNumber() : "#VX10001");
        vars.put("orderDate", order.getCreatedAt() != null ? order.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : "Today");
        vars.put("shippingAddress", order.getShippingAddress() != null ? order.getShippingAddress() : "Standard Delivery Address");
        vars.put("paymentMethod", order.getPaymentMethod() != null ? order.getPaymentMethod() : "Credit Card / UPI");
        vars.put("carrier", order.getCarrier() != null ? order.getCarrier() : "DHL Express");
        vars.put("trackingNumber", order.getTrackingNumber() != null ? order.getTrackingNumber() : "TRK-EXP-9901");
        vars.put("itemsTable", itemsHtml.toString());
        vars.put("subtotal", String.format("%.2f", order.getSubtotal() != null ? order.getSubtotal() : order.getTotalAmount()));
        vars.put("shippingFee", (order.getShippingFee() != null && order.getShippingFee() > 0) ? "$" + String.format("%.2f", order.getShippingFee()) : "FREE");
        vars.put("totalAmount", String.format("%.2f", order.getTotalAmount() != null ? order.getTotalAmount() : 0.0));
        vars.put("trackOrderUrl", "http://localhost:5173/");

        return sendTemplateEmail(toEmail, "invoice", vars);
    }
}
