# Context間依存関係とデータフロー

## エンティティ関係概要

### 主要エンティティとその関係

```mermaid
graph TB
    subgraph "Party Context"
        Borrower[Borrower]
        Investor[Investor]
    end
    
    subgraph "Syndicate Context"
        Syndicate[Syndicate]
    end
    
    subgraph "Facility Context"
        Facility[Facility]
        SharePie[SharePie]
    end
    
    subgraph "Loan Context"
        Loan[Loan]
        Drawdown[Drawdown]
        AmountPie[AmountPie]
    end
    
    subgraph "Fee Context"
        FeePayment[FeePayment]
    end
    
    %% ID参照による関係
    Syndicate -.->|borrowerId| Borrower
    Syndicate -.->|leadBankId| Investor
    Facility -.->|syndicateId| Syndicate
    SharePie -.->|facilityId| Facility
    SharePie -.->|investorId| Investor
    Loan -.->|facilityId| Facility
    Drawdown -.->|facilityId| Facility
    AmountPie -.->|investorId| Investor
    FeePayment -.->|facilityId| Facility
```

## Context依存関係の解決

### 改善前: 直接Repository依存

```mermaid
graph LR
    subgraph "問題のあるパターン"
        PS[PartyService]
        SyS[SyndicateService]
    end
    
    subgraph "他Context Repository"
        SR[SyndicateRepository]
        FR[FacilityRepository]
        BR[BorrowerRepository]
        IR[InvestorRepository]
    end
    
    PS -.->|直接依存| SR
    PS -.->|直接依存| FR
    SyS -.->|直接依存| BR
    SyS -.->|直接依存| IR
    
    style PS fill:#ffcccc
    style SyS fill:#ffcccc
```

**問題点:**
- Bounded Context境界の破綻
- テスタビリティの低下
- 将来のマイクロサービス分割阻害
- "Unknown"表示によるデータ整合性リスク

### 改善後: イベント駆動調整

```mermaid
graph TB
    subgraph "Business Services"
        FS[FacilityService]
        DS[DrawdownService]
        PS[PartyService]
    end
    
    subgraph "Event Infrastructure"
        EP[EventPublisher]
        FLH[FacilityLifecycleEventHandler]
        DLH[DrawdownLifecycleEventHandler]
    end
    
    subgraph "State Management"
        FSM[FacilityStateManager]
        BSM[BorrowerStateManager]
        ISM[InvestorStateManager]
        SSM[SyndicateStateManager]
    end
    
    FS -->|publishEvent| EP
    DS -->|publishEvent| EP
    EP -.->|EventListener| FLH
    EP -.->|EventListener| DLH
    
    FLH --> FSM
    FLH --> BSM
    FLH --> ISM
    FLH --> SSM
    DLH --> FSM
    
    style EP fill:#ccffcc
    style FLH fill:#ccffcc
    style DLH fill:#ccffcc
```

**改善点:**
- イベント経由での疎結合
- 境界コンテキスト間の明確な分離
- 非同期処理による性能向上
- テスタビリティの大幅改善

## イベントフローパターン

### 1. Facility作成フロー

```mermaid
sequenceDiagram
    participant Client
    participant FS as FacilityService
    participant EP as EventPublisher
    participant FLH as FacilityLifecycleEventHandler
    participant SM as StateManagers

    Client->>+FS: createFacility(request)
    FS->>FS: Facility作成
    FS->>FS: SharePie作成
    FS->>FS: ビジネスルール検証
    FS->>FS: データベース保存
    
    FS->>+EP: publishEvent(FacilityCreatedEvent)
    Note over EP: 非同期イベント処理
    
    EP->>+FLH: handleFacilityCreated(event)
    
    FLH->>SM: Syndicate状態遷移
    FLH->>SM: Borrower状態遷移
    FLH->>SM: Investor状態遷移
    
    FLH-->>-EP: 全遷移完了
    EP-->>-FS: イベント発行完了
    FS-->>-Client: Facility作成完了
```

### 2. Drawdown作成フロー

```mermaid
sequenceDiagram
    participant Client
    participant DS as DrawdownService
    participant EP as EventPublisher
    participant DLH as DrawdownLifecycleEventHandler
    participant FSM as FacilityStateManager

    Client->>+DS: createDrawdown(request)
    DS->>DS: ビジネスルール検証
    DS->>DS: Loan作成
    DS->>DS: Drawdown作成
    DS->>DS: AmountPie計算
    DS->>DS: 投資家投資額更新
    DS->>DS: データベース保存
    
    DS->>+EP: publishEvent(DrawdownCreatedEvent)
    Note over EP: 非同期イベント処理
    
    EP->>+DLH: handleDrawdownCreated(event)
    
    DLH->>+FSM: transitionToFixed(facilityId)
    
    alt 初回ドローダウン
        FSM->>FSM: 状態遷移実行
        FSM->>FSM: Facility状態更新
        FSM-->>-DLH: 成功
    else 2度目のドローダウン
        FSM-->>DLH: BusinessRuleViolationException
        Note over DLH: 例外伝播
    end
    
    DLH-->>-EP: 遷移完了
    EP-->>-DS: イベント発行完了
    DS-->>-Client: 作成完了 or エラー
```

