package com.example.syndicatelending.syndicate.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "syndicates")
public class Syndicate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    // シンジケート団のリードバンク（InvestorのIDで管理）
    @Column(name = "lead_bank_id")
    private Long leadBankId;

    // メンバー（投資家IDのリスト、シンプルな形で実装）
    @ElementCollection
    @CollectionTable(name = "syndicate_members", joinColumns = @JoinColumn(name = "syndicate_id"))
    @Column(name = "investor_id")
    private List<Long> memberInvestorIds = new ArrayList<>();

    // 作成日時・更新日時
    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private java.time.LocalDateTime updatedAt;

    public Syndicate() {
    }

    public Syndicate(String name, Long leadBankId, List<Long> memberInvestorIds) {
        this.name = name;
        this.leadBankId = leadBankId;
        if (memberInvestorIds != null) {
            this.memberInvestorIds = memberInvestorIds;
        }
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = this.updatedAt = java.time.LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = java.time.LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getLeadBankId() {
        return leadBankId;
    }

    public void setLeadBankId(Long leadBankId) {
        this.leadBankId = leadBankId;
    }

    public List<Long> getMemberInvestorIds() {
        return memberInvestorIds;
    }

    public void setMemberInvestorIds(List<Long> memberInvestorIds) {
        this.memberInvestorIds = memberInvestorIds;
    }

    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public java.time.LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(java.time.LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Syndicate that = (Syndicate) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
