# Facility管理 画面設計書

融資枠（Facility）の作成・一覧・編集・削除・詳細確認を行う画面。4ステップのウィザード形式でSyndicate選択・融資条件・持分配分を設定する。

---

## 1. 概要

| 項目 | 内容 |
|---|---|
| URL | `/facilities` |
| 目的 | 融資枠の作成・管理（CRUD）・持分配分（SharePie）管理 |
| 関連スクリーンショット | `facilities-list.png` / `facilities-form-1-1.png` / `facilities-form-1-2.png` / `facilities-form-2.png` / `facilities-form-3-1.png` / `facilities-form-3-2.png` / `facilities-form-3-3.png` / `facilities-form-fin.png` / `facilities-show-detail.png`（9枚） |

---

## 2. 画面レイアウト

### 2.1 一覧モード（showForm=false）

![Facility一覧](./screenshots/facilities-list.png)

```
+----------------------------------------------------------------------+
| Facilities                              [+ New Facility]             |
| Create and manage financing facilities                               |
+----------------------------------------------------------------------+
| Search                                                               |
| [🔍 Search by facility ID or interest terms...        ] [Refresh]   |
+----------------------------------------------------------------------+
| +------------------+ +------------------+ +------------------------+ |
| | Draft            | | Active           | | Total Facility Amount  | |
| | 1                | | 0                | | ¥1000                  | |
| | Editable fac..   | | Confirmed fac..  | | Combined commitment    | |
| +------------------+ +------------------+ +------------------------+ |
+----------------------------------------------------------------------+
| 1 facility(ies)                                         Page 1 / 1  |
+----------------------------------------------------------------------+
| FACILITY         | AMOUNT     |SYNDICATE| PERIOD      |INVESTORS     |
| STATUS           | ACTIONS                                           |
|------------------|------------|---------|-------------|--------------|
| [F1] Facility #1 | ¥1000      | #1      | 2025-7-4    | 2 investors  |
|      TIBOR+0.2%  | JPY        |         | ~ 2026-7-4  | 100%         |
|      [Draft] v0  | ✏️ 🗑️                                             |
+----------------------------------------------------------------------+
```

> **Borrower / Investor との違い**: "New Facility" ボタンはトグル動作（常時表示）。Quick Stats（Draft件数・Fixed件数・Total合計）を表示。

### 2.2 ウィザード Step1: Syndicate選択

**選択前:**

![Step1 Syndicate選択（選択前）](./screenshots/facilities-form-1-1.png)

```
+----------------------------------------------------------------------+
| Facilities                             [+ Close Form]                |
| Create and manage financing facilities                               |
+----------------------------------------------------------------------+
| ①──────────── 2 ──────────── 3 ──────────── 4                      |
| Syndicate Selection                                                  |
| Select the syndicate to create the facility                          |
+----------------------------------------------------------------------+
| Syndicate Selection                                                  |
| Select the syndicate for this facility                               |
|                                                                      |
| Select Syndicate *                                                   |
| [Please select a syndicate                                       ▼ ] |
|                                                                      |
| [                   Next (disabled)                  ] [Cancel]     |
+----------------------------------------------------------------------+
```

**選択後:**

![Step1 Syndicate選択（選択後）](./screenshots/facilities-form-1-2.png)

```
| Select Syndicate *                                                   |
| [[A] ABC-Syndicate-2026  Draft  ID: 10 | Members: 3            ▼ ] |
|                                                                      |
| [                         Next                         ] [Cancel]   |
```

### 2.3 ウィザード Step2: 基本情報

![Step2 基本情報](./screenshots/facilities-form-2.png)

