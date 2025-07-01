# Transaction Architecture 設計書

シンジケートローン管理システムにおける統一Transaction基盤の設計とアーキテクチャを説明します。

## 概要

全ての取引タイプ（Drawdown, Payment, FeePayment, FacilityInvestment）に対して統一的な管理基盤を提供し、取引の追跡・監査・レポート機能を実現しています。

## アーキテクチャ設計

### Transaction基底クラス設計

```java
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Transaction {
    // 共通フィールド
    private Long id;                    // 取引ID
    private Long facilityId;            // Facility ID
    private Long borrowerId;            // Borrower ID
    private LocalDate transactionDate; // 取引日
    private TransactionType transactionType; // 取引タイプ（enum）
    private TransactionStatus status;   // 取引状態（enum）
    private Money amount;               // 取引金額
    private LocalDateTime createdAt;    // 作成日時
    private LocalDateTime updatedAt;    // 更新日時
    private Long version;               // 楽観的排他制御
}
```

### 継承戦略：JPA JOINED

- **基底テーブル**: `transaction` - 共通フィールドを格納
- **サブクラステーブル**: 各取引タイプ専用フィールドを格納
- **結合**: サブクラス検索時に自動JOIN実行

### Transaction階層

```
Transaction (基底クラス)
├── Drawdown extends Transaction
│   ├── loanId: Long
│   ├── currency: String
│   ├── purpose: String
│   └── amountPies: List<AmountPie>
├── Payment extends Transaction
│   ├── loanId: Long
│   ├── paymentDate: LocalDate
│   ├── totalAmount: Money
│   ├── principalAmount: Money
│   ├── interestAmount: Money
│   ├── currency: String
│   └── paymentDistributions: List<PaymentDistribution>
├── FeePayment extends Transaction
│   ├── feeType: FeeType
│   ├── feeDate: LocalDate
│   ├── description: String
│   ├── recipientType: String
│   ├── recipientId: Long
│   ├── calculationBase: Money
│   ├── feeRate: Double
│   ├── currency: String
│   └── feeDistributions: List<FeeDistribution>
└── FacilityInvestment extends Transaction
    └── investorId: Long
```

## 列挙型定義

### TransactionType enum

| 値 | 説明 | 用途 |
|---|---|---|
| `DRAWDOWN` | ドローダウン | 資金引出取引 |
| `PAYMENT` | 支払い | 元本・利息返済 |
| `FEE_PAYMENT` | 手数料支払い | 各種手数料処理 |
| `FACILITY_INVESTMENT` | ファシリティ投資 | 投資家の投資記録 |
| `TRADE` | 取引 | セカンダリー取引（将来拡張） |
| `SETTLEMENT` | 精算 | 最終精算（将来拡張） |

### TransactionStatus enum

| 値 | 説明 | 用途 |
|---|---|---|
| `PENDING` | 保留中 | 承認待ち状態 |
| `PROCESSING` | 処理中 | 実行中状態 |
| `COMPLETED` | 完了 | 正常完了 |
| `FAILED` | 失敗 | エラー発生 |
| `CANCELLED` | キャンセル | 意図的中止 |
| `REFUNDED` | 返金済み | 返金処理完了（将来拡張） |

## サービス層設計

### TransactionService（横断的管理）

```java
@Service
public class TransactionService {
    // 横断的検索機能
    List<Transaction> getTransactionsByFacility(Long facilityId);
    List<Transaction> getTransactionsByBorrower(Long borrowerId);
    List<Transaction> getTransactionsByType(TransactionType type);
    
    // 状態管理機能
    void approveTransaction(Long transactionId);
    void completeTransaction(Long transactionId);
    void cancelTransaction(Long transactionId, String reason);
    void failTransaction(Long transactionId, String errorMessage);
    
    // 統計・レポート機能
    TransactionStatistics getTransactionStatistics(Long facilityId);
}
```

### 各取引タイプ専用サービス

- **DrawdownService**: ドローダウン特有のビジネスロジック
- **PaymentService**: 返済処理・配分計算
- **FeePaymentService**: 手数料計算・配分処理
- **FacilityService**: FacilityInvestment生成

## REST API設計

### 横断的Transaction API

```
GET    /api/v1/transactions/{id}                    # 取引詳細
GET    /api/v1/transactions                         # 全取引（ページング）
GET    /api/v1/transactions/facility/{facilityId}   # Facility別取引履歴
GET    /api/v1/transactions/borrower/{borrowerId}   # Borrower別取引履歴
GET    /api/v1/transactions/type/{transactionType}  # タイプ別取引
GET    /api/v1/transactions/facility/{facilityId}/statistics # 取引統計

POST   /api/v1/transactions/{id}/approve            # 取引承認
POST   /api/v1/transactions/{id}/complete           # 取引完了
POST   /api/v1/transactions/{id}/cancel             # 取引キャンセル
POST   /api/v1/transactions/{id}/fail               # 取引失敗
```

### 取引タイプ別API

```
POST   /api/v1/loans/drawdowns        # ドローダウン実行
POST   /api/v1/loans/payments         # 返済処理
POST   /api/v1/fees/payments          # 手数料支払い
POST   /api/v1/facilities             # Facility作成（FacilityInvestment生成）
```

## ビジネス価値

### 1. 統一的な取引管理
- 全取引タイプの一貫した追跡・監査
- 横断的なレポート・分析機能
- 取引状態の統一的な管理

### 2. 拡張性の確保
- 新しい取引タイプの追加が容易
- 既存コードへの影響最小化
- 一貫したAPI設計パターン

### 3. データ整合性の保証
- Transaction基底クラスによる必須フィールド強制
- enum活用による型安全性
- 楽観的排他制御による同時更新防止

### 4. 監査・コンプライアンス対応
- 全取引の完全な追跡履歴
- 状態変更の記録・監査証跡
- 金融業界の監査要件対応

## 実装の特徴

### 1. 段階的な移行
- 既存のDrawdown, FacilityInvestmentは元からTransaction継承
- PaymentをTransaction継承に変更（重複フィールド削除）
- 文字列リテラル→enum移行

### 2. ビジネスメソッドの提供
```java
// Transaction基底クラスのビジネスメソッド
transaction.markAsCompleted();
transaction.markAsFailed();
transaction.markAsCancelled();
transaction.isCompleted();
transaction.isCancellable();
transaction.isProcessing();
```

### 3. 自動状態設定
- `@PrePersist`でデフォルト状態（PENDING）設定
- 各サブクラスで適切なTransactionType自動設定

## 今後の拡張予定

1. **取引承認ワークフロー**: 複雑な承認プロセスの実装
2. **取引キャンセル・修正**: リバーストランザクション機能
3. **バッチ処理**: 大量取引の一括処理機能
4. **監査ログ強化**: 詳細な変更履歴追跡
5. **パフォーマンス最適化**: インデックス最適化・N+1問題対応

この設計により、シンジケートローン管理システムの取引管理が統一的かつ拡張可能な基盤として確立されています。