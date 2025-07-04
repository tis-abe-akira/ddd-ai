package com.example.syndicatelending.party.dto;

/**
 * エンティティの更新可能性を表すレスポンスDTO
 * 
 * Facility組成状況に基づく各フィールドの変更可否を提供する。
 * フロントエンドでのUI制御に使用される。
 */
public class CanUpdateResponse {
    
    /** 全体として更新可能かどうか */
    private boolean canUpdate;
    
    /** 制約がある場合の理由 */
    private String reason;
    
    /** フィールド別の更新可否マップ */
    private FieldUpdateability fields;
    
    /**
     * 制約がない場合のコンストラクタ
     */
    public CanUpdateResponse(boolean canUpdate) {
        this.canUpdate = canUpdate;
        this.reason = canUpdate ? null : "不明な制約があります";
    }
    
    /**
     * 制約がある場合のコンストラクタ
     */
    public CanUpdateResponse(boolean canUpdate, String reason, FieldUpdateability fields) {
        this.canUpdate = canUpdate;
        this.reason = reason;
        this.fields = fields;
    }
    
    // Getters and Setters
    public boolean isCanUpdate() {
        return canUpdate;
    }
    
    public void setCanUpdate(boolean canUpdate) {
        this.canUpdate = canUpdate;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public FieldUpdateability getFields() {
        return fields;
    }
    
    public void setFields(FieldUpdateability fields) {
        this.fields = fields;
    }
    
    /**
     * フィールド別更新可否の基底クラス
     */
    public static abstract class FieldUpdateability {
        // 共通フィールド（全エンティティで変更可能）
        private boolean name = true;
        private boolean email = true;
        private boolean phoneNumber = true;
        
        public boolean isName() { return name; }
        public void setName(boolean name) { this.name = name; }
        
        public boolean isEmail() { return email; }
        public void setEmail(boolean email) { this.email = email; }
        
        public boolean isPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(boolean phoneNumber) { this.phoneNumber = phoneNumber; }
    }
    
    /**
     * Borrower用フィールド更新可否
     */
    public static class BorrowerFieldUpdateability extends FieldUpdateability {
        private boolean companyId;
        private boolean creditRating;
        private boolean creditLimit;
        
        public BorrowerFieldUpdateability(boolean companyId, boolean creditRating, boolean creditLimit) {
            this.companyId = companyId;
            this.creditRating = creditRating;
            this.creditLimit = creditLimit;
        }
        
        public boolean isCompanyId() { return companyId; }
        public void setCompanyId(boolean companyId) { this.companyId = companyId; }
        
        public boolean isCreditRating() { return creditRating; }
        public void setCreditRating(boolean creditRating) { this.creditRating = creditRating; }
        
        public boolean isCreditLimit() { return creditLimit; }
        public void setCreditLimit(boolean creditLimit) { this.creditLimit = creditLimit; }
    }
    
    /**
     * Investor用フィールド更新可否
     */
    public static class InvestorFieldUpdateability extends FieldUpdateability {
        private boolean companyId;
        private boolean investmentCapacity;
        private boolean investorType = true; // 常に変更可能
        
        public InvestorFieldUpdateability(boolean companyId, boolean investmentCapacity) {
            this.companyId = companyId;
            this.investmentCapacity = investmentCapacity;
        }
        
        public boolean isCompanyId() { return companyId; }
        public void setCompanyId(boolean companyId) { this.companyId = companyId; }
        
        public boolean isInvestmentCapacity() { return investmentCapacity; }
        public void setInvestmentCapacity(boolean investmentCapacity) { this.investmentCapacity = investmentCapacity; }
        
        public boolean isInvestorType() { return investorType; }
        public void setInvestorType(boolean investorType) { this.investorType = investorType; }
    }
}