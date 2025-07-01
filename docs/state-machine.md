# State Machine 仕様書

シンジケートローン管理システムにおける状態管理の仕様を説明します。

## 概要

Spring State Machineを用いて、エンティティのライフサイクル管理を実現しています。

### 解決する課題
- **データ整合性の保証**: 不正な状態変更を防止し、ビジネスルール違反を根本的に阻止
- **複雑なビジネスルールの明確化**: 状態遷移図により、ビジネス要件を可視化・文書化
- **運用リスクの軽減**: 人的ミスによる不正操作（2度目のドローダウン等）を技術的に防止
- **監査要件への対応**: 金融業界で求められる状態変更の追跡可能性を確保

### ビジネス価値
- **信頼性向上**: システムが自動的にビジネス制約を強制し、データ破綻を防止
- **開発効率化**: 状態管理ロジックの一元化により、コードの重複・バグを削減
- **保守性向上**: 状態遷移が明示的で、ビジネスルール変更時の影響範囲が明確

## 1. Facility状態管理

### 状態定義
- **DRAFT**: 作成直後（変更可能）
- **FIXED**: ドローダウン実行後（変更不可・確定済み）

### 状態遷移
```
DRAFT --[DRAWDOWN_EXECUTED]--> FIXED
```

### ビジネスルール
- **DRAFT状態**: SharePie（持分比率）の変更・更新が可能
- **FIXED状態**: 
  - Facility情報の変更を禁止
  - 2度目のドローダウン実行を禁止
  - 確定済みのため不変

### 遷移トリガー
- `DRAWDOWN_EXECUTED`: ドローダウン実行時に自動発火

## 2. Loan状態管理

### 状態定義
- **DRAFT**: ドローダウン直後（返済未開始）
- **ACTIVE**: 返済中状態（正常返済中）
- **OVERDUE**: 遅延状態（返済遅延中）
- **COMPLETED**: 完済状態（返済完了）

### 状態遷移
```
DRAFT --[FIRST_PAYMENT]--> ACTIVE
ACTIVE --[PAYMENT_OVERDUE]--> OVERDUE
OVERDUE --[OVERDUE_RESOLVED]--> ACTIVE
ACTIVE --[FINAL_PAYMENT]--> COMPLETED
OVERDUE --[FINAL_PAYMENT]--> COMPLETED
```

### ビジネスルール
- **DRAFT状態**: 支払いスケジュール生成済み、返済未開始
- **ACTIVE状態**: 期日通りの返済が続いている正常状態
- **OVERDUE状態**: 返済期日を過ぎても未返済、延滞管理が必要
- **COMPLETED状態**: 全ての元本・利息返済完了、残高ゼロ

### 遷移トリガー
- `FIRST_PAYMENT`: 初回の元本または利息支払い時
- `PAYMENT_OVERDUE`: 返済期日を過ぎても未返済の場合
- `OVERDUE_RESOLVED`: 遅延していた支払いを実行した場合
- `FINAL_PAYMENT`: 全ての元本・利息返済完了時

## 3. Party状態管理

### Borrower状態定義
- **ACTIVE**: 通常状態（変更可能）
- **RESTRICTED**: Facility参加後（制限状態）

### Investor状態定義
- **ACTIVE**: 通常状態（変更可能）
- **RESTRICTED**: Facility参加後（制限状態）

### 状態遷移
```
ACTIVE --[FACILITY_PARTICIPATION]--> RESTRICTED
```

### ビジネスルール
- **ACTIVE状態**: 基本情報の変更・更新が可能
- **RESTRICTED状態**: 
  - 基本情報の変更を制限
  - Facility参加により重要データの整合性を保護

### 遷移トリガー
- `FACILITY_PARTICIPATION`: Facility組成時に自動発火

## 4. 統合状態管理

### EntityStateService
Facility組成時の連鎖的状態変更を管理：

1. **Borrower状態遷移**: ACTIVE → RESTRICTED
2. **関連Investor状態遷移**: ACTIVE → RESTRICTED

### 処理フロー
```
Facility作成 → EntityStateService.onFacilityCreated()
    ↓
1. SyndicateからBorrower取得
    ↓
2. Borrower: ACTIVE → RESTRICTED
    ↓
3. FacilityのSharePieからInvestor一覧取得
    ↓
4. 各Investor: ACTIVE → RESTRICTED
```

## 5. 実装アーキテクチャ

### コンポーネント構成
- **State/Event定義**: 各エンティティごとのEnum
- **StateMachineConfig**: Spring State Machine設定
- **EntityStateService**: 統合状態管理サービス
- **Service層**: 業務処理での状態遷移呼び出し

### パッケージ構造
```
com.example.syndicatelending.common.statemachine/
├── EntityStateService.java
├── facility/
│   ├── FacilityState.java
│   ├── FacilityEvent.java
│   └── FacilityStateMachineConfig.java
├── loan/
│   ├── LoanState.java
│   ├── LoanEvent.java
│   └── LoanStateMachineConfig.java
└── party/
    ├── BorrowerState.java
    ├── BorrowerEvent.java
    ├── InvestorState.java
    ├── InvestorEvent.java
    └── PartyStateMachineConfig.java
```

## 6. 利用例

### Facility状態管理
```java
// ドローダウン実行時の状態遷移
@Transactional
public void executeDrawdown(CreateDrawdownRequest request) {
    // ドローダウン処理...
    
    // Facility状態をFIXEDに遷移
    facility.setStatus(FacilityState.FIXED);
    facilityRepository.save(facility);
}
```

### Loan状態管理
```java
// 初回返済時の状態遷移
@Transactional 
public void processPayment(CreatePaymentRequest request) {
    // 返済処理...
    
    // 初回返済の場合、Loan状態をACTIVEに遷移
    if (loan.getStatus() == LoanState.DRAFT) {
        loan.setStatus(LoanState.ACTIVE);
        loanRepository.save(loan);
    }
}
```

## 7. 今後の拡張予定

- **返済遅延の自動検知**: スケジューラーによるOVERDUE状態への自動遷移
- **Facility期限管理**: 期限切れ状態の追加
- **複雑な状態遷移**: 条件分岐やガード条件の追加
- **監査ログ**: 状態遷移履歴の記録