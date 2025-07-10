package com.example.syndicatelending.common.statemachine.events;

import java.time.LocalDateTime;

/**
 * Drawdown削除イベント
 * 
 * Drawdownが削除された際に発行されるドメインイベント。
 * 以下の状態復旧を引き起こす：
 * - Facility: FIXED → DRAFT
 * 
 * このイベントはSpring Eventsメカニズムを通じて
 * 関連するイベントハンドラーに配信される。
 */
public class DrawdownDeletedEvent {
    
    private final Long facilityId;
    private final Long drawdownId;
    private final LocalDateTime occurredAt;
    
    public DrawdownDeletedEvent(Long facilityId, Long drawdownId) {
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
        return "DrawdownDeletedEvent{" +
                "facilityId=" + facilityId +
                ", drawdownId=" + drawdownId +
                ", occurredAt=" + occurredAt +
                '}';
    }
}