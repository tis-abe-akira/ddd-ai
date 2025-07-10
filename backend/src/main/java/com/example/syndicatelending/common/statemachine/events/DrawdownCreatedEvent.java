package com.example.syndicatelending.common.statemachine.events;

import java.time.LocalDateTime;

/**
 * Drawdown作成イベント
 * 
 * Drawdownが作成された際に発行されるドメインイベント。
 * 以下の状態変更を引き起こす：
 * - Facility: DRAFT → FIXED
 * 
 * このイベントはSpring Eventsメカニズムを通じて
 * 関連するイベントハンドラーに配信される。
 */
public class DrawdownCreatedEvent {
    
    private final Long facilityId;
    private final Long drawdownId;
    private final LocalDateTime occurredAt;
    
    public DrawdownCreatedEvent(Long facilityId, Long drawdownId) {
        this.facilityId = facilityId;
        this.drawdownId = drawdownId;
        this.occurredAt = LocalDateTime.now();
    }
    
    public Long getFacilityId() {
        return facilityId;
    }
    
    public Long getDrawdownId() {
        return drawdownId;
    }
    
    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
    
    @Override
    public String toString() {
        return "DrawdownCreatedEvent{" +
                "facilityId=" + facilityId +
                ", drawdownId=" + drawdownId +
                ", occurredAt=" + occurredAt +
                '}';
    }
}