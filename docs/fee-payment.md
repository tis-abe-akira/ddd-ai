# Fee Payment 機能仕様書

シンジケートローン管理システムにおける手数料支払い管理機能の仕様と実装を説明します。

## 概要

シンジケートローンにおける各種手数料の計算・配分・支払い処理を管理し、金融機関間の手数料収益分配を自動化します。

## ビジネス要件

### 手数料の種類と特性

| 手数料タイプ | 受益者 | 計算基準 | 配分方法 | 発生タイミング |
|-------------|--------|----------|----------|----------------|
| **管理手数料** (MANAGEMENT_FEE) | リードバンク | 融資枠残高×料率 | 単一受益者 | 定期（月次・四半期） |
| **アレンジメント手数料** (ARRANGEMENT_FEE) | リードバンク | 融資枠設定額×料率 | 単一受益者 | シンジケート組成時 |
| **コミットメント手数料** (COMMITMENT_FEE) | 投資家 | 未使用残高×料率 | 持分比率ベース | 定期（月次） |
| **取引手数料** (TRANSACTION_FEE) | エージェントバンク | 取引件数・金額ベース | 単一受益者 | 取引実行時 |
| **遅延手数料** (LATE_FEE) | 投資家 | 遅延金額×料率×日数 | 持分比率ベース | 遅延発生時 |
| **エージェント手数料** (AGENT_FEE) | エージェントバンク | 定額または料率 | 単一受益者 | 定期（月次） |
| **その他手数料** (OTHER_FEE) | 任意 | 個別設定 | 個別設定 | 不定期 |

## データモデル設計

### FeePayment エンティティ

```java
@Entity
@Table(name = "fee_payments")
public class FeePayment extends Transaction {
    private FeeType feeType;           // 手数料タイプ
    private LocalDate feeDate;         // 手数料日付
    private String description;        // 説明
    private String recipientType;      // 受益者タイプ（BANK/INVESTOR/BORROWER）
    private Long recipientId;          // 受益者ID
    private Money calculationBase;     // 計算基準額
    private Double feeRate;            // 手数料率（%）
    private String currency;           // 通貨
    private List<FeeDistribution> feeDistributions; // 配分詳細
}
```

### FeeDistribution エンティティ

```java
@Entity
@Table(name = "fee_distributions")
public class FeeDistribution {
    private String recipientType;      // 配分先タイプ
    private Long recipientId;          // 配分先ID
    private Money distributionAmount;  // 配分金額
    private Double distributionRatio;  // 配分比率（%）
    private String currency;           // 通貨
    private FeePayment feePayment;     // 親手数料支払い
}
```

## ビジネスロジック

### 手数料計算ルール

#### 1. 基本計算式
```
手数料額 = 計算基準額 × 手数料率 ÷ 100
```

#### 2. バリデーション
- 計算結果と入力された手数料額の整合性チェック
- 手数料日付の妥当性チェック（未来日付不可）
- 手数料タイプと受益者タイプの整合性チェック

#### 3. 受益者タイプ制約
```java
switch (feeType) {
    case MANAGEMENT_FEE:
    case ARRANGEMENT_FEE:
        // リードバンクのみ受益可能
        recipientType == "BANK"
        
    case COMMITMENT_FEE:
    case LATE_FEE:
        // 投資家配分必須
        recipientType == "INVESTOR"
        
    case TRANSACTION_FEE:
    case AGENT_FEE:
        // エージェントバンクのみ受益可能
        recipientType == "BANK"
        
    case OTHER_FEE:
        // 任意の受益者タイプ許可
}
```

### 配分計算ロジック

#### 投資家配分が必要な手数料（COMMITMENT_FEE, LATE_FEE）

```java
for (SharePie sharePie : facility.getSharePies()) {
    Money distributionAmount = feePayment.getAmount()
        .multiply(sharePie.getShare().getValue().divide(BigDecimal.valueOf(100)));
    
    FeeDistribution distribution = new FeeDistribution(
        "INVESTOR",
        sharePie.getInvestorId(),
        distributionAmount,
        sharePie.getShare().getValue().doubleValue(),
        currency
    );
}
```

#### 単一受益者手数料（その他）
- 指定された受益者に全額配分
- 配分比率100%

## REST API仕様

### エンドポイント一覧

```
POST   /api/v1/fees/payments                           # 手数料支払い作成
GET    /api/v1/fees/payments/{id}                       # 手数料支払い詳細
GET    /api/v1/fees/payments                            # 全手数料支払い（ページング）
GET    /api/v1/fees/payments/facility/{facilityId}      # Facility別手数料履歴
GET    /api/v1/fees/payments/type/{feeType}             # タイプ別手数料
GET    /api/v1/fees/payments/date-range                 # 日付範囲検索
GET    /api/v1/fees/payments/facility/{facilityId}/statistics # 手数料統計
```

