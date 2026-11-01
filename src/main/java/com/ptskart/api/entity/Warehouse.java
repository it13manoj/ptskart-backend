package com.ptskart.api.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "warehouses")
public class Warehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String code; // e.g. WH-LON-01

    private String location; // e.g. London Logistics Center, UK

    private Integer capacity = 50000;

    private Integer utilizationPercent = 68;

    private String managerName;

    private String contactPhone;

    private Boolean active = true;

    public Warehouse() {}

    public Warehouse(String name, String code, String location, Integer capacity, Integer utilizationPercent, String managerName, String contactPhone) {
        this.name = name;
        this.code = code;
        this.location = location;
        this.capacity = capacity;
        this.utilizationPercent = utilizationPercent;
        this.managerName = managerName;
        this.contactPhone = contactPhone;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public Integer getUtilizationPercent() { return utilizationPercent; }
    public void setUtilizationPercent(Integer utilizationPercent) { this.utilizationPercent = utilizationPercent; }

    public String getManagerName() { return managerName; }
    public void setManagerName(String managerName) { this.managerName = managerName; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}

