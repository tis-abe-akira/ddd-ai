# 業務フロー

シンジケートローン管理システムの主要業務フロー（スイムレーン付き）。

---

## 1. シンジケートローン組成フロー

```mermaid
flowchart TD
    subgraph Borrower["🏢 借り手（Borrower）"]
        B1([融資申請])
        B2[借り手情報を提出]
    end

    subgraph LeadBank["🏦 リードバンク（Lead Bank）"]
        L1[Borrower登録]
        L2[Investor登録]
        L3[Syndicate作成\n- Borrower選択\n- Lead Bank選択\n- メンバーInvestor選択]
        L4[Facility作成\n- 融資枠金額設定\n- 期間・金利条件設定]
        L5[持分比率設定\n SharePie 合計100%]
        L6{SharePie\n合計100%?}
        L7([Facility確定\nFacilityステータス: DRAFT])
    end

    subgraph MemberInvestors["💼 メンバー投資家（Investors）"]
        M1[投資枠確認・同意]
    end

    subgraph System["⚙️ システム"]
        S1[Borrower: ACTIVE状態に遷移]
        S2[Investor: ACTIVE状態に遷移]
        S3[Syndicate: DRAFT状態で作成]
        S4[Facility: DRAFT状態で作成\nBorrower → RESTRICTED\nInvestor → RESTRICTED\nSyndicate → ACTIVE]
    end

    B1 --> B2
    B2 --> L1
    L1 --> S1
    L1 --> L2
    L2 --> S2
    S2 --> M1
    M1 --> L3
    L3 --> S3
    S3 --> L4
    L4 --> L5
    L5 --> L6
    L6 -->|No| L5
    L6 -->|Yes| S4
    S4 --> L7
```

---

## 2. ドローダウン実行フロー

```mermaid
flowchart TD
    subgraph Borrower["🏢 借り手（Borrower）"]
        B1([資金調達要求])
        B2[ドローダウン申請\n- 金額\n- 目的\n- 実行日]
    end

    subgraph LeadBank["🏦 リードバンク（Lead Bank）"]
        L1[Facility選択]
        L2[ドローダウン情報入力\n- 金額\n- 目的\n- 実行日]
        L3[返済条件設定\n- 年利\n- 返済期間\n- 返済サイクル\n- 返済方式]
        L4[内容確認・実行]
        L5([ドローダウン完了])
    end

    subgraph System["⚙️ システム"]
        S1{利用可能額\nチェック}
        S2[Drawdown作成]
        S3[Loan作成\n- 元本・利率・期間設定]
        S4[返済スケジュール自動生成\nPaymentDetail×返済期間分]
        S5[投資家別配分額計算\nAmountPie生成\n- SharePie比率ベース]
        S6[FacilityをFIXED状態に遷移]
        S7[InvestorのcurrentInvestmentAmount増加]
        S8{利用可能額\n超過?}
    end

    B1 --> B2
    B2 --> L1
    L1 --> L2
    L2 --> L3
    L3 --> L4
    L4 --> S1
    S1 --> S8
    S8 -->|Yes| ERR1[エラー: 利用可能額超過]
    S8 -->|No| S2
    S2 --> S3
    S3 --> S4
    S4 --> S5
    S5 --> S6
    S6 --> S7
    S7 --> L5
```

---

## 3. 返済実行フロー

```mermaid
flowchart TD
    subgraph Borrower["🏢 借り手（Borrower）"]
        B1([返済実行])
    end

    subgraph LeadBank["🏦 リードバンク（Lead Bank）"]
        L1[Drawdown詳細画面を開く]
        L2[返済スケジュール確認]
        L3[Payボタンをクリック]
        L4[支払い確認ダイアログ]
        L5([返済完了])
    end

    subgraph System["⚙️ システム"]
        S1[Payment作成\n- 元本・利息・合計金額]
        S2[PaymentDetail状態更新\nPENDING → PAID\n実際の支払い日を記録]
        S3{初回返済?}
        S4[LoanをACTIVE状態に遷移\nDRAFT → ACTIVE]
        S5[投資家別配分額計算\nPaymentDistribution生成\n- SharePie比率ベース]
        S6[Loanの残高更新\noutstandingBalance減少]
        S7[InvestorのcurrentInvestmentAmount減少\n※元本返済分のみ]
        S8{全額返済\n完了?}
        S9[LoanをCOMPLETED状態に遷移]
    end

    B1 --> L1
    L1 --> L2
    L2 --> L3
    L3 --> L4
    L4 -->|確認OK| S1
    L4 -->|キャンセル| L2
    S1 --> S2
    S2 --> S3
    S3 -->|Yes| S4
    S3 -->|No| S5
    S4 --> S5
    S5 --> S6
    S6 --> S7
    S7 --> S8
    S8 -->|Yes| S9
    S9 --> L5
    S8 -->|No| L5
```

