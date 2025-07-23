package com.example.syndicatelending.common.statemachine.events;

import com.example.syndicatelending.facility.entity.Facility;
import java.time.LocalDateTime;

/**
 * Facility作成イベント
 * 
 * Facilityが作成された際に発行されるドメインイベント。
 * 以下の連鎖的状態変更を引き起こす：
 * - Syndicate: DRAFT → ACTIVE
 * - Borrower: ACTIVE → RESTRICTED
 * - Investor: ACTIVE → RESTRICTED
 * 
 * このイベントはSpring Eventsメカニズムを通じて
 * 関連するイベントハンドラーに配信される。
 */
public class FacilityCreatedEvent {
    
    private final Facility facility;
    private final LocalDateTime occurredAt;
    
    public FacilityCreatedEvent(Facility facility) {
        this.facility = facility;
        this.occurredAt = LocalDateTime.now();
    }
    
    public Facility getFacility() {
        return facility;
    }
    
    public Long getFacilityId() {
        return facility.getId();
    }
    
    public Long getSyndicateId() {
        return facility.getSyndicateId();
    }
    
    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
    
    @Override
    public String toString() {
        return "FacilityCreatedEvent{" +
                "facilityId=" + facility.getId() +
                ", syndicateId=" + facility.getSyndicateId() +
                ", occurredAt=" + occurredAt +
                '}';
    }
}