```
+----------------------------------------------------------------------+
| ✓ ──────────── ②──────────── 3 ──────────── 4                     |
| Basic Information                                                    |
| Enter basic facility information                                     |
+----------------------------------------------------------------------+
| Facility Basic Information                                           |
| Set the basic conditions for the credit facility                     |
|                                                                      |
| Facility Amount *                      | Currency *                 |
| Credit Limit: ¥2000  Available: ¥2000  |                            |
| [2000                               ]  | [JPY (Japanese Yen)    ▼ ] |
|                                                                      |
| Start Date *                           | End Date *                 |
| [2026/06/02                      📅 ]  | [2027/06/02          📅 ] |
|                                                                      |
| Interest Terms *                                                     |
| [4.5                                                              ]  |
|                                                                      |
| [      Back      ] [                Next                ] [Cancel]  |
+----------------------------------------------------------------------+
```

> 与信残高（`creditLimit - currentFacilityAmount`）が正の場合は success色、0以下の場合は warning色で表示。

### 2.4 ウィザード Step3: 投資家持分配分（SharePie）

**初期状態（配分未設定）:**

![Step3 持分配分（初期）](./screenshots/facilities-form-3-1.png)

```
+----------------------------------------------------------------------+
| ✓ ──────── ✓ ──────────── ③──────────── 4                         |
| Share Allocation                                                     |
| Set investor share percentages                                       |
+----------------------------------------------------------------------+
| Investor Share Allocation                                            |
| Set the share percentage for each investor (total 100%)              |
|                                                                      |
| Investor Share Allocation *                                          |
| [i] Only investors participating in Syndicate "ABC-Syndicate-2026"  |
|     can be selected (3 investors)                                    |
|                                                                      |
| [+ Add Investor]                                                     |
|                                                                      |
| +------------------------------------------------------------------+ |
| | Total Share Percentage                               0%  ⚠      | |
| | Please adjust to reach 100% total (current: -100%)              | |
| +------------------------------------------------------------------+ |
|                                                                      |
| [      Back      ] [         Next (disabled)          ] [Cancel]    |
+----------------------------------------------------------------------+
```

**配分中（一部設定済み）:**

![Step3 持分配分（設定中）](./screenshots/facilities-form-3-2.png)

```
| +------------------------------------------------------------------+ |
| | [i] investor02  ID: 2  [BANK] Investment Capacity: ¥27  50 % 🗑 | |
| +------------------------------------------------------------------+ |
| | Investor                      | Share Percentage (%)             | |
| | [investor04 (ID: 4)       ▼ ] | [30                    ]         | |
| | [Add] [Cancel]                                                   | |
| +------------------------------------------------------------------+ |
| | Total Share Percentage                               50%  ⚠      | |
| | Please adjust to reach 100% total (current: -50%)                | |
| +------------------------------------------------------------------+ |
```

**配分完了（合計100%）:**

![Step3 持分配分（完了）](./screenshots/facilities-form-3-3.png)

```
| +------------------------------------------------------------------+ |
| | [i] investor02  ID: 2  [BANK]       Investment Capacity: ¥27  50 % 🗑 |
| | [i] investor04  ID: 4  [LEAD_BANK]  Investment Capacity: ¥47  30 % 🗑 |
| | [i] investor06  ID: 6  [BANK]       Investment Capacity: ¥67  20 % 🗑 |
| +------------------------------------------------------------------+ |
| | Total Share Percentage                              100%  ✓ (green)| |
| | Total share percentage has reached 100%                          | |
| +------------------------------------------------------------------+ |
|                                                                      |
| [      Back      ] [                Next                ] [Cancel]  |
```

> 合計が100%になるまで "Next" ボタンは disabled。

### 2.5 ウィザード Step4: 確認

> **注意**: facilities-form-fin.png は Facility 作成後の一覧画面（2件表示）

![Facilities一覧（作成後）](./screenshots/facilities-form-fin.png)

