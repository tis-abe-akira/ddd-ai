# Fee Payment管理 画面設計書

手数料支払い（Fee Payment）の登録・一覧・フィルタリング・統計確認を行う画面。

---

## 1. 概要

| 項目 | 内容 |
|---|---|
| URL | `/fees` |
| 目的 | 7種類の手数料支払いの登録・管理・統計確認 |
| 関連スクリーンショット | 未取得（後日追加予定） |

> スクリーンショット取得後、以下のファイル名で配置してください：
> - `fee-list.png`（一覧画面）
> - `fee-form.png`（登録フォーム）

---

## 2. 画面レイアウト

### 2.1 一覧モード（showForm=false）

> **[スクリーンショット未取得: fee-list.png]**

```
+----------------------------------------------------------+
| Fee Payments                       [New Fee Payment]     |
+----------------------------------------------------------+
| Filters:                                                 |
| Facility [▼ All Facilities] FeeType [▼ All Types]       |
| Start Date [YYYY-MM-DD]  End Date [YYYY-MM-DD]          |
| [Clear Filters]                                         |
+----------------------------------------------------------+
| Statistics（Facility選択中 && statistics取得済み のとき）|
| [Total: N件] [Total Amount: ¥XXX] [Most Common: XXX]    |
| [Types: N種]                                             |
| Fee Type Breakdown: 各タイプの件数・割合（プログレスバー）|
+----------------------------------------------------------+
| エラーバナー（エラー発生時のみ）                          |
+----------------------------------------------------------+
| ID | FeeType | 金額 | 計算基準 | 手数料率 | 支払日 | 受取人 | Facility | ACTIONS        |
|----|---------|------|----------|---------|--------|--------|---------|----------------|
| 1  | ...     | ¥... | ¥...     | X.XX%   | ...    | ...    | #N      | View / Delete  |
+----------------------------------------------------------+
| ページネーション（totalPages > 1 のとき）                |
+----------------------------------------------------------+
```

### 2.2 登録フォーム（showForm=true）

> **[スクリーンショット未取得: fee-form.png]**

```
+----------------------------------------------------------+
| Fee Payments                       [Back to List]        |
+----------------------------------------------------------+
| Facility * [▼ Facilityを選択]                            |
| （Facility選択後に Borrower情報を表示）                   |
|   Borrower: [Borrower名]（自動セット）                   |
|                                                          |
| Fee Type *（カード形式ラジオボタン）                     |
|  ┌────────────────┐ ┌────────────────┐                 |
|  │ Management Fee │ │Arrangement Fee │                  |
|  │ リードバンク収益│ │ リードバンク収益│                 |
|  └────────────────┘ └────────────────┘                 |
|  ┌────────────────┐ ┌────────────────┐                 |
|  │Commitment Fee  │ │Transaction Fee │                  |
|  │ 投資家配分     │ │ エージェント   │                   |
|  └────────────────┘ └────────────────┘                 |
|  ┌────────────────┐ ┌────────────────┐ ┌───────────┐  |
|  │  Late Fee      │ │   Agent Fee    │ │ Other Fee │  |
|  │  投資家配分    │ │  エージェント  │ │  手動指定 │  |
|  └────────────────┘ └────────────────┘ └───────────┘  |
|                                                          |
| 受取人（FeeTypeにより自動決定または手動選択）             |
|  自動の場合: "Automatically distributed to Lead Bank"   |
|  手動の場合: Recipient Type [▼] + Recipient [▼]         |
|                                                          |
| Calculation Base [__________]（Facility選択時に自動セット）|
| Fee Rate (%) [__________]（FeeTypeにより自動セット）     |
| Fee Amount * [__________]（自動計算: Base × Rate / 100）|
|   💡 計算プレビュー: ¥XXX × X.XX% = ¥XXX               |
| Fee Date * [YYYY-MM-DD] (max: 今日)                     |
| Currency [JPY]（readonly、Facility選択時に自動セット）   |
| Description * [__________________________________]      |
|               [_______________] （3行テキストエリア）    |
|                                                          |
|         [Cancel]              [Create Fee Payment]      |
+----------------------------------------------------------+
```

---

## 3. 項目定義

### 3.1 フィルターパネル項目

| ラベル | フィールド名 | 型 | 説明 |
|---|---|---|---|
| Facility | `facilityFilter` | select | 全Facilityリストから選択（"All Facilities" 含む） |
| Fee Type | `feeTypeFilter` | select | FeeTypeリストから選択（"All Types" 含む） |
| Start Date | `startDate` | date | 日付範囲フィルター開始 |
| End Date | `endDate` | date | 日付範囲フィルター終了 |

**フィルター優先順位（API呼び出しの優先度）:**

