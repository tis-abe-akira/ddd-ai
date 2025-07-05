package com.example.syndicatelending.syndicate.dto;

import com.example.syndicatelending.common.statemachine.syndicate.SyndicateState;
import java.time.LocalDateTime;
import java.util.List;

/**
 * シンジケート詳細レスポンスDTO
 * 
 * 関連エンティティ（Borrower、Investor）の詳細情報を含む
 * UIでの表示用に最適化されたレスポンス形式
 */
public class SyndicateDetailResponseDTO {
    
    private Long id;
    private String name;
    
    // Status情報
    private SyndicateState status;
    
    // Borrower詳細情報
    private Long borrowerId;
    private String borrowerName;
    
    // Lead Bank詳細情報
    private Long leadBankId;
    private String leadBankName;
    
    // Member詳細情報
    private List<Long> memberInvestorIds;
    private List<String> memberInvestorNames;
    
    // メタデータ
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;
    
    /**
     * メンバー投資家の詳細情報を表現する内部クラス
     */
    public static class MemberInvestorInfo {
        private Long id;
        private String name;
        private String investorType;
        
        public MemberInvestorInfo() {}
        
        public MemberInvestorInfo(Long id, String name, String investorType) {
            this.id = id;
            this.name = name;
            this.investorType = investorType;
        }
        
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getInvestorType() { return investorType; }
        public void setInvestorType(String investorType) { this.investorType = investorType; }
    }
    
    // Constructors
    public SyndicateDetailResponseDTO() {}
    
    public SyndicateDetailResponseDTO(Long id, String name, SyndicateState status, Long borrowerId, String borrowerName,
                                      Long leadBankId, String leadBankName, List<Long> memberInvestorIds,
                                      List<String> memberInvestorNames, LocalDateTime createdAt,
                                      LocalDateTime updatedAt, Long version) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.borrowerId = borrowerId;
        this.borrowerName = borrowerName;
        this.leadBankId = leadBankId;
        this.leadBankName = leadBankName;
        this.memberInvestorIds = memberInvestorIds;
        this.memberInvestorNames = memberInvestorNames;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.version = version;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public SyndicateState getStatus() { return status; }
    public void setStatus(SyndicateState status) { this.status = status; }
    
    public Long getBorrowerId() { return borrowerId; }
    public void setBorrowerId(Long borrowerId) { this.borrowerId = borrowerId; }
    
    public String getBorrowerName() { return borrowerName; }
    public void setBorrowerName(String borrowerName) { this.borrowerName = borrowerName; }
    
    public Long getLeadBankId() { return leadBankId; }
    public void setLeadBankId(Long leadBankId) { this.leadBankId = leadBankId; }
    
    public String getLeadBankName() { return leadBankName; }
    public void setLeadBankName(String leadBankName) { this.leadBankName = leadBankName; }
    
    public List<Long> getMemberInvestorIds() { return memberInvestorIds; }
    public void setMemberInvestorIds(List<Long> memberInvestorIds) { this.memberInvestorIds = memberInvestorIds; }
    
    public List<String> getMemberInvestorNames() { return memberInvestorNames; }
    public void setMemberInvestorNames(List<String> memberInvestorNames) { this.memberInvestorNames = memberInvestorNames; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}