package com.example.syndicatelending.party.dto;

import com.example.syndicatelending.party.entity.Country;
import com.example.syndicatelending.party.entity.Industry;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 企業更新リクエストDTO。
 */
public class UpdateCompanyRequest {
    @NotBlank(message = "Company name is required")
    private final String companyName;

    private final String registrationNumber;

    private final Industry industry;

    private final String address;

    private final Country country;

    @NotNull(message = "Version is required for optimistic locking")
    private final Long version;

    @JsonCreator
    public UpdateCompanyRequest(
            @JsonProperty("companyName") String companyName,
            @JsonProperty("registrationNumber") String registrationNumber,
            @JsonProperty("industry") Industry industry,
            @JsonProperty("address") String address,
            @JsonProperty("country") Country country,
            @JsonProperty("version") Long version) {
        this.companyName = companyName;
        this.registrationNumber = registrationNumber;
        this.industry = industry;
        this.address = address;
        this.country = country;
        this.version = version;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public Industry getIndustry() {
        return industry;
    }

    public String getAddress() {
        return address;
    }

    public Country getCountry() {
        return country;
    }

    public Long getVersion() {
        return version;
    }
}
