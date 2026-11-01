package com.ptskart.api.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "store_settings")
public class StoreSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String settingKey;

    @Column(columnDefinition = "TEXT")
    private String settingValue;

    private String settingGroup; // GENERAL, BRANDING, SEO, CHECKOUT, TAX, SHIPPING

    private String description;

    public StoreSetting() {}

    public StoreSetting(String settingKey, String settingValue, String settingGroup, String description) {
        this.settingKey = settingKey;
        this.settingValue = settingValue;
        this.settingGroup = settingGroup;
        this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSettingKey() { return settingKey; }
    public void setSettingKey(String settingKey) { this.settingKey = settingKey; }

    public String getSettingValue() { return settingValue; }
    public void setSettingValue(String settingValue) { this.settingValue = settingValue; }

    public String getSettingGroup() { return settingGroup; }
    public void setSettingGroup(String settingGroup) { this.settingGroup = settingGroup; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}

