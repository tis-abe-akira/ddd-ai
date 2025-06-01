package com.example.syndicatelending.syndicate.dto;

import java.util.List;

public class CreateSyndicateRequest {
    private String name;
    private Long leadBankId;
    private List<Long> memberInvestorIds;

    public CreateSyndicateRequest() {
    }

    public CreateSyndicateRequest(String name, Long leadBankId, List<Long> memberInvestorIds) {
        this.name = name;
        this.leadBankId = leadBankId;
        this.memberInvestorIds = memberInvestorIds;
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
}