1. 日付範囲（startDate/endDate が設定されている場合）: `GET /api/v1/fees/payments/date-range?startDate=...&endDate=...`
2. Facility（facilityFilterが設定されている場合）: `GET /api/v1/fees/payments/facility/{facilityId}`
3. FeeType（feeTypeFilterが設定されている場合）: `GET /api/v1/fees/payments/type/{feeType}`
4. 全件: `GET /api/v1/fees/payments?page={n}`

### 3.2 一覧テーブル列定義

| 列名 | データソース | 備考 |
|---|---|---|
| ID | `feePayment.id` | |
| FeeType | `feePayment.feeType` | バッジ表示 |
| 金額 | `feePayment.amount` | 通貨フォーマット |
| 計算基準 | `feePayment.calculationBase` | |
| 手数料率 | `feePayment.feeRate` | X.XX% |
| 支払日 | `feePayment.feeDate` | |
| 受取人 | `feePayment.recipientType` + ID | |
| Facility | `feePayment.facilityId` | `#${facilityId}` |
| ACTIONS | — | View Details / Delete ボタン |

### 3.3 登録フォーム入力項目

| ラベル | フィールド名 | 型 | 必須 | 備考 |
|---|---|---|---|---|
| Facility | `facilityId` | select | ✅ | 選択時にcurrency・calculationBaseを自動セット |
| Borrower情報 | `borrowerId` | hidden（自動） | — | Facility選択時に自動決定、表示のみ |
| Fee Type | `feeType` | radio（カード形式） | ✅ | 7種類（下記参照） |
| Recipient Type | `recipientType` | select or hidden | ✅ | FeeTypeにより自動決定（下記参照） |
| Recipient | `recipientId` | select | 条件付き | `recipientType === 'INVESTOR'` のときのみ表示 |
| Calculation Base | `calculationBase` | number（step=0.01） | — | Facility選択時にcommitmentが自動セット |
| Fee Rate (%) | `feeRate` | number（step=0.01） | — | FeeTypeにより自動セット |
| Fee Amount | `feeAmount` | number（step=0.01） | ✅ | `calculationBase × feeRate / 100` で自動計算 |
| Fee Date | `feeDate` | date | ✅ | 最大値=今日 |
| Currency | `currency` | text（readonly） | ✅ | Facility選択時に自動セット |
| Description | `description` | textarea | ✅ | 3行 |

### 3.4 受取人自動決定ロジック

| FeeType | recipientType | 手動/自動 | 説明 |
|---|---|---|---|
| MANAGEMENT_FEE | LEAD_BANK | 自動 | リードバンクへの収益 |
| ARRANGEMENT_FEE | LEAD_BANK | 自動 | リードバンクへの収益 |
| COMMITMENT_FEE | AUTO_DISTRIBUTE | 自動 | 投資家へ持分比率ベースで配分 |
| LATE_FEE | AUTO_DISTRIBUTE | 自動 | 投資家へ持分比率ベースで配分 |
| TRANSACTION_FEE | AGENT_BANK | 自動 | エージェントバンクへの収益 |
| AGENT_FEE | AGENT_BANK | 自動 | エージェントバンクへの収益 |
| OTHER_FEE | 手動選択 | 手動 | recipientTypeとrecipientIdを手動で選択 |

### 3.5 Statistics Panel 表示項目

| 表示名 | 算出方法 |
|---|---|
| Total Payments | `statistics.totalFeePayments` |
| Total Amount | `statistics.totalFeeAmount`（通貨フォーマット） |
| Fee Types | 利用されているFeeTypeの種数 |
| Average Amount | `totalFeeAmount / totalFeePayments` |
| Fee Type Breakdown | 各FeeTypeの件数と割合（プログレスバー表示、降順ソート） |

---

## 4. 表示条件

| 要素 | 表示条件 |
|---|---|
| "New Fee Payment" ボタン表示 | 常時表示（フォーム中は "Back to List" に変わる） |
| フィルター・テーブル・ページネーション | `showForm === false` のとき |
| 登録フォーム | `showForm === true` のとき |
| Statistics Panel | `selectedFacility !== null && statistics !== null` のとき |
| エラーバナー | `error !== null` のとき |
| Borrower情報セクション | `facilityId > 0`（Facility選択後）のとき |
| 受取人自動表示テキスト | `isRecipientAutomatic(feeType) === true` のとき |
| 受取人手動選択UI（Recipient Type + Recipient） | `isRecipientAutomatic(feeType) === false` のとき |
| Recipient select（投資家リスト） | `recipientType === 'INVESTOR'` のとき |
| 計算プレビューバナー | `calculationBase > 0 && feeRate > 0` のとき |
| ページネーション | `totalPages > 1` のとき |

