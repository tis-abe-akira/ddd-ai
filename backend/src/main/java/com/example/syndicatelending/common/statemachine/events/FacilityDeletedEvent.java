package com.example.syndicatelending.common.statemachine.events;

import com.example.syndicatelending.facility.entity.Facility;
import java.time.LocalDateTime;

/**
 * Facility削除イベント
 * 
 * Facilityが削除された際に発行されるドメインイベント。
 * 以下の連鎖的状態復旧を引き起こす：
 * - Syndicate: ACTIVE → DRAFT
 * - Borrower: RESTRICTED → ACTIVE
 * - Investor: RESTRICTED → ACTIVE
 * 
 * このイベントはSpring Eventsメカニズムを通じて
 * 関連するイベントハンドラーに配信される。
 */
public class FacilityDeletedEvent {
    
    private final Facility facility;
    private final LocalDateTime occurredAt;
    
    public FacilityDeletedEvent(Facility facility) {
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
        return "FacilityDeletedEvent{" +
                "facilityId=" + facility.getId() +
                ", syndicateId=" + facility.getSyndicateId() +
                ", occurredAt=" + occurredAt +
                '}';
    }
}