```
+----------------------------------------------------------------------+
| Facilities                              [+ New Facility]             |
| Create and manage financing facilities                               |
+----------------------------------------------------------------------+
| Search: [                                              ] [Refresh]   |
| Draft: 2 | Active: 0 | Total Facility Amount: ¥3000                  |
| 2 facility(ies)                                         Page 1 / 1  |
+----------------------------------------------------------------------+
| [F11] Facility #11  ¥2000 JPY  #10  2026-6-2~2027-6-2  2inv 100% [Draft] v0 | ✏️ 🗑️ |
| [F1]  Facility #1   ¥1000 JPY  #1   2025-7-4~2026-7-4  2inv 100% [Draft] v0 | ✏️ 🗑️ |
+----------------------------------------------------------------------+
```

Step4 確認画面（スクリーンショット未取得）:

```
+----------------------------------------------------------------------+
| ✓ ──── ✓ ──── ✓ ──────────── ④                                    |
| Confirmation                                                         |
+----------------------------------------------------------------------+
| Syndicate      : ABC-Syndicate-2026 (ID: 10)                        |
| Facility Amount: ¥2000 / JPY                                        |
| Period         : 2026-06-02 ~ 2027-06-02                            |
| Interest Terms : 4.5                                                 |
| Share Pies     :                                                     |
|   investor02 (BANK)      : 50%  ¥1,000                             |
|   investor04 (LEAD_BANK) : 30%  ¥600                               |
|   investor06 (BANK)      : 20%  ¥400                               |
|                                                                      |
| [  Cancel  ] [  Back  ]              [  Create Facility  ]          |
+----------------------------------------------------------------------+
```

> 編集モードでは "Update Facility" ボタンが表示される。

### 2.6 詳細モーダル

![Facility詳細モーダル](./screenshots/facilities-show-detail.png)

```
+---------------------------------------------------------------+
| [F11] Facility #11 詳細                                  [×] |
|       Credit Facility Information                             |
+---------------------------------------------------------------+
| 基本情報                       | 期間情報                     |
|--------------------------------|------------------------------|
| Facility Amount                | Start Date                   |
| ¥ 2,000  JPY                   | 2026年6月2日                 |
|                                | End Date                     |
| Interest Terms                 | 2027年6月2日                 |
| 4.5                            | Created At                   |
|                                | 2026年6月2日                 |
| Status                         | Updated At                   |
| [Draft（編集可能）]             | 2026年6月2日                 |
|                                |                              |
| Version: v0                    |                              |
+---------------------------------------------------------------+
| 関連シンジケート情報                                          |
| Syndicate Name    Borrower       Lead Bank                    |
| ABC-Syndicate-2026  borrower02   investor04                   |
+---------------------------------------------------------------+
| 投資家別持分配分                              合計: 100%      |
|---------------------------------------------------------------|
| [i] investor04  ID: 4  [LEAD_BANK]  Capacity: ¥40,000        |
|                                              70%   ¥1,400    |
| [i] investor06  ID: 6  [BANK]       Capacity: ¥60,000        |
|                                              30%   ¥600      |
+---------------------------------------------------------------+
```

---

## 3. 項目定義

### 3.1 一覧テーブル列定義

| 列名 | データソース | 備考 |
|---|---|---|
| ID | `facility.id` | |
| Syndicate | `facility.syndicateId`（名前別途取得） | |
| 融資枠 | `facility.commitment` | 通貨フォーマット |
| 通貨 | `facility.currency` | |
| 開始日 | `facility.startDate` | |
| 終了日 | `facility.endDate` | |
| 状態 | `facility.status` | DRAFT（黄） / FIXED（緑） バッジ |
| ACTIONS | — | Detail / Edit / Delete ボタン |

### 3.2 Quick Stats 定義

| 表示名 | 算出方法 |
|---|---|
| Draft件数 | `facilities.filter(f => f.status === 'DRAFT').length` |
| Fixed件数 | `facilities.filter(f => f.status === 'FIXED').length` |
| Total Facility Amount | `facilities.reduce((sum, f) => sum + f.commitment, 0)` |

