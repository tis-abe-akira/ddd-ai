package com.example.syndicatelending.party.dto;

import com.example.syndicatelending.party.entity.Industry;
import com.example.syndicatelending.party.entity.Country;
import jakarta.validation.constraints.NotBlank;

/**
 * 企業作成リクエストDTO。
 */
public class CreateCompanyRequest {

    @NotBlank(message = "Company name is required")
    private String companyName;

    private String registrationNumber;
    private Industry industry;
    private String address;
    private Country country;

    public CreateCompanyRequest() {
    }

    public CreateCompanyRequest(String companyName, String registrationNumber,
            Industry industry, String address, Country country) {
        this.companyName = companyName;
        this.registrationNumber = registrationNumber;
        this.industry = industry;
        this.address = address;
        this.country = country;
    }

    // Getters and Setters
    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public Industry getIndustry() {
        return industry;
    }

    public void setIndustry(Industry industry) {
        this.industry = industry;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }
}
