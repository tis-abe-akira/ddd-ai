package com.example.syndicatelending.party.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 企業作成リクエストDTO。
 */
public class CreateCompanyRequest {

    @NotBlank(message = "Company name is required")
    private String companyName;

    private String registrationNumber;
    private String industry;
    private String address;
    private String country;

    public CreateCompanyRequest() {}

    public CreateCompanyRequest(String companyName, String registrationNumber, 
                               String industry, String address, String country) {
        this.companyName = companyName;
        this.registrationNumber = registrationNumber;
        this.industry = industry;
        this.address = address;
        this.country = country;
    }

    // Getters and Setters
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }

    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
}