### 3.3 ウィザード各ステップの入力項目

| ステップ | ラベル | フィールド名 | 型 | 必須 | 備考 |
|---|---|---|---|---|---|
| Step1 | Syndicate | `syndicateId` | SyndicateSelectコンポーネント | ✅ | 編集時はdisabled |
| Step2 | Facility Amount | `commitment` | number | ✅ | Borrower与信残高を参考表示 |
| Step2 | Currency | `currency` | select | ✅ | JPY / USD / EUR 等 |
| Step2 | Start Date | `startDate` | date | ✅ | |
| Step2 | End Date | `endDate` | date | ✅ | |
| Step2 | Interest Terms | `interestTerms` | text | ✅ | 例: LIBOR + 2% |
| Step3 | 持分配分 | `sharePies[]` | SharePieAllocationコンポーネント | ✅ | 合計100%必須 |
| Step4 | — | — | 読み取り専用確認 | — | |

### 3.4 詳細モーダル 表示項目

| セクション | ラベル | データソース |
|---|---|---|
| 基本情報 | Facility Amount | `facility.commitment`（通貨フォーマット）+ `currency` |
| 基本情報 | Interest Terms | `facility.interestTerms` |
| 基本情報 | Status | `facility.status` バッジ |
| 基本情報 | Version | `v${facility.version}` |
| 期間情報 | Start Date | `facility.startDate` |
| 期間情報 | End Date | `facility.endDate` |
| 期間情報 | Created At / Updated At | タイムスタンプ |
| シンジケート情報 | Syndicate Name | `syndicateDetail.name` |
| シンジケート情報 | Borrower | `syndicateDetail.borrowerName` |
| シンジケート情報 | Lead Bank | `syndicateDetail.leadBankName` |
| 持分配分 | 各行 | 投資家名 / investorType バッジ / investmentCapacity / 持分% / 金額 |

---

## 4. 表示条件

| 要素 | 表示条件 |
|---|---|
| "New Facility" ボタン表示 | `showForm === false` のとき（トグル動作）  |
| "Close Form" ボタン表示 | `showForm === true` のとき |
| Quick Stats | `showForm === false` のとき |
| 検索ボックス・テーブル | `showForm === false` のとき |
| ウィザードフォーム | `showForm === true` のとき |
| "Next" ボタン活性 | 現ステップのバリデーション通過時のみ |
| 合計持分 success色 | `Math.abs(total - 1.0) < 0.0001`（100%） |
| 合計持分 warning色 | 上記以外 |
| 詳細モーダル表示 | `showDetail === true && selectedFacility !== null` |
| シンジケート情報セクション | `syndicateDetail !== null`（API取得成功時のみ） |
| 持分配分セクション | `facility.sharePies && facility.sharePies.length > 0` |
| Borrower与信残高 success色 | 残高が正（`creditLimit - currentFacilityAmount > 0`） |
| Borrower与信残高 warning色 | 残高が0以下 |

---

## 5. イベント一覧

| イベントID | イベント名 | トリガー |
|---|---|---|
| FA-01 | ウィザードフォーム表示/非表示トグル | "New Facility" / "Close Form" ボタンクリック |
| FA-02 | 次ステップへ進む | "Next" ボタンクリック |
| FA-03 | 前ステップへ戻る | "Back" ボタンクリック |
| FA-04 | Facility登録 | Step4の "Create Facility" ボタンクリック |
| FA-05 | Facility更新 | Step4の "Update Facility" ボタンクリック |
| FA-06 | フォームキャンセル | "Cancel" ボタンクリック |
| FA-07 | Facility詳細表示 | テーブル行の "Detail" ボタンクリック |
| FA-08 | 詳細モーダルを閉じる | モーダルの "×" ボタンクリック |
| FA-09 | Facility編集開始 | テーブル行の "Edit" ボタンクリック |
| FA-10 | Facility削除 | テーブル行の "Delete" ボタンクリック |
| FA-11 | 一覧リフレッシュ | "Refresh" ボタンクリック |