---

## 4. 支払い取り消しフロー

```mermaid
flowchart TD
    subgraph LeadBank["🏦 リードバンク（Lead Bank）"]
        L1[Drawdown詳細画面で\n誤った支払いを確認]
        L2[Cancelボタンをクリック]
        L3[取り消し確認ダイアログ]
        L4([取り消し完了])
    end

    subgraph System["⚙️ システム"]
        S1{PaymentがCOMPLETED\n状態?}
        S2[Payment状態をCANCELLEDに変更]
        S3[PaymentDetail状態をPENDINGに戻す]
        S4[Loanの残高を戻す\noutstandingBalance増加]
        S5[InvestorのcurrentInvestmentAmountを戻す]
        ERR1[エラー: 取り消し不可]
    end

    L1 --> L2
    L2 --> L3
    L3 -->|確認OK| S1
    L3 -->|キャンセル| L1
    S1 -->|Yes| S2
    S1 -->|No| ERR1
    S2 --> S3
    S3 --> S4
    S4 --> S5
    S5 --> L4
```

---

## 5. 手数料支払いフロー

```mermaid
flowchart TD
    subgraph Borrower["🏢 借り手（Borrower）"]
        B1([手数料支払い義務発生])
    end

    subgraph LeadBank["🏦 リードバンク（Lead Bank）"]
        L1[Fee Payment画面を開く]
        L2[Facilityを選択]
        L3[手数料タイプを選択\n7種類から選択]
        L4[計算基準額・手数料率を入力\n手数料額が自動計算]
        L5[受取人・日付・説明を入力]
        L6[Create Fee Paymentをクリック]
        L7([手数料支払い完了])
    end

    subgraph System["⚙️ システム"]
        S1[FeePayment作成]
        S2{手数料タイプ\n判定}
        S3a[リードバンクへ収益\nLEAD_BANK]
        S3b[エージェントバンクへ収益\nAGENT_BANK]
        S3c[投資家へ配分\n持分比率ベース\nFeeDistribution生成]
        S4[手数料額整合性チェック\n計算基準額×手数料率\n= 手数料額]
        ERR1[エラー: 整合性不一致]
    end

    B1 --> L1
    L1 --> L2
    L2 --> L3
    L3 --> L4
    L4 --> L5
    L5 --> L6
    L6 --> S4
    S4 -->|不一致| ERR1
    S4 -->|一致| S1
    S1 --> S2
    S2 -->|MANAGEMENT/ARRANGEMENT| S3a
    S2 -->|TRANSACTION/AGENT| S3b
    S2 -->|COMMITMENT/LATE| S3c
    S3a --> L7
    S3b --> L7
    S3c --> L7
```

---

## 6. Facility削除フロー（状態復旧）

```mermaid
flowchart TD
    subgraph LeadBank["🏦 リードバンク（Lead Bank）"]
        L1[Facility一覧でDeleteボタン]
        L2[削除確認ダイアログ]
        L3([Facility削除完了])
    end

    subgraph System["⚙️ システム"]
        S1{削除可否\n検証}
        S2[状態復旧処理]
        S3[Syndicate: ACTIVE → DRAFT]
        S4[Borrower: RESTRICTED → ACTIVE]
        S5[Investor: RESTRICTED → ACTIVE\n全メンバー分]
        S6[SharePie削除\n関連データ削除]
        S7[Facility物理削除]
        ERR1[エラー: \nDrawdownが存在するため削除不可]
    end

    L1 --> L2
    L2 -->|確認OK| S1
    L2 -->|キャンセル| L1
    S1 -->|Drawdownあり| ERR1
    S1 -->|削除可| S2
    S2 --> S3
    S3 --> S4
    S4 --> S5
    S5 --> S6
    S6 --> S7
    S7 --> L3
```
