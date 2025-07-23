# Event駆動アーキテクチャ実装

このドキュメントでは、シンジケートローン管理システムにおけるEvent駆動アーキテクチャの実装について、Mermaidコミュニケーション図を用いて説明します。

## 概要

BoundedContext間の依存関係を解決するため、直接的なサービス呼び出しからEvent駆動アーキテクチャに移行しました。これにより、疎結合で拡張性の高いシステムを実現しています。

## 問題の背景

### 第1段階の問題（直接呼び出しパターン）
- FacilityServiceが直接InvestorStateを更新
- Cross-context双方向依存
- 状態更新ロジックの巨大化・複雑化
- AIによる修正で他部分が壊れる問題

### 第2段階の解決（Event駆動パターン）
- Eventを発行して間接的に状態更新
- 疎結合なCross-context通信
- 責務の明確な分離
- 拡張性とテスト容易性の向上

## Event駆動アーキテクチャフロー

### 概略図（エッセンス）

```mermaid
sequenceDiagram
    participant FS as FacilityService
    participant EP as EventPublisher
    participant EH as EventHandler
    participant SM as StateManager
    participant Entity as Investor/Borrower

    Note over FS,Entity: Event駆動による疎結合な状態管理

    FS->>EP: publishEvent(FacilityCreatedEvent)
    Note right of EP: 第2段階：直接呼び出しを<br/>Eventに置き換え
    
    EP->>EH: handleFacilityCreated()
    Note right of EH: Cross-context状態変更<br/>の責務を分離
    
    EH->>SM: transitionToRestricted()
    Note right of SM: StateMachine制約<br/>による安全な状態遷移
    
    SM->>Entity: setStatus(RESTRICTED)
    Note right of Entity: Facility参加により<br/>削除・変更を制限
```

### 詳細フロー（実装レベル）

### Facility作成時の状態変更フロー

```mermaid
sequenceDiagram
    participant Client as クライアント
    participant FS as FacilityService
    participant EP as ApplicationEventPublisher
    participant FH as FacilityLifecycleEventHandler
    participant SSM as SyndicateStateManager
    participant BSM as BorrowerStateManager
    participant ISM as InvestorStateManager
    participant SM as StateMachine
    participant SR as SyndicateRepository
    participant BR as BorrowerRepository
    participant IR as InvestorRepository
    participant SE as SyndicateEntity
    participant BE as BorrowerEntity
    participant IE as InvestorEntity

    Note over Client,IE: Facility作成によるCross-context状態変更
    
    Client->>FS: createFacility(request)
    FS->>FS: Facility作成処理
    
    Note over FS,EP: 第2段階：Event発行による疎結合
    FS->>EP: publishEvent(FacilityCreatedEvent)
    
    Note over EP,FH: Spring Events機能による非同期配信
    EP->>FH: handleFacilityCreated(event)
    
    Note over FH,ISM: 連鎖的状態変更の開始
    
    rect rgb(200, 255, 200)
        Note over FH,SE: 1. Syndicate状態遷移（DRAFT → ACTIVE）
        FH->>SSM: transitionToActive(syndicateId)
        SSM->>SM: StateMachine実行
        SM->>SSM: 状態遷移成功
        SSM->>SR: findById(syndicateId)
        SR->>SE: Syndicateエンティティ取得
        SSM->>SE: setStatus(ACTIVE)
        SSM->>SR: save(syndicate)
    end
    
    rect rgb(255, 200, 200)
        Note over FH,BE: 2. Borrower状態遷移（ACTIVE → RESTRICTED）
        FH->>BSM: transitionToRestricted(borrowerId)
        BSM->>SM: StateMachine実行
        SM->>BSM: 状態遷移成功
        BSM->>BR: findById(borrowerId)
        BR->>BE: Borrowerエンティティ取得
        BSM->>BE: setStatus(RESTRICTED)
        BSM->>BR: save(borrower)
    end
    
    rect rgb(200, 200, 255)
        Note over FH,IE: 3. Investor状態遷移（ACTIVE → RESTRICTED）
        loop SharePie内の各投資家
            FH->>ISM: transitionToRestricted(investorId)
            ISM->>SM: StateMachine実行
            SM->>ISM: 状態遷移成功
            ISM->>IR: findById(investorId)
            IR->>IE: Investorエンティティ取得
            ISM->>IE: setStatus(RESTRICTED)
            ISM->>IR: save(investor)
        end
    end
    
    FH->>FS: イベント処理完了
    FS->>Client: Facility作成完了
```

### コミュニケーション図（概略）

```mermaid
graph LR
    subgraph "Facility Context"
        FS[FacilityService]
    end
    
    subgraph "Event Infrastructure"
        EP[EventPublisher]
        EH[EventHandler]
    end
    
    subgraph "State Management"
        SM[StateManager]
    end
    
    subgraph "Party Context"
        Entity[Investor/Borrower<br/>Entity]
    end
    
    FS -->|publishEvent| EP
    EP -->|handleEvent| EH
    EH -->|transitionState| SM
    SM -->|updateStatus| Entity
    
    %% Styling
    classDef facilityStyle fill:#e3f2fd
    classDef eventStyle fill:#e8f5e8
    classDef stateStyle fill:#fff3e0
    classDef partyStyle fill:#fce4ec
    
    class FS facilityStyle
    class EP,EH eventStyle
    class SM stateStyle
    class Entity partyStyle
```

### コミュニケーション図（詳細）

