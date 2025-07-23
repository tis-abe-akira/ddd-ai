# シンジケートローン管理システム - 簡易データモデル

このドキュメントでは、シンジケートローン管理システムの実装済みデータモデルを簡潔に示します。

## 概要

シンジケートローンは **複数の金融機関が協調して単一企業に大規模融資を行う仕組み** です。このシステムは以下の主要な特徴を持ちます：

- **統一Transaction基盤**: 全取引タイプ（Drawdown, Payment, FeePayment）が継承階層で管理
- **状態管理**: Spring State Machineによる包括的ライフサイクル制御
- **持分・配分管理**: SharePie（比率）→ AmountPie/Distribution（金額）の二層構造
- **投資額追跡**: 投資家の現在投資額を自動管理
- **金融計算**: Money/Percentage値オブジェクトによる精密計算

## データモデル

```mermaid
erDiagram
    %% 参加者管理
    Borrower {
        Long id PK
        String name
        Money creditLimit
        BorrowerState status
    }
    
    Investor {
        Long id PK
        String name
        Money currentInvestmentAmount
        InvestorState status
        InvestorType type
    }
    
    Syndicate {
        Long id PK
        String name
        Long leadBankId FK
        Long borrowerId FK
    }

    %% 融資枠・持分管理
    Facility {
        Long id PK
        Long syndicateId FK
        Money commitment
        FacilityState status
        LocalDate startDate
        LocalDate endDate
    }
    
    SharePie {
        Long id PK
        Long facilityId FK
        Long investorId FK
        Percentage share
    }

    %% 取引基盤（統一Transaction階層）
    Transaction {
        Long id PK
        TransactionType type
        TransactionStatus status
        Money amount
        LocalDate transactionDate
    }
    
    Drawdown {
        Long id PK
        Long facilityId FK
        String purpose
    }
    
    Payment {
        Long id PK
        Long loanId FK
        Money principalAmount
        Money interestAmount
    }
    
    FeePayment {
        Long id PK
        Long facilityId FK
        FeeType feeType
        Money calculationBase
        Double feeRate
    }

    %% ローン・配分管理
    Loan {
        Long id PK
        Long facilityId FK
        Money principalAmount
        Money outstandingBalance
        LoanState status
        RepaymentMethod repaymentMethod
    }
    
    AmountPie {
        Long id PK
        Long drawdownId FK
        Long investorId FK
        Money amount
    }
    
    PaymentDistribution {
        Long id PK
        Long paymentId FK
        Long investorId FK
        Money principalAmount
        Money interestAmount
    }
    
    FeeDistribution {
        Long id PK
        Long feePaymentId FK
        Long investorId FK
        Money distributionAmount
    }

    %% リレーションシップ
    Syndicate ||--|| Borrower : "借り手"
    Syndicate ||--|| Investor : "リード銀行"
    Syndicate ||--o{ Investor : "参加投資家"
    
    Facility ||--|| Syndicate : "belongs to"
    SharePie }|--|| Facility : "持分定義"
    SharePie }|--|| Investor : "投資家持分"
    
    Transaction ||--o| Drawdown : "継承"
    Transaction ||--o| Payment : "継承"  
    Transaction ||--o| FeePayment : "継承"
    
    Drawdown ||--|| Loan : "creates"
    Drawdown ||--o{ AmountPie : "投資家別配分"
    
    Payment ||--o{ PaymentDistribution : "返済配分"
    FeePayment ||--o{ FeeDistribution : "手数料配分"
    
    AmountPie }|--|| Investor : "配分先"
    PaymentDistribution }|--|| Investor : "返済先"
    FeeDistribution }|--|| Investor : "手数料配分先"
```

## 主要エンティティ説明

### 参加者管理
- **Borrower**: 融資を受ける企業、信用限度額と状態管理
- **Investor**: 資金提供する金融機関、現在投資額を自動追跡
- **Syndicate**: 特定融資のための投資家グループ

### 融資枠管理
- **Facility**: 借り手が利用可能な融資枠
- **SharePie**: 各投資家の持分比率（合計100%必須）

### 取引管理（統一基盤）
- **Transaction**: 全取引の基底クラス、JPA JOINED継承
- **Drawdown**: 融資枠からの資金引き出し
- **Payment**: 元本・利息の返済
- **FeePayment**: 各種手数料支払い（7種類対応）

### ローン・配分管理
- **Loan**: ドローダウンで生成される融資残高
- **AmountPie**: ドローダウン時の投資家別金額配分
- **PaymentDistribution**: 返済時の投資家別配分
- **FeeDistribution**: 手数料の投資家別配分

## 主要な業務フロー

1. **シンジケート組成**: Borrower + 複数Investor → Syndicate
2. **融資枠設定**: SharePieで投資家持分比率を設定 → Facility
3. **資金引き出し**: Drawdown → Loan生成 + AmountPie配分
4. **返済処理**: Payment → PaymentDistribution配分
5. **手数料処理**: FeePayment → FeeDistribution配分

## 状態管理

各エンティティはSpring State Machineで制御：

- **Facility**: DRAFT → ACTIVE → COMPLETED
- **Borrower/Investor**: DRAFT → ACTIVE → RESTRICTED
- **Transaction**: DRAFT → ACTIVE → COMPLETED（取り消し可能）
- **Loan**: DRAFT → ACTIVE → OVERDUE → COMPLETED

## Value Objects

- **Money**: BigDecimalベースの精密金融計算
- **Percentage**: 持分比率の正確な管理と計算

## 実装状況

✅ **完全実装済み**: 全コアエンティティ、統一Transaction基盤、状態管理、配分計算、REST API  
🔄 **将来拡張**: 投資家間取引（FacilityTrade）、追加手数料タイプ

---

**作成日**: 2025-07-23  
**実装完了度**: 95%（コア機能完全実装済み）