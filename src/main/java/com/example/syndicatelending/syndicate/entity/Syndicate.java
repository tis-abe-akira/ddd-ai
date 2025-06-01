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

    // シンジケート団のリードバンク（代表金融機関）
    private String leadBank;

    // メンバー（投資家IDのリスト、シンプルな形で実装）
    @ElementCollection
    @CollectionTable(name = "syndicate_members", joinColumns = @JoinColumn(name = "syndicate_id"))
    @Column(name = "investor_id")
    private List<Long> memberInvestorIds = new ArrayList<>();

    public Syndicate() {
    }

    public Syndicate(String name, String leadBank, List<Long> memberInvestorIds) {
        this.name = name;
        this.leadBank = leadBank;
        if (memberInvestorIds != null) {
            this.memberInvestorIds = memberInvestorIds;
        }
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

    public String getLeadBank() {
        return leadBank;
    }

    public void setLeadBank(String leadBank) {
        this.leadBank = leadBank;
    }

    public List<Long> getMemberInvestorIds() {
        return memberInvestorIds;
    }

    public void setMemberInvestorIds(List<Long> memberInvestorIds) {
        this.memberInvestorIds = memberInvestorIds;
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
