# ドローダウン（Drawdown）機能の使用方法

## 概要

ドローダウンは、Facilityから資金を引き出す取引です。ドローダウン実行時には、Loanエンティティと返済スケジュールが自動生成されます。

## 機能の特徴

- **Loan自動生成**: ドローダウン実行時に対応するLoanエンティティが自動生成されます
- **返済スケジュール自動計算**: 返済方法に基づいて返済スケジュールが自動生成されます
  - 元利均等返済（EQUAL_INSTALLMENT）: 毎回の支払額（元本+利息）が一定
  - バレット返済（BULLET_PAYMENT）: 期間中は利息のみ支払い、満期時に元本一括返済
- **投資家別金額配分**: SharePieに基づいて投資家ごとのAmountPieが自動生成されます

## API仕様

### ドローダウン実行

- **エンドポイント**: `POST /api/v1/loans/drawdowns`
- **リクエスト例**:

```json
{
  "facilityId": 1,
  "borrowerId": 1,
  "amount": 10000000,
  "currency": "JPY",
  "purpose": "運転資金",
  "annualInterestRate": 0.025,
  "drawdownDate": "2025-06-01",
  "repaymentPeriodMonths": 12,
  "repaymentCycle": "MONTHLY",
  "repaymentMethod": "EQUAL_INSTALLMENT"
}
```

- **レスポンス**: 作成されたDrawdownエンティティ（AmountPieを含む）

### ドローダウン一覧取得

- **エンドポイント**: `GET /api/v1/loans/drawdowns`
- **レスポンス**: Drawdownエンティティのリスト

### ドローダウン詳細取得

- **エンドポイント**: `GET /api/v1/loans/drawdowns/{id}`
- **レスポンス**: 指定されたIDのDrawdownエンティティ

### ファシリティ別ドローダウン一覧取得

- **エンドポイント**: `GET /api/v1/loans/drawdowns/facility/{facilityId}`
- **レスポンス**: 指定されたファシリティIDに関連するDrawdownエンティティのリスト

### ページング対応ドローダウン一覧取得

- **エンドポイント**: `GET /api/v1/loans/drawdowns/paged`
- **パラメータ**:
  - `page`: ページ番号（0から開始）
  - `size`: ページサイズ
  - `sort`: ソート条件（例: `id,desc`）
- **レスポンス**: ページングされたDrawdownエンティティのリスト

## 処理フロー

1. クライアントがドローダウンリクエストを送信
2. システムがFacilityの残高を確認し、引き出し可能か検証
3. Loanエンティティが自動生成され、返済条件が設定される
4. 返済スケジュール（PaymentDetail）が自動生成される
5. 投資家ごとの金額配分（AmountPie）がSharePieに基づいて自動計算される
6. Drawdownトランザクションが記録される
7. 作成されたDrawdownエンティティがレスポンスとして返される

## 返済スケジュール計算ロジック

### 元利均等返済（EQUAL_INSTALLMENT）

毎回の支払額（元本+利息）が一定になるよう計算されます。

1. 月利を計算: `annualInterestRate / 12`
2. 毎月の支払額を計算: `P * r * (1 + r)^n / ((1 + r)^n - 1)`
   - P: 元本金額
   - r: 月利
   - n: 支払回数
3. 各回の支払いで、利息部分と元本部分を計算
   - 利息部分: `残高 * 月利`
   - 元本部分: `毎月の支払額 - 利息部分`
4. 残高を更新: `残高 = 残高 - 元本部分`

### バレット返済（BULLET_PAYMENT）

期間中は利息のみ支払い、満期時に元本を一括返済します。

1. 月利を計算: `annualInterestRate / 12`
2. 毎月の利息支払額を計算: `元本 * 月利`
3. 最終回のみ元本全額を返済

## エラーケース

- **Facility残高不足**: ドローダウン金額がFacilityの利用可能残高を超える場合
- **金利不正**: 金利がマイナスの場合
- **返済期間不正**: 返済期間が0以下の場合
- **Facility/Borrower存在しない**: 指定されたFacilityIDまたはBorrowerIDが存在しない場合

## 実装例

```java
// ドローダウン実行リクエスト作成
CreateDrawdownRequest request = new CreateDrawdownRequest();
request.setFacilityId(1L);
request.setBorrowerId(1L);
request.setAmount(new BigDecimal("10000000"));
request.setCurrency("JPY");
request.setPurpose("運転資金");
request.setAnnualInterestRate(new BigDecimal("0.025"));
request.setDrawdownDate(LocalDate.of(2025, 6, 1));
request.setRepaymentPeriodMonths(12);
request.setRepaymentCycle("MONTHLY");
request.setRepaymentMethod(RepaymentMethod.EQUAL_INSTALLMENT);

// APIリクエスト実行
ResponseEntity<Drawdown> response = restTemplate.postForEntity(
    "/api/v1/loans/drawdowns",
    request,
    Drawdown.class
);
```

## 関連エンティティ

- **Drawdown**: ドローダウン取引情報
- **Loan**: ローン情報（元本、金利、返済条件など）
- **PaymentDetail**: 返済スケジュール明細
- **AmountPie**: 投資家ごとの金額配分

## 注意事項

- ドローダウン実行後は、Loanエンティティと返済スケジュールが自動生成されます
- 返済スケジュールは返済方法に基づいて自動計算されます
- AmountPieはSharePieに基づいて自動生成されますが、明示的に指定することも可能です
