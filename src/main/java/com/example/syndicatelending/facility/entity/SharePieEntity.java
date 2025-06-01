package com.example.syndicatelending.facility.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "facility_share_pies")
public class SharePieEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long facilityId;

    @Column(nullable = false)
    private Long investorId;

    @Column(nullable = false, precision = 8, scale = 4)
    private String share; // Percentage型はJPAではStringやBigDecimalで持つ

    // getter/setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(Long facilityId) {
        this.facilityId = facilityId;
    }

    public Long getInvestorId() {
        return investorId;
    }

    public void setInvestorId(Long investorId) {
        this.investorId = investorId;
    }

    public String getShare() {
        return share;
    }

    public void setShare(String share) {
        this.share = share;
    }
}