```mermaid
graph TB
    subgraph "Facility Bounded Context"
        FS[FacilityService]
        FC[FacilityController]
    end
    
    subgraph "Event Infrastructure"
        EP[ApplicationEventPublisher]
        FE[FacilityCreatedEvent]
        FH[FacilityLifecycleEventHandler]
    end
    
    subgraph "State Management Layer"
        SSM[SyndicateStateManager]
        BSM[BorrowerStateManager]
        ISM[InvestorStateManager]
        SM[StateMachine Framework]
    end
    
    subgraph "Syndicate Bounded Context"
        SS[SyndicateService]
        SR[SyndicateRepository]
        SE[SyndicateEntity]
    end
    
    subgraph "Party Bounded Context"
        PS[PartyService]
        BR[BorrowerRepository]
        IR[InvestorRepository]
        BE[BorrowerEntity]
        IE[InvestorEntity]
    end
    
    %% Event Flow
    FC -->|createFacility| FS
    FS -->|publishEvent| EP
    EP -->|delivery| FE
    FE -->|EventListener| FH
    
    %% State Management Flow
    FH -->|transitionToActive| SSM
    FH -->|transitionToRestricted| BSM
    FH -->|transitionToRestricted| ISM
    
    %% StateMachine Integration
    SSM -->|executeTransition| SM
    BSM -->|executeTransition| SM
    ISM -->|executeTransition| SM
    
    %% Entity Update Flow
    SSM -->|save| SR
    BSM -->|save| BR
    ISM -->|save| IR
    
    SR -->|update| SE
    BR -->|update| BE
    IR -->|update| IE
    
    %% Styling
    classDef eventStyle fill:#e1f5fe
    classDef serviceStyle fill:#f3e5f5
    classDef stateStyle fill:#e8f5e8
    classDef entityStyle fill:#fff3e0
    
    class EP,FE,FH eventStyle
    class FS,SS,PS serviceStyle
    class SSM,BSM,ISM,SM stateStyle
    class SE,BE,IE,SR,BR,IR entityStyle
```

## アーキテクチャの特徴

### 1. **疎結合の実現**
- FacilityServiceは他のBoundedContextを直接知らない
- Eventを通じた間接的な通信
- 依存方向の一方向化

### 2. **責務の明確な分離**
```java
// Event発行側（FacilityService）
eventPublisher.publishEvent(new FacilityCreatedEvent(savedFacility));

// Event処理側（FacilityLifecycleEventHandler）
@EventListener
public void handleFacilityCreated(FacilityCreatedEvent event) {
    syndicateStateManager.transitionToActive(syndicate.getId());
    borrowerStateManager.transitionToRestricted(syndicate.getBorrowerId());
    // ...
}
```

### 3. **State Machineとの統合**
```java
// InvestorStateManager内での状態遷移
boolean success = stateMachineExecutor.executeTransition(
    investorStateMachine,
    investor.getStatus(),          // 現在状態
    InvestorEvent.FACILITY_PARTICIPATION,  // イベント
    investorId,
    "Investor"
);

if (success) {
    investor.setStatus(InvestorState.RESTRICTED);  // 新状態
    investorRepository.save(investor);
}
```

### 4. **トランザクション管理**
- 各EventHandlerは独立したトランザクション
- 状態変更の原子性を保証
- エラー時の適切なロールバック

## 実現された効果

### ✅ **解決された問題**
1. **Cross-context双方向依存の排除**
2. **状態管理ロジックの分散化**
3. **テスト容易性の向上**
4. **保守性の大幅改善**

### ✅ **追加された価値**
1. **拡張性**: 新しいEventListenerを追加するだけで機能拡張
2. **監査性**: 全ての状態変更がEventとして記録
3. **非同期処理**: Spring Eventsによる非同期実行
4. **エラーハンドリング**: 各段階での適切なエラー処理

### ✅ **DDD原則の遵守**
1. **BoundedContextの独立性維持**
2. **ドメインイベントパターンの実装**
3. **集約間の疎結合**
4. **ビジネスルールの適切な配置**

## コード例

### Event定義
```java
public class FacilityCreatedEvent {
    private final Facility facility;
    private final LocalDateTime occurredAt;
    
    // 以下の連鎖的状態変更を引き起こす：
    // - Syndicate: DRAFT → ACTIVE
    // - Borrower: ACTIVE → RESTRICTED  
    // - Investor: ACTIVE → RESTRICTED
}
```

### EventHandler実装
```java
@Component
@Transactional
public class FacilityLifecycleEventHandler {
    
    @EventListener
    public void handleFacilityCreated(FacilityCreatedEvent event) {
        // 1. Syndicate状態遷移
        syndicateStateManager.transitionToActive(syndicate.getId());
        
        // 2. Borrower状態遷移
        borrowerStateManager.transitionToRestricted(syndicate.getBorrowerId());
        
        // 3. 関連Investor状態遷移
        for (Long investorId : investorIds) {
            investorStateManager.transitionToRestricted(investorId);
        }
    }
}
```

## 今後の発展方向

1. **Event Store導入**: イベントの永続化と再生機能
2. **Saga Pattern**: 複数BoundedContextにまたがる複雑なビジネス処理
3. **CQRS統合**: コマンドとクエリの分離
4. **マイクロサービス化**: BoundedContextの物理的分離

---

この Event駆動アーキテクチャにより、複雑なシンジケートローン業務における**Cross-context依存関係の問題を根本的に解決**し、**保守性・拡張性・テスト容易性**を大幅に向上させることができました。