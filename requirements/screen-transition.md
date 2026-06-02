# 画面遷移図

シンジケートローン管理システムの画面遷移。

---

## 全体画面遷移図

```mermaid
graph TD
    START([アプリ起動]) --> DASH[ダッシュボード\n/]

    DASH --> NAV_B[サイドバー: Borrowers]
    DASH --> NAV_I[サイドバー: Investors]
    DASH --> NAV_S[サイドバー: Syndicates]
    DASH --> NAV_F[サイドバー: Facilities]
    DASH --> NAV_D[サイドバー: Drawdowns]
    DASH --> NAV_FEE[サイドバー: Fee Payments]

    NAV_B --> BORROWER[Borrower管理画面\n/borrowers]
    NAV_I --> INVESTOR[Investor管理画面\n/lenders]
    NAV_S --> SYNDICATE[Syndicate管理画面\n/syndicates]
    NAV_F --> FACILITY[Facility管理画面\n/facilities]
    NAV_D --> DRAWDOWN[Drawdown管理画面\n/drawdowns]
    NAV_FEE --> FEE[Fee Payment管理画面\n/fees]

    DRAWDOWN -->|行クリック| DRAWDOWN_DETAIL[Drawdown詳細画面\n/drawdowns/:id]
    DRAWDOWN_DETAIL -->|Back to Drawdownsボタン| DRAWDOWN
```

---

## ページ内遷移詳細

### Borrower管理画面（/borrowers）

```mermaid
stateDiagram-v2
    direction TB
    [*] --> 一覧表示 : 画面アクセス

    一覧表示 --> フォーム表示_新規 : New Borrowerボタン
    フォーム表示_新規 --> 一覧表示 : Cancelボタン
    フォーム表示_新規 --> 一覧表示 : Register成功

    一覧表示 --> フォーム表示_編集 : Editボタン（テーブル行）
    フォーム表示_編集 --> 一覧表示 : Cancelボタン
    フォーム表示_編集 --> 一覧表示 : Update成功

    一覧表示 --> 一覧表示 : Deleteボタン（確認ダイアログ → 削除）
```

### Investor管理画面（/lenders）

```mermaid
stateDiagram-v2
    direction TB
    [*] --> 一覧表示 : 画面アクセス

    一覧表示 --> フォーム表示_新規 : New Investorボタン
    フォーム表示_新規 --> 一覧表示 : Cancelボタン
    フォーム表示_新規 --> 一覧表示 : Register成功

    一覧表示 --> フォーム表示_編集 : Editボタン
    フォーム表示_編集 --> 一覧表示 : Cancelボタン
    フォーム表示_編集 --> 一覧表示 : Update成功

    一覧表示 --> 一覧表示 : Deleteボタン（確認ダイアログ → 削除）
```

### Syndicate管理画面（/syndicates）

```mermaid
stateDiagram-v2
    direction TB
    [*] --> 一覧表示 : 画面アクセス

    一覧表示 --> Step1_基本情報 : New Syndicateボタン
    Step1_基本情報 --> Step2_Borrower選択 : Nextボタン
    Step2_Borrower選択 --> Step1_基本情報 : Backボタン
    Step2_Borrower選択 --> Step3_リードバンク選択 : Nextボタン
    Step3_リードバンク選択 --> Step2_Borrower選択 : Backボタン
    Step3_リードバンク選択 --> Step4_メンバー選択 : Nextボタン
    Step4_メンバー選択 --> Step3_リードバンク選択 : Backボタン
    Step4_メンバー選択 --> Step5_確認 : Nextボタン
    Step5_確認 --> Step4_メンバー選択 : Backボタン
    Step5_確認 --> 一覧表示 : Create Syndicateボタン（登録成功）
    Step5_確認 --> 一覧表示 : Cancelボタン

    一覧表示 --> Step1_基本情報_編集 : Editボタン（Step2から開始）
    Step1_基本情報_編集 --> 一覧表示 : Update Syndicate成功

    一覧表示 --> 一覧表示 : Deleteボタン（確認ダイアログ → 削除）
```

### Facility管理画面（/facilities）