> **注意（Borrower/Investorとの違い）**:
> - "New Fee Payment" ボタンは常時表示でラベルが変わるトグル動作（Borrower/Investorと異なる）
> - 成功通知は `alert()` のみ（バナーなし）
> - 削除時に `window.confirm` なし（即時削除）

---

## 5. イベント一覧

| イベントID | イベント名 | トリガー |
|---|---|---|
| FP-01 | 登録フォーム表示 | "New Fee Payment" ボタンクリック |
| FP-02 | 一覧に戻る | "Back to List" ボタンクリック（フォーム内） |
| FP-03 | Fee Payment登録 | フォームの "Create Fee Payment" ボタンクリック |
| FP-04 | フォームキャンセル | "Cancel" ボタンクリック |
| FP-05 | Facilityフィルター変更 | Facilityドロップダウン変更 |
| FP-06 | FeeTypeフィルター変更 | FeeTypeドロップダウン変更 |
| FP-07 | 日付範囲フィルター設定 | Start Date / End Date 入力 |
| FP-08 | フィルタークリア | "Clear Filters" ボタンクリック |
| FP-09 | Fee Payment詳細表示 | テーブル行の "View Details" ボタンクリック |
| FP-10 | Fee Payment削除 | テーブル行の "Delete" ボタンクリック |
| FP-11 | ページ切替 | ページネーションボタンクリック |

---

## 6. イベント詳細

### FP-01: 登録フォーム表示

- **処理**: `setShowForm(true)`
- ボタンラベル変更: "New Fee Payment" → "Back to List"

---

### FP-02: 一覧に戻る

- **処理**: `setShowForm(false)`
- **注意**: これはページ遷移ではなく、`showForm` フラグの切り替えのみ

---

### FP-03: Fee Payment登録

- **前提条件**: 全必須項目入力済み、手数料額整合性チェック通過
- **API**: `POST /api/v1/fees/payments`
- **リクエスト**: `{ facilityId, borrowerId, feeType, recipientType, recipientId, calculationBase, feeRate, feeAmount, feeDate, currency, description }`
- **成功時**: `alert("Fee payment created successfully")` 表示後、`setShowForm(false)` でフォームを閉じる
- **失敗時**: `alert(errorMessage)`
- **副作用（API側）**:
  - FeeDistribution 自動生成（FeeTypeに応じた配分先へ）
  - COMMITMENT_FEE / LATE_FEE: 投資家へ持分比率ベースで配分
  - MANAGEMENT_FEE / ARRANGEMENT_FEE: リードバンクへの収益
  - TRANSACTION_FEE / AGENT_FEE: エージェントバンクへの収益

---

### FP-04: フォームキャンセル

- **処理**: `setShowForm(false)` + フォームの入力内容をリセット

---

### FP-05: Facilityフィルター変更

- **処理**:
  1. `setSelectedFacility(facility)`（または null で "All Facilities"）
  2. `setCurrentPage(0)` でページを先頭に戻す
  3. Statistics API を同時取得: `GET /api/v1/fees/payments/facility/{facilityId}/statistics`
- **副作用**: Statistics Panel が表示される（facilityが選択されている場合）

---

### FP-06: FeeTypeフィルター変更

- **処理**: `setFeeTypeFilter(type)` → `GET /api/v1/fees/payments/type/{feeType}` を呼び出し

---

### FP-07: 日付範囲フィルター設定

- **処理**: `setStartDate` / `setEndDate` → `GET /api/v1/fees/payments/date-range?startDate=...&endDate=...` を呼び出し
- **優先度**: 日付範囲フィルターはFacility / FeeTypeフィルターより優先される

---

### FP-08: フィルタークリア

- **処理**: `setSelectedFacility(null)` + `setFeeTypeFilter('')` + `setStartDate('')` + `setEndDate('')` + `setCurrentPage(0)`
- **副作用**: Statistics Panel が非表示になる

---

### FP-09: Fee Payment詳細表示

- **処理**: `alert(JSON.stringify(feePayment))` で詳細情報を表示
- **注意**: **モーダル未実装**。現状は `alert()` での表示のみ

---

### FP-10: Fee Payment削除

- **処理**: `DELETE /api/v1/fees/payments/{feePaymentId}` を呼び出し
- **注意**: **`window.confirm` なし（即時削除）**。Borrower/InvestorのDeleteとは動作が異なる
- **成功時**: `alert("Fee payment deleted successfully")` + 一覧リフレッシュ
- **失敗時**: `alert(errorMessage)`

---

### FP-11: ページ切替

- **処理**: `setCurrentPage(page)` → `GET /api/v1/fees/payments?page={n}` を呼び出し
