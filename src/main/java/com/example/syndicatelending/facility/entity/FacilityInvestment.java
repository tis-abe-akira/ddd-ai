package com.example.syndicatelending.facility.entity;

import com.example.syndicatelending.transaction.entity.Transaction;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "facility_investment")
public class FacilityInvestment extends Transaction {

    @Column(nullable = false)
    private Long investorId;

    public Long getInvestorId() {
        return investorId;
    }

    public void setInvestorId(Long investorId) {
        this.investorId = investorId;
    }
}