```mermaid
stateDiagram-v2
    direction TB
    [*] --> 一覧表示 : 画面アクセス

    一覧表示 --> Step1_Syndicate選択 : New Facilityボタン
    Step1_Syndicate選択 --> Step2_基本情報 : Nextボタン
    Step2_基本情報 --> Step1_Syndicate選択 : Backボタン
    Step2_基本情報 --> Step3_持分配分 : Nextボタン
    Step3_持分配分 --> Step2_基本情報 : Backボタン
    Step3_持分配分 --> Step4_確認 : Nextボタン
    Step4_確認 --> Step3_持分配分 : Backボタン
    Step4_確認 --> 一覧表示 : Create Facilityボタン（登録成功）
    Step4_確認 --> 一覧表示 : Cancelボタン

    一覧表示 --> モーダル表示 : 詳細表示ボタン
    モーダル表示 --> 一覧表示 : ×ボタン

    一覧表示 --> 一覧表示 : Deleteボタン（確認ダイアログ → 削除）
```

### Drawdown管理画面（/drawdowns）

```mermaid
stateDiagram-v2
    direction TB
    [*] --> 一覧表示 : 画面アクセス

    一覧表示 --> Step1_Facility選択 : New Drawdownボタン
    Step1_Facility選択 --> Step2_基本情報 : Nextボタン
    Step2_基本情報 --> Step1_Facility選択 : Backボタン
    Step2_基本情報 --> Step3_返済条件 : Nextボタン
    Step3_返済条件 --> Step2_基本情報 : Backボタン
    Step3_返済条件 --> Step4_確認実行 : Nextボタン
    Step4_確認実行 --> Step3_返済条件 : Backボタン
    Step4_確認実行 --> 一覧表示 : Execute Drawdownボタン（実行成功）
    Step4_確認実行 --> 一覧表示 : Cancelボタン

    一覧表示 --> Step2_基本情報_編集 : Editボタン（DRAFT/FAILEDのみ\nStep1をスキップ）
    Step2_基本情報_編集 --> 一覧表示 : Update Drawdown成功

    一覧表示 --> 一覧表示 : Deleteボタン（DRAFT/FAILEDのみ\n確認ダイアログ → 削除）

    一覧表示 --> 詳細画面 : 行クリック → /drawdowns/:id
```

### Drawdown詳細画面（/drawdowns/:id）

```mermaid
stateDiagram-v2
    direction TB
    [*] --> データ読み込み中 : 画面アクセス
    データ読み込み中 --> 詳細表示 : データ取得成功
    データ読み込み中 --> エラー表示 : データ取得失敗

    詳細表示 --> 一覧画面 : Backボタン（← アイコン）
    エラー表示 --> 一覧画面 : Back to Drawdownsボタン

    詳細表示 --> 詳細表示 : Payボタン（支払い実行 → 画面リフレッシュ）
    詳細表示 --> 詳細表示 : Cancelボタン（支払い取り消し → 画面リフレッシュ）
```

### Fee Payment管理画面（/fees）

```mermaid
stateDiagram-v2
    direction TB
    [*] --> 一覧表示 : 画面アクセス

    一覧表示 --> フォーム表示 : New Fee Paymentボタン
    フォーム表示 --> 一覧表示 : Back to Listボタン
    フォーム表示 --> 一覧表示 : Create Fee Payment成功

    一覧表示 --> 一覧表示 : Deleteボタン（確認 → 削除）
    一覧表示 --> 一覧表示 : フィルター変更（Facility/FeeType/日付範囲）
```

---

## 画面間の遷移サマリー

| 遷移元 | 遷移先 | トリガー |
|---|---|---|
| サイドバー | Borrower管理画面 | "Borrowers" クリック |
| サイドバー | Investor管理画面 | "Investors" クリック |
| サイドバー | Syndicate管理画面 | "Syndicates" クリック |
| サイドバー | Facility管理画面 | "Facilities" クリック |
| サイドバー | Drawdown管理画面 | "Drawdowns" クリック |
| サイドバー | Fee Payment管理画面 | "Fee Payments" クリック |
| Drawdown管理画面 | Drawdown詳細画面 | テーブル行クリック |
| Drawdown詳細画面 | Drawdown管理画面 | Back ボタン |

> **注**: Borrower/Investor/Syndicate/Facility/Fee Payment画面はページ内遷移（フォーム表示切替）のみで、別URLへの遷移は発生しない。
