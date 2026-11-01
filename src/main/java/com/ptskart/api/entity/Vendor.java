package com.ptskart.api.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "vendors")
public class Vendor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String storeName;

    @Column(nullable = false)
    private String ownerName;

    @Column(nullable = false)
    private String email;

    private String phone;

    private Double commissionRate = 12.5; // percentage

    private Double rating = 4.9;

    private Integer totalProducts = 24;

    private Double totalSales = 34820.0;

    private String status = "Active"; // Active, Pending, Suspended

    public Vendor() {}

    public Vendor(String storeName, String ownerName, String email, String phone, Double commissionRate, Double rating, String status) {
        this.storeName = storeName;
        this.ownerName = ownerName;
        this.email = email;
        this.phone = phone;
        this.commissionRate = commissionRate;
        this.rating = rating;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Double getCommissionRate() { return commissionRate; }
    public void setCommissionRate(Double commissionRate) { this.commissionRate = commissionRate; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public Integer getTotalProducts() { return totalProducts; }
    public void setTotalProducts(Integer totalProducts) { this.totalProducts = totalProducts; }

    public Double getTotalSales() { return totalSales; }
    public void setTotalSales(Double totalSales) { this.totalSales = totalSales; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

