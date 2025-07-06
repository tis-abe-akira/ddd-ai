package com.example.syndicatelending.loan.entity;

/**
 * 支払い状態の列挙型
 */
public enum PaymentStatus {
    /** 未払い（支払い期限前または期限到来） */
    PENDING("Pending"),
    
    /** 支払い済み */
    PAID("Paid"),
    
    /** 延滞（支払い期限を過ぎても未払い） */
    OVERDUE("Overdue");
    
    private final String displayName;
    
    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * 支払い可能な状態かどうかを判定
     * @return 支払い可能な場合true
     */
    public boolean isPayable() {
        return this == PENDING || this == OVERDUE;
    }
    
    /**
     * 支払い済みかどうかを判定
     * @return 支払い済みの場合true
     */
    public boolean isPaid() {
        return this == PAID;
    }
}