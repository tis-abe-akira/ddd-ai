package com.example.syndicatelending.facility.entity;

import com.example.syndicatelending.common.domain.model.Percentage;
import com.example.syndicatelending.common.domain.model.PercentageAttributeConverter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "facility_share_pies")
public class SharePie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long investorId;

    @Convert(converter = PercentageAttributeConverter.class)
    @Column(nullable = false, precision = 8, scale = 4)
    private Percentage share;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    @JsonIgnore
    private Facility facility;

    // getter/setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getInvestorId() {
        return investorId;
    }

    public void setInvestorId(Long investorId) {
        this.investorId = investorId;
    }

    public Percentage getShare() {
        return share;
    }

    public void setShare(Percentage share) {
        this.share = share;
    }

    public Facility getFacility() {
        return facility;
    }

    public void setFacility(Facility facility) {
        this.facility = facility;
    }
}
