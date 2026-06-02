# 画面設計書

シンジケートローン管理システムの全画面設計定義。

---

## 目次

1. [共通レイアウト](#共通レイアウト)
2. [ダッシュボード（/）](#ダッシュボード)
3. [Borrower管理画面（/borrowers）](#borrower管理画面)
4. [Investor管理画面（/lenders）](#investor管理画面)
5. [Syndicate管理画面（/syndicates）](#syndicate管理画面)
6. [Facility管理画面（/facilities）](#facility管理画面)
7. [Drawdown管理画面（/drawdowns）](#drawdown管理画面)
8. [Drawdown詳細画面（/drawdowns/:id）](#drawdown詳細画面)
9. [Fee Payment管理画面（/fees）](#fee-payment管理画面)

---

## 共通レイアウト

### 画面レイアウト

<!-- TODO: スクリーンショット配置予定 -->
ファイルパス: screenshots/common-layout.png
> **[スクリーンショット: 共通レイアウト全体]**
![](./screenshots/common-layout.png)
```
+----------------------------------------------------------+
| HEADER: シンジケートローン管理システム                    |
+----------------+-----------------------------------------+
| SIDEBAR        | MAIN CONTENT AREA                       |
|                |                                         |
| Dashboard      |  各ページのコンテンツ                   |
| Borrowers      |                                         |
| Investors      |                                         |
| Syndicates     |                                         |
| Facilities     |                                         |
| Drawdowns      |                                         |
| Fee Payments   |                                         |
|                |                                         |
+----------------+-----------------------------------------+
```

### 共通項目定義

| コンポーネント | 説明 |
|---|---|
| Header | システム名・ロゴを表示するグローバルヘッダー |
| Sidebar | 左側のナビゲーション。現在ページはアクティブスタイルで強調表示 |

### 共通イベント

| イベント名 | トリガー | 処理内容 |
|---|---|---|
| ページ遷移 | サイドバーの各メニュークリック | React Routerでページ切替 |

---

## ダッシュボード

**URL**: `/`

### 画面レイアウト

<!-- TODO: スクリーンショット配置予定 -->
ファイルパス: screenshots/dashboard.png
> **[スクリーンショット: ダッシュボード]**
![](./screenshots/common-layout.png)

### 概要

システムのトップページ。現時点では静的テキストのプレースホルダー（"Welcome to Syndicate Lending Management System"）が表示される。

---

## Borrower管理画面

**URL**: `/borrowers`

### 画面レイアウト（一覧モード）

<!-- TODO: スクリーンショット配置予定 -->
ファイルパス: screenshots/borrower-list.png
> **[スクリーンショット: Borrower一覧画面]**
![](./screenshots/borrower-list.png)


### 画面レイアウト（フォームモード）

<!-- TODO: スクリーンショット配置予定 -->
ファイルパス: screenshots/borrower-form.png
> **[スクリーンショット: Borrower登録・編集フォーム]**
![](./screenshots/borrower-form.png)

```
+--------------------------------------------------+
| Borrowers                                        |
+--------------------------------------------------+
| Company Name *  [________________________]       |
| Email Address * [________________________]       |
| Phone Number *  [________________________]       |
| Company ID      [________________________]       |
| Credit Limit *  [________________________] JPY   |
| Credit Rating * [▼ Select _______________]       |
|                                                  |
|         [Cancel]  [Register / Update]            |
+--------------------------------------------------+
```

### 項目定義

| ラベル | フィールド名 | 型 | 必須 | バリデーション |
|---|---|---|---|---|
| Company Name | name | text | ✅ | 必須入力 |
| Email Address | email | email | ✅ | メール形式 |
| Phone Number | phoneNumber | tel | ✅ | 必須入力 |
| Company ID | companyId | text | - | 任意 |
| Credit Limit | creditLimit | number | ✅ | 最小1、最大1,000,000,000（JPY） |
| Credit Rating | creditRating | select | ✅ | AAA/AA/A/BBB/BB/B/CCC/CC/C/D から選択 |

### 表示条件

| 要素 | 表示条件 |
|---|---|
| `New Borrower` ボタン | `showForm === false` のとき |
| 成功メッセージバナー | `successMessage !== null` のとき（3秒後に自動消去） |
| 登録・編集フォーム | `showForm === true` のとき |
| Borrower一覧テーブル | `showForm === false` のとき |
| `Register` ボタンラベル | `mode === 'create'` のとき |
| `Update` ボタンラベル | `mode === 'edit'` のとき |
| `Registering...` / `Updating...` | API呼び出し中 |

### イベント一覧

| イベントID | イベント名 | トリガー |
|---|---|---|
| B-01 | 新規登録フォーム表示 | `New Borrower` ボタンクリック |
| B-02 | Borrower登録 | フォームの `Register` ボタンクリック |
| B-03 | Borrower更新 | フォームの `Update` ボタンクリック |
| B-04 | フォームキャンセル | `Cancel` ボタンクリック |
| B-05 | Borrower編集開始 | テーブルの `Edit` ボタンクリック |
| B-06 | Borrower削除 | テーブルの `Delete` ボタンクリック |

### イベント詳細

#### B-01: 新規登録フォーム表示
- **前提条件**: 一覧表示モード
- **処理**: `showForm = true` に切替、フォームを空の状態で表示
- **遷移先**: フォーム表示モード（同一URL）

#### B-02: Borrower登録
- **前提条件**: フォーム表示モード（新規）、全必須項目入力済み
- **処理**: `POST /api/v1/parties/borrowers` にリクエスト送信
- **成功時**: 成功メッセージ表示 → 一覧リフレッシュ → 一覧モードに戻る
- **失敗時**: エラーメッセージ表示

#### B-03: Borrower更新
- **前提条件**: フォーム表示モード（編集）、全必須項目入力済み
- **処理**: `PUT /api/v1/parties/borrowers/{id}` にリクエスト送信（楽観的ロック版番号付き）
- **成功時**: 成功メッセージ表示 → 一覧リフレッシュ → 一覧モードに戻る

#### B-04: フォームキャンセル
- **処理**: `showForm = false`、`editMode/editData` をリセット
- **遷移先**: 一覧モード（変更なし）

#### B-05: Borrower編集開始
- **処理**: 対象Borrowerのデータをフォームにセット、`showForm = true`
- **遷移先**: フォーム表示モード（編集）

#### B-06: Borrower削除
- **処理**: 確認ダイアログ表示 → OK時に `DELETE /api/v1/parties/borrowers/{id}` 送信
- **成功時**: 成功メッセージ → 一覧リフレッシュ
- **制約**: Syndicate/Facilityに参加中のBorrowerは削除不可（API側でエラー返却）

---

## Investor管理画面

**URL**: `/lenders`

### 画面レイアウト（一覧モード）

<!-- TODO: スクリーンショット配置予定 -->
<!-- ファイルパス: screenshots/investor-list.png -->
> **[スクリーンショット: Investor一覧画面]**

### 画面レイアウト（フォームモード）

<!-- TODO: スクリーンショット配置予定 -->
<!-- ファイルパス: screenshots/investor-form.png -->
> **[スクリーンショット: Investor登録・編集フォーム]**

### 項目定義

| ラベル | フィールド名 | 型 | 必須 | バリデーション |
|---|---|---|---|---|
| Investor Name | name | text | ✅ | 必須入力 |
| Email Address | email | email | ✅ | メール形式 |
| Phone Number | phoneNumber | tel | ✅ | 必須入力 |
| Company ID | companyId | text | - | 任意 |
| Investment Capacity | investmentCapacity | number | ✅ | 最小1、最大10,000,000,000（JPY） |
| Investor Type | investorType | select | ✅ | BANK/INSURANCE/PENSION_FUND/HEDGE_FUND/OTHER から選択 |

### 表示条件

Borrower管理画面と同一パターン（showFormフラグによる切替）

### イベント一覧

| イベントID | イベント名 | トリガー |
|---|---|---|
| I-01 | 新規登録フォーム表示 | `New Investor` ボタンクリック |
| I-02 | Investor登録 | フォームの `Register` ボタンクリック |
| I-03 | Investor更新 | フォームの `Update` ボタンクリック |
| I-04 | フォームキャンセル | `Cancel` ボタンクリック |
| I-05 | Investor編集開始 | テーブルの `Edit` ボタンクリック |
| I-06 | Investor削除 | テーブルの `Delete` ボタンクリック |

### イベント詳細

#### I-02: Investor登録
- **処理**: `POST /api/v1/parties/investors` にリクエスト送信

#### I-03: Investor更新
- **処理**: `PUT /api/v1/parties/investors/{id}` にリクエスト送信（楽観的ロック版番号付き）

#### I-06: Investor削除
- **制約**: Syndicate/Facilityに参加中のInvestorは削除不可（API側でエラー返却）

---

## Syndicate管理画面

**URL**: `/syndicates`

### 画面レイアウト（一覧モード）

<!-- TODO: スクリーンショット配置予定 -->
<!-- ファイルパス: screenshots/syndicate-list.png -->
> **[スクリーンショット: Syndicate一覧画面]**

```
+----------------------------------------------------------+
| Syndicates       [Search: __________]  [New Syndicate]  |
|                  [Refresh]                               |
+----------------------------------------------------------+
| ID | 名前 | Borrower | Lead Bank | メンバー数 | 状態 | 操作 |
|----|------|----------|-----------|-----------|------|------|
| 1  | ...  | ...      | ...       | ...       | ...  | Edit / Delete |
+----------------------------------------------------------+
```

### 画面レイアウト（ウィザードフォーム）

<!-- TODO: スクリーンショット配置予定 -->
<!-- ファイルパス: screenshots/syndicate-form-step1.png -->
> **[スクリーンショット: Syndicateウィザード Step1 - 基本情報]**

<!-- ファイルパス: screenshots/syndicate-form-step2.png -->
> **[スクリーンショット: Syndicateウィザード Step2 - Borrower選択]**

<!-- ファイルパス: screenshots/syndicate-form-step3.png -->
> **[スクリーンショット: Syndicateウィザード Step3 - Lead Bank選択]**

<!-- ファイルパス: screenshots/syndicate-form-step4.png -->
> **[スクリーンショット: Syndicateウィザード Step4 - メンバー選択]**

<!-- ファイルパス: screenshots/syndicate-form-step5.png -->
> **[スクリーンショット: Syndicateウィザード Step5 - 確認]**

### 項目定義（ウィザード各ステップ）

**Step 1: Basic Information**

| ラベル | フィールド名 | 型 | 必須 |
|---|---|---|---|
| Syndicate Name | name | text | ✅ |

**Step 2: Borrower Selection**

| ラベル | フィールド名 | 型 | 必須 |
|---|---|---|---|
| Borrower | borrowerId | select（BorrowerSelectコンポーネント） | ✅ |

**Step 3: Lead Bank Selection**

| ラベル | フィールド名 | 型 | 必須 |
|---|---|---|---|
| Lead Bank | leadBankId | InvestorCardsコンポーネント（single-select） | ✅ |

> 選択されたLead BankはmemberInvestorIdsにも自動追加される。

**Step 4: Member Investor Selection**

| ラベル | フィールド名 | 型 | 必須 |
|---|---|---|---|
| Member Investors | memberInvestorIds | InvestorCardsコンポーネント（multi-select） | ✅（1件以上） |

**Step 5: Confirmation（読み取り専用）**

- Step1〜4で入力した内容を一覧表示して確認

### 表示条件

| 要素 | 表示条件 |
|---|---|
| 検索ボックス・テーブル | `showForm === false` のとき |
| ウィザードフォーム | `showForm === true` のとき |
| `Next` ボタン | 現ステップのバリデーション成功時のみ活性化 |
| ステップインジケーター | 完了済み: チェックマーク、現在: アクセントカラー、未到達: セカンダリカラー |

### イベント一覧

| イベントID | イベント名 | トリガー |
|---|---|---|
| SY-01 | ウィザードフォーム表示 | `New Syndicate` ボタンクリック |
| SY-02 | 次ステップへ進む | `Next` ボタンクリック |
| SY-03 | 前ステップへ戻る | `Back` ボタンクリック |
| SY-04 | Syndicate登録 | Step5の `Create Syndicate` ボタンクリック |
| SY-05 | Syndicate更新 | Step5の `Update Syndicate` ボタンクリック |
| SY-06 | フォームキャンセル | `Cancel` ボタンクリック |
| SY-07 | Syndicate編集開始 | テーブルの `Edit` ボタンクリック |
| SY-08 | Syndicate削除 | テーブルの `Delete` ボタンクリック |
| SY-09 | 一覧リフレッシュ | `Refresh` ボタンクリック |
| SY-10 | 検索フィルタリング | 検索ボックスへの入力（リアルタイム） |

### イベント詳細

#### SY-04: Syndicate登録
- **前提条件**: Step5（確認画面）表示中
- **処理**: `POST /api/v1/syndicates` にリクエスト送信
- **成功時**: 成功メッセージ → 一覧モード
- **副作用**: Borrower → RESTRICTED、全Investor → RESTRICTED、Syndicate → ACTIVE状態に遷移

#### SY-08: Syndicate削除
- **処理**: 確認ダイアログ → `DELETE /api/v1/syndicates/{id}` 送信
- **制約**: Facilityが存在するSyndicateは削除不可

---

## Facility管理画面

**URL**: `/facilities`

### 画面レイアウト（一覧モード）

<!-- TODO: スクリーンショット配置予定 -->
<!-- ファイルパス: screenshots/facility-list.png -->
> **[スクリーンショット: Facility一覧画面（Quick Stats付き）]**

```
+----------------------------------------------------------+
| Facilities    [Search: ________]    [New Facility]       |
|                                     [Refresh]            |
| [Draft: N件]  [Active: N件]  [Total: ¥XXX]              |
+----------------------------------------------------------+
| ID | Syndicate | 融資枠 | 通貨 | 開始日 | 終了日 | 状態 | 操作 |
|----|-----------|--------|------|--------|--------|------|------|
| 1  | ...       | ...    | ...  | ...    | ...    | ...  | Detail / Edit / Delete |
+----------------------------------------------------------+
```

### 画面レイアウト（ウィザードフォーム Step2）

<!-- TODO: スクリーンショット配置予定 -->
<!-- ファイルパス: screenshots/facility-form-step2.png -->
> **[スクリーンショット: Facilityウィザード Step2 - 基本情報入力（与信残高表示付き）]**

### 画面レイアウト（詳細モーダル）

<!-- TODO: スクリーンショット配置予定 -->
<!-- ファイルパス: screenshots/facility-detail-modal.png -->
> **[スクリーンショット: Facility詳細モーダル（持分配分表示）]**

### 項目定義（ウィザード各ステップ）

**Step 1: Syndicate Selection**

| ラベル | フィールド名 | 型 | 必須 | 備考 |
|---|---|---|---|---|
| Syndicate | syndicateId | SyndicateSelectコンポーネント | ✅ | 編集時はdisabled |

**Step 2: Facility Basic Information**

| ラベル | フィールド名 | 型 | 必須 | 備考 |
|---|---|---|---|---|
| Facility Amount | commitment | number | ✅ | BorrowerのcreditLimit・残高をインライン表示 |
| Currency | currency | select | ✅ | JPY/USD/EUR等 |
| Start Date | startDate | date | ✅ | |
| End Date | endDate | date | ✅ | |
| Interest Terms | interestTerms | text | ✅ | 例: LIBOR + 2% |

> `creditLimit - currentFacilityAmount` が正ならsuccess色、0以下ならwarning色で表示。

**Step 3: Investor Share Allocation（SharePieAllocationコンポーネント）**

| ラベル | フィールド名 | 型 | 必須 | バリデーション |
|---|---|---|---|---|
| 投資家持分（複数行） | sharePies[].investorId + share | 各行でInvestor選択 + 比率入力 | ✅ | 合計100%必須 |

**Step 4: Confirmation（読み取り専用）**

- 全入力内容を確認表示

### 詳細モーダル項目定義

| セクション | 項目 | 内容 |
|---|---|---|
| 基本情報 | Facility Amount | commitment（通貨フォーマット）+ currency |
| 基本情報 | Interest Terms | interestTerms |
| 基本情報 | Status | バッジ（DRAFT: 黄、FIXED: 緑） |
| 期間情報 | Start Date | startDate |
| 期間情報 | End Date | endDate |
| 期間情報 | Created At | createdAt |
| 期間情報 | Updated At | updatedAt |
| シンジケート情報 | Syndicate Name | syndicateDetail.name |
| シンジケート情報 | Borrower | syndicateDetail.borrowerName |
| シンジケート情報 | Lead Bank | syndicateDetail.leadBankName |
| 持分配分 | 各投資家行 | 投資家名・タイプ・投資能力・持分%・金額 |
| 持分配分 | 合計持分 | 100%ならsuccess色、それ以外warning色 |

### 表示条件

| 要素 | 表示条件 |
|---|---|
| Quick Stats | `showForm === false` のとき |
| 詳細モーダル | `showDetail === true && selectedFacility !== null` のとき |
| シンジケート情報セクション | `syndicateDetail !== null`（API取得成功時） |
| 持分配分セクション | `facility.sharePies.length > 0` のとき |

### イベント一覧

| イベントID | イベント名 | トリガー |
|---|---|---|
| FA-01 | ウィザードフォーム表示 | `New Facility` ボタンクリック |
| FA-02 | 次ステップへ進む | `Next` ボタンクリック |
| FA-03 | 前ステップへ戻る | `Back` ボタンクリック |
| FA-04 | Facility登録 | Step4の `Create Facility` ボタンクリック |
| FA-05 | Facility更新 | Step4の `Update Facility` ボタンクリック |
| FA-06 | フォームキャンセル | `Cancel` ボタンクリック |
| FA-07 | Facility詳細表示 | テーブルの `Detail` ボタンクリック |
| FA-08 | 詳細モーダルを閉じる | モーダルの `×` ボタンクリック |
| FA-09 | Facility編集開始 | テーブルの `Edit` ボタンクリック |
| FA-10 | Facility削除 | テーブルの `Delete` ボタンクリック |

### イベント詳細

#### FA-04: Facility登録
- **処理**: `POST /api/v1/facilities` にリクエスト送信
- **副作用**: Borrower → RESTRICTED、全Investor → RESTRICTED、Syndicate → ACTIVE状態に遷移

#### FA-10: Facility削除
- **処理**: 確認ダイアログ → `DELETE /api/v1/facilities/{id}` 送信
- **副作用（成功時）**: Syndicate → DRAFT、Borrower → ACTIVE、全Investor → ACTIVE状態に復旧
- **制約**: Drawdownが存在するFacilityは削除不可

---

## Drawdown管理画面

**URL**: `/drawdowns`

### 画面レイアウト（一覧モード）

<!-- TODO: スクリーンショット配置予定 -->
<!-- ファイルパス: screenshots/drawdown-list.png -->
> **[スクリーンショット: Drawdown一覧画面（Facility IDフィルター付き）]**

```
+----------------------------------------------------------+
| Drawdowns        [Search: ________]    [New Drawdown]    |
|                  [Facility Filter: ___] [Clear Filter]   |
|                  [Refresh]                               |
+----------------------------------------------------------+
| ID | 金額 | Loan ID | 目的 | 実行日 | 投資家配分 | 状態 | 操作 |
|----|------|---------|------|--------|-----------|------|------|
| 1  | ...  | #1      | ...  | ...    | ...       | ...  | Edit/Delete |
+----------------------------------------------------------+
```

### 画面レイアウト（ウィザードフォーム Step4）

<!-- TODO: スクリーンショット配置予定 -->
<!-- ファイルパス: screenshots/drawdown-form-step4.png -->
> **[スクリーンショット: Drawdownウィザード Step4 - 確認・実行（警告メッセージ付き）]**

### 項目定義（ウィザード各ステップ）

**Step 1: Facility Selection**（編集モードはスキップ）

| ラベル | フィールド名 | 型 | 必須 |
|---|---|---|---|
| Facility | facilityId | FacilitySelectForDrawdownコンポーネント | ✅ |

**Step 2: Basic Information**

| ラベル | フィールド名 | 型 | 必須 | 備考 |
|---|---|---|---|---|
| Drawdown Amount | amount | number | ✅ | 最大値 = 選択FacilityのcommitmentAmount |
| Drawdown Execution Date | drawdownDate | date | ✅ | min = 今日 |
| Drawdown Purpose | purpose | text | ✅ | クイック選択ボタン付き（Working Capital等） |

**Step 3: Repayment Terms（RepaymentTermsコンポーネント）**

| ラベル | フィールド名 | 型 | 必須 |
|---|---|---|---|
| Annual Interest Rate | annualInterestRate | number（step=0.01） | ✅ |
| Repayment Period | repaymentPeriodMonths | number（months） | ✅ |
| Repayment Cycle | repaymentCycle | select | ✅ |
| Repayment Method | repaymentMethod | select | ✅ |

**Step 4: Confirmation & Execution（読み取り専用）**

- 全入力内容を表示
- 警告メッセージ: "After drawdown execution, the facility will become fixed and no further modifications will be allowed."

### 表示条件

| 要素 | 表示条件 |
|---|---|
| フィルターパネル・テーブル | `showForm === false` のとき |
| `Clear Filter` ボタン | `facilityFilter !== undefined` のとき |
| テーブルの `Edit` ボタン | `status === 'DRAFT' || status === 'FAILED'` のとき |
| テーブルの `Delete` ボタン | `status === 'DRAFT' || status === 'FAILED'` のとき |
| AmountPie差額 | `amountPies.length > 0` のとき（差額 < 0.01 → success色、それ以外 → warning色） |
| ページネーション | `totalPages > 1` のとき |

### ステータスバッジ定義

| ステータス | 表示テキスト | 色 |
|---|---|---|
| DRAFT | Draft | warning（黄） |
| ACTIVE | Processing | accent（青） |
| COMPLETED | Completed | success（緑） |
| FAILED | Failed | error（赤） |
| CANCELLED | Cancelled | secondary（灰） |
| REFUNDED | Refunded | purple（紫） |

### イベント一覧

| イベントID | イベント名 | トリガー |
|---|---|---|
| D-01 | ウィザードフォーム表示 | `New Drawdown` ボタンクリック |
| D-02 | 次ステップへ進む | `Next` ボタンクリック |
| D-03 | 前ステップへ戻る | `Back` ボタンクリック |
| D-04 | Drawdown実行 | Step4の `Execute Drawdown` ボタンクリック |
| D-05 | Drawdown更新 | Step4の `Update Drawdown` ボタンクリック |
| D-06 | フォームキャンセル | `Cancel` ボタンクリック |
| D-07 | Drawdown編集開始 | テーブルの `Edit` ボタンクリック（DRAFT/FAILEDのみ） |
| D-08 | Drawdown削除 | テーブルの `Delete` ボタンクリック（DRAFT/FAILEDのみ） |
| D-09 | 詳細画面へ遷移 | テーブルの行クリック |
| D-10 | Facility IDフィルター | Facility IDフィルター入力 |
| D-11 | フィルタークリア | `Clear Filter` ボタンクリック |
| D-12 | 一覧リフレッシュ | `Refresh` ボタンクリック |

### イベント詳細

#### D-04: Drawdown実行
- **前提条件**: Step4表示中、Facilityが選択済み
- **処理**: `POST /api/v1/loans/drawdowns` にリクエスト送信
- **副作用（成功時）**:
  - Loan作成（返済スケジュール自動生成）
  - AmountPie生成（SharePie比率ベースで投資家別配分計算）
  - Facility → FIXED状態に遷移
  - 各InvestorのcurrentInvestmentAmount増加

#### D-09: 詳細画面へ遷移
- **処理**: `navigate('/drawdowns/${drawdown.id}')` でDrawdown詳細画面へ遷移

---

## Drawdown詳細画面

**URL**: `/drawdowns/:id`

### 画面レイアウト

<!-- TODO: スクリーンショット配置予定 -->
<!-- ファイルパス: screenshots/drawdown-detail.png -->
> **[スクリーンショット: Drawdown詳細画面（返済スケジュール・投資家配分付き）]**

```
+----------------------------------------------------------+
| ← Back to Drawdowns                                      |
| Drawdown #ID                                            |
+----------------------------------------------------------+
| Drawdown Information          | Investor Allocation      |
| Amount: ¥XXX                  | Investor #1: ¥XXX (X%)  |
| Loan ID: #X                   | Investor #2: ¥XXX (X%)  |
| Purpose: XXX                  | Total: ¥XXX              |
| Execution Date: XXXX-XX-XX    | Difference: ¥0.00 ✅    |
| Created Date: XXXX-XX-XX      |                          |
+-------------------------------+--------------------------+
| Related Loan Information      |                          |
| Loan Terms          | Repayment Terms               |
| Loan ID: #X         | Annual Rate: X%               |
| Status: ACTIVE ✅   | Period: XX months             |
| Principal: ¥XXX     | Cycle: MONTHLY                |
| Outstanding: ¥XXX   | Method: EQUAL_INSTALLMENT     |
+----------------------------------------------------------+
| Payment Schedule & History  [X scheduled, X completed]  |
| No. | Due Date | Principal | Interest | Total | Balance | Status | Action |
|-----|----------|-----------|----------|-------|---------|--------|--------|
| 1   | XXXX-XX-XX | ¥XXX   | ¥XXX     | ¥XXX  | ¥XXX    | PAID ✅ | Cancel |
| 2   | XXXX-XX-XX | ¥XXX   | ¥XXX     | ¥XXX  | ¥XXX    | PENDING⚠️ | Pay |
+----------------------------------------------------------+
```

### 項目定義

**Drawdown Informationセクション**

| ラベル | データソース | 型 |
|---|---|---|
| Amount | drawdown.amount | 通貨フォーマット |
| Loan ID | drawdown.loanId | `#${loanId}` |
| Purpose | drawdown.purpose | テキスト |
| Execution Date | drawdown.transactionDate | 日付 |
| Created Date | drawdown.createdAt | 日時 |

**Investor Allocationセクション**

| 項目 | データソース |
|---|---|
| 各投資家行 | amountPies（Investor #ID・金額・比率%） |
| Total Allocation | amountPies合計 |
| Difference | drawdown.amount - 合計（差額） |

**Related Loan Informationセクション**

| ラベル | データソース |
|---|---|
| Loan ID / Status | loan.id / loan.status |
| Principal Amount | loan.principalAmount |
| Outstanding Balance | loan.outstandingBalance |
| Annual Interest Rate | loan.annualInterestRate × 100（%表示） |
| Repayment Period | loan.repaymentPeriodMonths months |
| Repayment Cycle | loan.repaymentCycle |
| Repayment Method | loan.repaymentMethod |
| Drawdown Date | loan.drawdownDate |

**Payment Schedule & Historyセクション**

| 列名 | 内容 |
|---|---|
| Payment # | paymentNumber |
| Due Date | dueDate（実際の支払日も表示） |
| Principal | principalPayment |
| Interest | interestPayment |
| Total Payment | principal + interest |
| Remaining Balance | remainingBalance |
| Status | PENDING（黄） / PAID（緑） / OVERDUE（赤） |
| Action | Pay / Cancel / Completed / No Action |

### 表示条件

| 要素 | 表示条件 |
|---|---|
| ローディングスピナー | `loading === true` のとき |
| エラー画面 | `error !== null \|\| drawdown === null` のとき |
| Loan情報スピナー | `loanLoading === true` のとき |
| Loan Terms / Repayment Terms | `loan !== null` のとき |
| "Loan information not available" | `loan === null`（取得失敗）のとき |
| PaymentDetailTable | `paymentDetails.length > 0` のとき |
| 差額の色 | 差額 < 0.01 → success色、それ以外 → warning色 |
| Loanステータスバッジ | ACTIVE: success（緑）、DRAFT: warning（黄）、その他: secondary（灰） |

### イベント一覧

| イベントID | イベント名 | トリガー |
|---|---|---|
| DD-01 | 一覧画面へ戻る | `←` ボタン（ヘッダー左） |
| DD-02 | 返済実行 | `Pay` ボタンクリック |
| DD-03 | 支払い取り消し | `Cancel` ボタンクリック |
| DD-04 | エラー時に一覧へ戻る | エラー画面の `Back to Drawdowns` ボタン |

### イベント詳細

#### DD-02: 返済実行
- **表示条件**: paymentStatus が `PENDING` または `OVERDUE` の行
- **処理**: 確認ダイアログ → `POST /api/v1/loans/payments/scheduled/{paymentDetailId}` 送信
- **成功時**: 画面リフレッシュ（返済スケジュール・Loan残高更新）
- **副作用**:
  - PaymentDetail → PAID状態
  - Loanが初回返済なら DRAFT → ACTIVE に遷移
  - PaymentDistribution生成（投資家別配分）
  - Loanの outstandingBalance 減少
  - 元本返済分: InvestorのcurrentInvestmentAmount減少

#### DD-03: 支払い取り消し
- **表示条件**: paymentStatus が `PAID` かつ paymentId が存在する行
- **処理**: 確認ダイアログ → `DELETE /api/v1/loans/payments/{paymentId}/cancel` 送信
- **成功時**: 画面リフレッシュ
- **副作用**: PaymentDetail → PENDING状態に戻る、Loan残高を元に戻す

---

## Fee Payment管理画面

**URL**: `/fees`

### 画面レイアウト（一覧モード）

<!-- TODO: スクリーンショット配置予定 -->
<!-- ファイルパス: screenshots/fee-list.png -->
> **[スクリーンショット: Fee Payment一覧画面（統計ダッシュボード付き）]**

```
+----------------------------------------------------------+
| Fee Payments                          [New Fee Payment]  |
+----------------------------------------------------------+
| Filters: [Facility▼] [FeeType▼] [開始日] [終了日]       |
|          [Clear Filters]                                 |
+----------------------------------------------------------+
| Statistics（Facility選択時のみ表示）                      |
| Total: N件 | ¥XXX | Most Common: XXX | Types: N種        |
+----------------------------------------------------------+
| ID | FeeType | 金額 | 支払日 | 受取人 | Facility | 操作    |
|----|---------|------|--------|--------|---------|---------|
| 1  | ...     | ...  | ...    | ...    | ...     | View/Delete |
+----------------------------------------------------------+
| [< Prev] [1][2][3] [Next >]                             |
+----------------------------------------------------------+
```

### 画面レイアウト（登録フォーム）

<!-- TODO: スクリーンショット配置予定 -->
<!-- ファイルパス: screenshots/fee-form.png -->
> **[スクリーンショット: Fee Payment登録フォーム（手数料タイプ選択カード・自動計算付き）]**

### 項目定義

**フィルターパネル（一覧モード）**

| ラベル | フィールド名 | 型 | 説明 |
|---|---|---|---|
| Facility | facilityFilter | select | 全Facilityリストから選択（"All Facilities"含む） |
| Fee Type | feeTypeFilter | select | FeeTypeリストから選択（"All Types"含む） |
| Start Date | startDate | date | 日付範囲フィルター開始 |
| End Date | endDate | date | 日付範囲フィルター終了 |

**登録フォーム**

| ラベル | フィールド名 | 型 | 必須 | 備考 |
|---|---|---|---|---|
| Facility | facilityId | select | ✅ | 選択時にcurrency・calculationBaseを自動セット |
| Fee Type | feeType | radio（カード形式） | ✅ | 7種類（MANAGEMENT/ARRANGEMENT/COMMITMENT/TRANSACTION/LATE/AGENT/OTHER） |
| Recipient Type | recipientType | select | ✅ | FeeTypeにより自動決定または手動選択 |
| Recipient | recipientId | select | 条件付き | recipientType=INVESTORのときのみ表示 |
| Calculation Base | calculationBase | number | - | Facility選択時にcommitmentが自動セット |
| Fee Rate (%) | feeRate | number | - | FeeTypeによりデフォルト値が自動セット |
| Fee Amount | feeAmount | number | ✅ | calculationBase × feeRate/100 で自動計算 |
| Fee Date | feeDate | date | ✅ | 最大値 = 今日 |
| Currency | currency | text（readOnly） | ✅ | Facility選択時に自動セット |
| Description | description | textarea | ✅ | 3行 |

### 表示条件

| 要素 | 表示条件 |
|---|---|
| Statistics Panel | `selectedFacility !== null && statistics !== null` のとき |
| フィルター・テーブル | `showForm === false` のとき |
| 登録フォーム | `showForm === true` のとき |
| Borrower Informationセクション | `facilityId > 0`（Facility選択後）のとき |
| 受取人選択UI（手動） | `isRecipientAutomatic(feeType) === false` のとき |
| 受取人自動表示UI | `isRecipientAutomatic(feeType) === true` のとき |
| 自動計算プレビューバナー | `calculationBase > 0 && feeRate > 0` のとき |
| Recipient select（投資家リスト） | `recipientType === 'INVESTOR'` のとき |
| ページネーション | `totalPages > 1` のとき |

### 受取人自動決定ロジック

| FeeType | 受取人タイプ | 手動/自動 |
|---|---|---|
| MANAGEMENT_FEE | LEAD_BANK | 自動 |
| ARRANGEMENT_FEE | LEAD_BANK | 自動 |
| COMMITMENT_FEE | AUTO_DISTRIBUTE（投資家配分） | 自動 |
| LATE_FEE | AUTO_DISTRIBUTE（投資家配分） | 自動 |
| TRANSACTION_FEE | AGENT_BANK | 自動 |
| AGENT_FEE | AGENT_BANK | 自動 |
| OTHER_FEE | 手動選択 | 手動 |

### イベント一覧

| イベントID | イベント名 | トリガー |
|---|---|---|
| FP-01 | 登録フォーム表示 | `New Fee Payment` ボタンクリック |
| FP-02 | 一覧に戻る | `Back to List` ボタンクリック |
| FP-03 | Fee Payment登録 | フォームの `Create Fee Payment` ボタンクリック |
| FP-04 | フォームキャンセル | `Cancel` ボタンクリック |
| FP-05 | Facilityフィルター | Facilityドロップダウン変更 |
| FP-06 | FeeTypeフィルター | FeeTypeドロップダウン変更 |
| FP-07 | 日付範囲フィルター | Start Date / End Date入力 |
| FP-08 | フィルタークリア | `Clear Filters` ボタンクリック |
| FP-09 | Fee Payment詳細表示 | `View Details` ボタンクリック |
| FP-10 | Fee Payment削除 | `Delete` ボタンクリック |
| FP-11 | ページ切替 | ページネーションボタンクリック |

### イベント詳細

#### FP-03: Fee Payment登録
- **前提条件**: 全必須項目入力済み、手数料額整合性チェック通過
- **処理**: `POST /api/v1/fees/payments` にリクエスト送信
- **副作用**:
  - FeeDistribution自動生成（FeeTypeに応じた配分先へ）
  - COMMITMENT_FEE/LATE_FEE: 投資家へ持分比率ベースで配分

#### FP-05: Facilityフィルター
- **処理**: 選択Facility → Statistics APIも同時取得 (`GET /api/v1/fees/payments/facility/{facilityId}/statistics`)
- **フィルター優先順位**: 日付範囲 > Facility > FeeType > 全件

#### FP-09: Fee Payment詳細表示
- **処理**: alertダイアログで詳細情報を表示（モーダル未実装）

---

## スクリーンショット配置ガイド

スクリーンショット取得後、以下のパスに配置してください：

| ファイルパス | 対応画面 |
|---|---|
| `screenshots/common-layout.png` | 共通レイアウト |
| `screenshots/dashboard.png` | ダッシュボード |
| `screenshots/borrower-list.png` | Borrower一覧 |
| `screenshots/borrower-form.png` | Borrower登録フォーム |
| `screenshots/investor-list.png` | Investor一覧 |
| `screenshots/investor-form.png` | Investor登録フォーム |
| `screenshots/syndicate-list.png` | Syndicate一覧 |
| `screenshots/syndicate-form-step1.png` | Syndicateウィザード Step1 |
| `screenshots/syndicate-form-step2.png` | Syndicateウィザード Step2 |
| `screenshots/syndicate-form-step3.png` | Syndicateウィザード Step3 |
| `screenshots/syndicate-form-step4.png` | Syndicateウィザード Step4 |
| `screenshots/syndicate-form-step5.png` | Syndicateウィザード Step5 |
| `screenshots/facility-list.png` | Facility一覧 |
| `screenshots/facility-form-step2.png` | Facilityウィザード Step2 |
| `screenshots/facility-detail-modal.png` | Facility詳細モーダル |
| `screenshots/drawdown-list.png` | Drawdown一覧 |
| `screenshots/drawdown-form-step4.png` | Drawdownウィザード Step4 |
| `screenshots/drawdown-detail.png` | Drawdown詳細 |
| `screenshots/fee-list.png` | Fee Payment一覧 |
| `screenshots/fee-form.png` | Fee Payment登録フォーム |
