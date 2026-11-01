package com.ptskart.api.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "email_config")
public class EmailConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String providerKey = "brevo"; // brevo, gmail, sendgrid, custom

    @Column(nullable = false, length = 100)
    private String name = "Brevo (Sendinblue)";

    @Column(nullable = false)
    private String host = "smtp-relay.brevo.com";

    @Column(nullable = false)
    private Integer port = 587;

    private String username;

    private String password;

    private String fromEmail;

    private String fromName = "Ptskart Commerce";

    private Boolean smtpAuth = true;

    private Boolean startTls = true;

    private Boolean sslEnable = false;

    private Boolean active = true;

    private LocalDateTime updatedAt = LocalDateTime.now();

    public EmailConfig() {}

    public EmailConfig(String providerKey, String name, String host, Integer port,
                       String username, String password, String fromEmail, String fromName,
                       Boolean active) {
        this.providerKey = providerKey;
        this.name = name;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.fromEmail = fromEmail;
        this.fromName = fromName;
        this.active = active;
        this.smtpAuth = true;
        this.startTls = true;
        this.sslEnable = false;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getProviderKey() { return providerKey; }
    public void setProviderKey(String providerKey) { this.providerKey = providerKey; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    public Integer getPort() { return port; }
    public void setPort(Integer port) { this.port = port; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFromEmail() { return fromEmail; }
    public void setFromEmail(String fromEmail) { this.fromEmail = fromEmail; }

    public String getFromName() { return fromName; }
    public void setFromName(String fromName) { this.fromName = fromName; }

    public Boolean getSmtpAuth() { return smtpAuth; }
    public void setSmtpAuth(Boolean smtpAuth) { this.smtpAuth = smtpAuth; }

    public Boolean getStartTls() { return startTls; }
    public void setStartTls(Boolean startTls) { this.startTls = startTls; }

    public Boolean getSslEnable() { return sslEnable; }
    public void setSslEnable(Boolean sslEnable) { this.sslEnable = sslEnable; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
