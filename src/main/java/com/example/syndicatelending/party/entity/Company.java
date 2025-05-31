package com.example.syndicatelending.party.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 企業エンティティ（JPA Entity兼ドメインエンティティ）。
 */
@Entity
@Table(name = "companies")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "business_id", unique = true, nullable = false)
    private String businessId;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "registration_number")
    private String registrationNumber;

    @Column(name = "industry")
    private String industry;

    @Column(name = "address")
    private String address;

    @Column(name = "country")
    private String country;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Company() {
    }

    public Company(String companyName, String registrationNumber, String industry, String address, String country) {
        this.businessId = UUID.randomUUID().toString();
        this.companyName = companyName;
        this.registrationNumber = registrationNumber;
        this.industry = industry;
        this.address = address;
        this.country = country;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBusinessId() { return businessId; }
    public void setBusinessId(String businessId) { this.businessId = businessId; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { 
        this.companyName = companyName;
        this.updatedAt = LocalDateTime.now();
    }

    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { 
        this.registrationNumber = registrationNumber;
        this.updatedAt = LocalDateTime.now();
    }

    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { 
        this.industry = industry;
        this.updatedAt = LocalDateTime.now();
    }

    public String getAddress() { return address; }
    public void setAddress(String address) { 
        this.address = address;
        this.updatedAt = LocalDateTime.now();
    }

    public String getCountry() { return country; }
    public void setCountry(String country) { 
        this.country = country;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}