---

## 6. イベント詳細

### FA-01: ウィザードフォーム表示/非表示トグル

- **処理（一覧中）**: `setShowForm(true)`（ウィザード Step1 を表示）
- **処理（フォーム中）**: `setShowForm(false)` + `setEditMode('create')` + `setEditData(undefined)`
- ボタンラベル: `showForm === false` → "New Facility"、`showForm === true` → "Close Form"

---

### FA-02: 次ステップへ進む

- **前提条件**: 現ステップのバリデーション通過
  - Step1: `syndicateId` が選択済み
  - Step2: `commitment` / `currency` / `startDate` / `endDate` / `interestTerms` が全て入力済み
  - Step3: `sharePies` が1件以上かつ合計が100%
- **処理**: `setCurrentStep(currentStep + 1)`

---

### FA-03: 前ステップへ戻る

- **処理**: `setCurrentStep(currentStep - 1)`（入力内容は保持）

---

### FA-04: Facility登録

- **前提条件**: Step4（確認画面）表示中、`mode === 'create'`
- **API**: `POST /api/v1/facilities`
- **リクエスト**: `{ syndicateId, commitment, currency, startDate, endDate, interestTerms, sharePies }`
- **成功時**: 成功メッセージ + `setRefreshTrigger(prev + 1)` + `setShowForm(false)`
- **失敗時**: `alert(errorMessage)`
- **副作用（API側）**:
  - Borrower → RESTRICTED 状態に遷移
  - 全メンバーInvestor → RESTRICTED 状態に遷移
  - Syndicate → ACTIVE 状態に遷移
  - Facility → DRAFT 状態で作成

---

### FA-05: Facility更新

- **前提条件**: Step4 表示中、`mode === 'edit'`
- **API**: `PUT /api/v1/facilities/{id}`
- **リクエスト**: 登録と同様 + `version`（楽観的ロック）
- **制約**: DRAFT状態のFacilityのみ更新可能（FIXED状態では不可）

---

### FA-06: フォームキャンセル

- **処理**: `setShowForm(false)` + `setEditMode('create')` + `setEditData(undefined)` + ステップをStep1にリセット

---

### FA-07: Facility詳細表示

- **処理**:
  1. `setSelectedFacility(facility)`
  2. `setShowDetail(true)`
  3. モーダル内で `syndicateApi.getByIdWithDetails(facility.syndicateId)` を呼び出しシンジケート情報を取得
  4. `sharePies` の各投資家について `investorApi.getById(investorId)` を呼び出して投資家名を取得

---

### FA-08: 詳細モーダルを閉じる

- **処理**: `setShowDetail(false)` + `setSelectedFacility(null)`

---

### FA-09: Facility編集開始

- **前提条件**: 一覧モード（`showForm === false`）、`facility.status === 'DRAFT'`
- **処理**:
  1. `setEditMode('edit')`
  2. `setEditData(facility)`
  3. `setShowForm(true)`（ウィザード Step1 から表示、ただし syndicateId 選択欄は disabled）

---

### FA-10: Facility削除

- **処理**:
  1. `window.confirm(...)` でユーザー確認
  2. OK時: `DELETE /api/v1/facilities/{id}` を呼び出し
- **成功時**: 成功メッセージ + `setRefreshTrigger(prev + 1)`
- **失敗時**: `alert(errorMessage)`
- **制約（API側）**: Drawdownが存在するFacilityは削除不可
- **副作用（削除成功時）**:
  - Syndicate → DRAFT 状態に復旧
  - Borrower → ACTIVE 状態に復旧
  - 全メンバーInvestor → ACTIVE 状態に復旧

---

### FA-11: 一覧リフレッシュ

- **処理**: `setRefreshTrigger(prev + 1)` で FacilityTable に再フェッチをトリガー