### 手数料支払い作成API

**エンドポイント**: `POST /api/v1/fees/payments`

**リクエスト例**:
```json
{
  "facilityId": 1,
  "borrowerId": 1,
  "feeType": "MANAGEMENT_FEE",
  "feeDate": "2024-01-31",
  "feeAmount": 50000.00,
  "calculationBase": 10000000.00,
  "feeRate": 0.5,
  "recipientType": "BANK",
  "recipientId": 1,
  "currency": "JPY",
  "description": "2024年1月分管理手数料"
}
```

**レスポンス例**:
```json
{
  "id": 101,
  "facilityId": 1,
  "borrowerId": 1,
  "feeType": "MANAGEMENT_FEE",
  "feeDate": "2024-01-31",
  "description": "2024年1月分管理手数料",
  "recipientType": "BANK",
  "recipientId": 1,
  "calculationBase": {
    "amount": 10000000.00
  },
  "feeRate": 0.5,
  "currency": "JPY",
  "transactionType": "FEE_PAYMENT",
  "status": "PENDING",
  "amount": {
    "amount": 50000.00
  },
  "transactionDate": "2024-01-31",
  "feeDistributions": [
    {
      "id": 201,
      "recipientType": "BANK",
      "recipientId": 1,
      "distributionAmount": {
        "amount": 50000.00
      },
      "distributionRatio": 100.0,
      "currency": "JPY"
    }
  ]
}
```

### 手数料統計API

**エンドポイント**: `GET /api/v1/fees/payments/facility/{facilityId}/statistics`

**レスポンス例**:
```json
{
  "totalCount": 25,
  "totalAmount": "750000.00",
  "managementFeeCount": 12,
  "arrangementFeeCount": 1,
  "commitmentFeeCount": 10,
  "lateFeeCount": 2
}
```

## バリデーションルール

### 入力データ検証

```java
@NotNull(message = "Facility ID is required")
private Long facilityId;

@NotNull(message = "Fee type is required")
private FeeType feeType;

@NotNull(message = "Fee amount is required")
@DecimalMin(value = "0.0", inclusive = false)
private BigDecimal feeAmount;

@NotNull(message = "Fee rate is required")
@DecimalMin(value = "0.0")
@DecimalMax(value = "100.0")
private Double feeRate;

@Pattern(regexp = "BANK|INVESTOR|BORROWER")
private String recipientType;

@Size(min = 3, max = 3)
private String currency;
```

### ビジネスルール検証

1. **計算整合性**: 計算基準額×手数料率=手数料額
2. **日付妥当性**: 手数料日付が未来日付でない
3. **受益者整合性**: 手数料タイプと受益者タイプの組み合わせ
4. **Facility存在性**: 指定されたFacilityが存在する

## 実装の特徴

### 1. Transaction基底クラス継承
- 統一的な取引管理・追跡機能
- 状態管理・監査証跡の自動化
- 横断的なレポート機能との統合

### 2. 自動配分計算
- SharePie持分比率に基づく投資家配分
- 配分金額・比率の自動計算
- 配分詳細の永続化

### 3. 型安全な手数料分類
- FeeType enumによる手数料タイプ管理
- 受益者タイプとの整合性チェック
- ビジネスルール違反の防止

### 4. 金融計算の精度
- BigDecimalによる正確な金額計算
- Moneyクラスによる金融専用計算
- 丸め誤差の防止

## 使用例・シナリオ

### シナリオ1: 月次管理手数料

1. リードバンクが月末に管理手数料を請求
2. 融資枠残高10億円×年率0.5%÷12ヶ月=約41.7万円
3. 全額リードバンクの収益として記録

### シナリオ2: コミットメント手数料

1. 未使用融資枠2億円に対するコミットメント手数料
2. 年率0.25%×2億円÷12ヶ月=約4.2万円
3. 投資家3社の持分比率（50%・30%・20%）で自動配分

### シナリオ3: 遅延手数料

1. 返済遅延10日×遅延金額1,000万円×年率14.6%
2. 遅延手数料=1,000万円×14.6%×10日÷365日=約4万円
3. 投資家への配分として分配

## 今後の拡張予定

1. **手数料自動計算**: 定期的な手数料の自動生成機能
2. **手数料スケジュール**: 将来手数料の予定管理
3. **複雑な配分ルール**: ウォーターフォール配分・優先劣後構造
4. **手数料減免**: 条件に基づく手数料減額・免除機能
5. **外貨対応**: 複数通貨での手数料計算・配分

この機能により、シンジケートローンにおける手数料管理が完全に自動化され、透明性と正確性が確保されています。