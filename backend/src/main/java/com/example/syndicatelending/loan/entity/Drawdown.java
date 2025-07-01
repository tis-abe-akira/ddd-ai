package com.example.syndicatelending.loan.entity;

import com.example.syndicatelending.transaction.entity.Transaction;
import com.example.syndicatelending.transaction.entity.TransactionType;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "drawdown")
public class Drawdown extends Transaction {

    @Column(nullable = false)
    private Long loanId;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private String purpose;

    @JsonManagedReference
    @OneToMany(mappedBy = "drawdown", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AmountPie> amountPies;

    public Drawdown() {
        super();
        this.setTransactionType(TransactionType.DRAWDOWN);
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

    public List<AmountPie> getAmountPies() {
        return amountPies;
    }

    public void setAmountPies(List<AmountPie> amountPies) {
        this.amountPies = amountPies;
    }
}
