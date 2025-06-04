package com.example.syndicatelending.loan.entity;

import com.example.syndicatelending.transaction.entity.Transaction;
import jakarta.persistence.*;

@Entity
@Table(name = "drawdown")
public class Drawdown extends Transaction {

    @Column(nullable = false)
    private Long loanId;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private String purpose;

    public Drawdown() {
        super();
        this.setTransactionType("DRAWDOWN");
    }

    public Long getLoanId() {
        return loanId;
    }

    public void setLoanId(Long loanId) {
        this.loanId = loanId;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }
}
