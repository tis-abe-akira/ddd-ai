package com.example.syndicatelending.syndicate.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * シンジケート更新リクエストDTO。
 */
public class UpdateSyndicateRequest {
    @NotBlank(message = "Name is required")
    private final String name;

    private final Long leadBankId;

    private final Long borrowerId;

    private final List<Long> memberInvestorIds;

    @NotNull(message = "Version is required for optimistic locking")
    private final Long version;

    @JsonCreator
    public UpdateSyndicateRequest(
            @JsonProperty("name") String name,
            @JsonProperty("leadBankId") Long leadBankId,
            @JsonProperty("borrowerId") Long borrowerId,
            @JsonProperty("memberInvestorIds") List<Long> memberInvestorIds,
            @JsonProperty("version") Long version) {
        this.name = name;
        this.leadBankId = leadBankId;
        this.borrowerId = borrowerId;
        this.memberInvestorIds = memberInvestorIds;
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public Long getLeadBankId() {
        return leadBankId;
    }

    public Long getBorrowerId() {
        return borrowerId;
    }

    public List<Long> getMemberInvestorIds() {
        return memberInvestorIds;
    }

    public Long getVersion() {
        return version;
    }
}