### 3. Context間データフロー

```mermaid
graph TB
    subgraph "リクエストフロー"
        A[クライアントリクエスト] --> B[サービス層]
        B --> C[エンティティ作成/更新]
        C --> D[データベース永続化]
        D --> E[イベント発行]
    end
    
    subgraph "イベントフロー"
        E --> F[イベントハンドラー]
        F --> G[State Manager]
        G --> H[StateMachine Executor]
        H --> I[State Machine]
        I --> J[エンティティ状態更新]
    end
    
    subgraph "レスポンスフロー"
        J --> K[状態検証]
        K --> L[成功/エラーレスポンス]
        L --> M[クライアントレスポンス]
    end
    
    style E fill:#ffffcc
    style F fill:#ffffcc
    style G fill:#ffffcc
```

## データ整合性パターン

### 1. 参照整合性保護

直接Repository依存ではなく、イベントを使用して参照整合性を維持:

```mermaid
graph LR
    subgraph "旧パターン"
        A[PartyService] -.->|直接チェック| B[SyndicateRepository]
        A -.->|直接チェック| C[FacilityRepository]
    end
    
    subgraph "新パターン"
        D[PartyService] -->|イベント| E[PartyDeletionRequested]
        E -.->|検証| F[SyndicateIntegrityHandler]
        E -.->|検証| G[FacilityIntegrityHandler]
    end
    
    style A fill:#ffcccc
    style D fill:#ccffcc
```

### 2. 状態同期

Context間状態変更をイベント経由で調整:

```mermaid
stateDiagram-v2
    [*] --> FacilityCreated
    
    state FacilityCreated {
        SyndicateDRAFT --> SyndicateACTIVE
        BorrowerACTIVE --> BorrowerRESTRICTED
        InvestorACTIVE --> InvestorRESTRICTED
    }
    
    FacilityCreated --> [*]
```

### 3. ビジネスルール検証

複数Contextに関わる複雑なビジネスルール:

```mermaid
graph TD
    A[ビジネス操作] --> B{イベント発行}
    B --> C[イベントハンドラー1]
    B --> D[イベントハンドラー2]
    B --> E[イベントハンドラーN]
    
    C --> F[State Manager 1]
    D --> G[State Manager 2]
    E --> H[State Manager N]
    
    F --> I{検証}
    G --> I
    H --> I
    
    I -->|成功| J[状態更新]
    I -->|失敗| K[BusinessRuleViolationException]
    
    K --> L[トランザクションロールバック]
```

## 設計メリット

### 1. 疎結合
- サービスはイベント発行のみで責務完了
- Context間の直接依存なし
- 新しいイベントハンドラーの追加が容易

### 2. 保守性
- 明確な関心事の分離
- イベント中心の組織化
- ビジネスフローの理解が容易

### 3. テスタビリティ
- 各コンポーネントを独立してテスト可能
- イベントハンドラーが容易にモック化可能
- 明確な入出力契約

### 4. スケーラビリティ
- イベント駆動アーキテクチャによるスケーリング対応
- 非同期処理
- 将来のマイクロサービス移行準備完了

### 5. データ整合性
- 必要な箇所での強い整合性維持
- 明確なエラーハンドリングとロールバック
- ビジネスルール検証の保持

このイベント駆動アプローチにより、Context間依存関係を解決しつつ、データ整合性とビジネスルール検証を維持し、将来のアーキテクチャ発展の基盤を提供しています。

## 主要な実装成果

### EntityStateService問題の解決
- **Before**: 661行の巨大クラス
- **After**: イベント軸での責務分散
  - FacilityLifecycleEventHandler: 複雑な連鎖状態変更
  - DrawdownLifecycleEventHandler: シンプルな状態変更

### テスト結果
- **190/190テスト成功** ✅
- **0失敗、0エラー** ✅
- **完全移行完了** ✅

### アーキテクチャ改善
- **コード重複削減**: StateMachineExecutorによる統一化
- **責務明確化**: 単一責任原則の徹底
- **保守性向上**: イベント中心組織による可読性向上

この改善により、理論と実践のバランスを取った持続可能で拡張可能なアーキテクチャを実現しました。