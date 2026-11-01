package com.ptskart.api.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "email_templates")
public class EmailTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String templateKey; // otp, registration, invoice, advertisement, coupon, celebration

    @Column(nullable = false)
    private String name;

    private String category; // Security, Onboarding, Transactional, Marketing, Promotions, Seasonal

    @Column(nullable = false, length = 500)
    private String subject;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String bodyHtml;

    @Column(length = 500)
    private String description;

    @Column(length = 500)
    private String variables; // Comma-separated variable names

    private Boolean active = true;

    private LocalDateTime updatedAt = LocalDateTime.now();

    public EmailTemplate() {}

    public EmailTemplate(String templateKey, String name, String category, String subject, String bodyHtml, String description, String variables) {
        this.templateKey = templateKey;
        this.name = name;
        this.category = category;
        this.subject = subject;
        this.bodyHtml = bodyHtml;
        this.description = description;
        this.variables = variables;
        this.active = true;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTemplateKey() { return templateKey; }
    public void setTemplateKey(String templateKey) { this.templateKey = templateKey; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getBodyHtml() { return bodyHtml; }
    public void setBodyHtml(String bodyHtml) { this.bodyHtml = bodyHtml; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getVariables() { return variables; }
    public void setVariables(String variables) { this.variables = variables; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

