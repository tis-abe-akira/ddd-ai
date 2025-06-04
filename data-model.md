# シンジケートローン管理システム - データモデル

このドキュメントでは、シンジケートローン管理システムのデータモデルをMermaid形式で記述します。現在実装済みのエンティティと将来実装予定の概念レベルのモデルを含みます。

## 1. 現在実装済みデータモデル

### 1.1 メインエンティティ関係図

```mermaid
erDiagram
    Company {
        Long id PK
        String companyName
        String registrationNumber
        Industry industry
        Country country
        String address
    }

    Borrower {
        Long id PK
        String name
        String email
        String phoneNumber
        String companyId
        Money creditLimit
        CreditRating creditRating
    }

    Investor {
        Long id PK
        String name
        String email
        String phoneNumber
        String companyId
        BigDecimal investmentCapacity
        InvestorType investorType
        Boolean isActive
    }

    Syndicate {
        Long id PK
        String name
        Long leadBankId FK
        Long borrowerId FK
        List-Long- memberInvestorIds
    }

    Facility {
        Long id PK
        Long syndicateId FK
        Money commitment
        String currency
        LocalDate startDate
        LocalDate endDate
        String interestTerms
    }

    SharePie {
        Long id PK
        Long investorId FK
        Percentage share
        Long facilityId FK
    }

    Transaction {
        Long id PK
        Long facilityId FK
        Long borrowerId FK
        LocalDate transactionDate
        String transactionType
        Money amount
    }

    FacilityInvestment {
        Long id PK
        Long facilityId FK
        Long investorId FK
        Long borrowerId FK
        LocalDate transactionDate
        String transactionType
        Money amount
    }

    %% 関係性
    Syndicate ||--|| Borrower : "has borrower"
    Syndicate ||--|| Investor : "has lead bank"
    Syndicate ||--o{ Investor : "has members"
    Facility ||--|| Syndicate : "belongs to"
    SharePie }|--|| Facility : "defines shares for"
    SharePie }|--|| Investor : "investor share"
    Transaction ||--|| Facility : "related to"
    Transaction ||--|| Borrower : "involves"
    FacilityInvestment ||--|| Transaction : "is-a"
    FacilityInvestment ||--|| Investor : "made by"
```

### 1.2 Value Objects

```mermaid
classDiagram
    class Money {
        -BigDecimal amount
        +of(BigDecimal) Money
        +of(long) Money
        +zero() Money
        +add(Money) Money
        +subtract(Money) Money
        +multiply(BigDecimal) Money
        +isGreaterThan(Money) boolean
        +isZero() boolean
    }

    class Percentage {
        -BigDecimal value
        +of(BigDecimal) Percentage
        +of(int) Percentage
        +applyTo(Money) Money
        +add(Percentage) Percentage
        +getValue() BigDecimal
    }
```

### 1.3 列挙型定義

```mermaid
classDiagram
    class Country {
        <<enumeration>>
        JAPAN
        USA
        UK
        GERMANY
        FRANCE
        CHINA
        INDIA
        AUSTRALIA
        CANADA
        OTHER
    }

    class Industry {
        <<enumeration>>
        FINANCE
        MANUFACTURING
        IT
        RETAIL
        ENERGY
        TRANSPORTATION
        HEALTHCARE
        CONSTRUCTION
        AGRICULTURE
        OTHER
    }

    class CreditRating {
        <<enumeration>>
        AAA
        AA
        A
        BBB
        BB
        B
        CCC
        CC
        C
        D
        +getLimit() Money
        +isLimitSatisfied(Money) boolean
    }

    class InvestorType {
        <<enumeration>>
        LEAD_BANK
        BANK
        INSURANCE
        FUND
        CORPORATE
        INDIVIDUAL
        GOVERNMENT
        PENSION
        SOVEREIGN_FUND
        CREDIT_UNION
        OTHER
    }
```

## 2. 将来実装予定データモデル（概念レベル）

### 2.1 Position階層とTransaction階層

```mermaid
erDiagram
    %% Position階層（抽象基底概念）
    Position {
        Long id PK
        String positionType
        Long syndicateId FK
        Money amount
        String currency
    }

    Facility {
        Long id PK
        Long syndicateId FK
        Money commitment
        String currency
        LocalDate startDate
        LocalDate endDate
        String interestTerms
    }

    Loan {
        Long id PK
        Long facilityId FK
        Money outstandingBalance
        Money originalAmount
        LocalDate drawdownDate
        BigDecimal interestRate
    }

    %% Transaction階層（抽象基底概念）
    Transaction {
        Long id PK
        String transactionType
        Long positionId FK
        Money amount
        String currency
        LocalDate transactionDate
        String description
    }

    Drawdown {
        Long id PK
        Long facilityId FK
        Money amount
        LocalDate drawdownDate
        String purpose
    }

    Payment {
        Long id PK
        String paymentType
        Long loanId FK
        Money amount
        LocalDate paymentDate
    }

    InterestPayment {
        Long id PK
        Long loanId FK
        Money amount
        LocalDate paymentDate
        LocalDate periodStart
        LocalDate periodEnd
        BigDecimal interestRate
    }

    PrincipalPayment {
        Long id PK
        Long loanId FK
        Money amount
        LocalDate paymentDate
        Money remainingBalance
    }

    FeePayment {
        Long id PK
        Long facilityId FK
        Money amount
        LocalDate paymentDate
        FeeType feeType
        String description
    }

    FacilityTrade {
        Long id PK
        Long facilityId FK
        Long sellerInvestorId FK
        Long buyerInvestorId FK
        Percentage shareTransferred
        Money tradeAmount
        LocalDate tradeDate
    }

    %% 継承関係
    Position ||--o{ Facility : "is-a"
    Position ||--o{ Loan : "is-a"
    Transaction ||--o{ Drawdown : "is-a"
    Transaction ||--o{ Payment : "is-a"
    Payment ||--o{ InterestPayment : "is-a"
    Payment ||--o{ PrincipalPayment : "is-a"
    Payment ||--o{ FeePayment : "is-a"
    Transaction ||--o{ FacilityTrade : "is-a"

    %% 関係性
    Facility ||--o{ Loan : "generates"
    Facility ||--o{ Drawdown : "source of"
    Loan ||--o{ InterestPayment : "generates"
    Loan ||--o{ PrincipalPayment : "generates"
```

### 2.2 配分管理（Share Pie vs Amount Pie）

```mermaid
erDiagram
    SharePie {
        Long id PK
        Long positionId FK
        Long investorId FK
        Percentage share
        LocalDate effectiveDate
    }

    AmountPie {
        Long id PK
        Long transactionId FK
        Long investorId FK
        Money amount
        String distributionType
    }

    Position ||--o{ SharePie : "has share distribution"
    Transaction ||--o{ AmountPie : "has amount distribution"
    SharePie }|--|| Investor : "investor share"
    AmountPie }|--|| Investor : "investor amount"
```

### 2.3 マスタデータ

```mermaid
erDiagram
    Currency {
        String code PK
        String name
        String symbol
        Integer decimalPlaces
        Boolean isActive
    }

    FeeType {
        String code PK
        String name
        String description
        String calculationMethod
        Boolean isActive
    }

    TransactionType {
        String code PK
        String name
        String description
        String category
        Boolean isActive
    }

    %% 関係性
    Transaction }|--|| TransactionType : "classified by"
    FeePayment }|--|| FeeType : "categorized by"
    Position }|--|| Currency : "denominated in"
    Transaction }|--|| Currency : "denominated in"
```

### 2.4 手数料種別詳細

```mermaid
classDiagram
    class FeeType {
        <<enumeration>>
        COMMITMENT_FEE
        ARRANGEMENT_FEE
        AGENT_FEE
        PARTICIPATION_FEE
        PREPAYMENT_FEE
        ANNUAL_MANAGEMENT_FEE
        UTILIZATION_FEE
        FACILITY_FEE
        AMENDMENT_FEE
        OTHER
        +getDescription() String
        +getCalculationMethod() String
    }
```

## 3. 資金の流れ（業務プロセス）

### 3.1 主要な資金フロー

```mermaid
flowchart TD
    A[投資家] -->|Facility Investment| B[シンジケート/Facility]
    B -->|Drawdown| C[借り手]
    C -->|Interest Payment| D[シンジケート]
    C -->|Principal Payment| D
    C -->|Fee Payment| D
    D -->|Distribution| A
    
    E[投資家A] -->|Facility Trade| F[投資家B]
    
    subgraph "Share Pie Distribution"
        G[Share Pie] --> H[Amount Pie Calculation]
        H --> I[Individual Distributions]
    end
    
    D --> G
```

### 3.2 取引タイプ別分類

```mermaid
mindmap
  root((Transaction Types))
    Drawdown
      Regular Drawdown
      Emergency Drawdown
      Revolving Drawdown
    Payment
      Interest Payment
        Fixed Rate
        Floating Rate
      Principal Payment
        Scheduled
        Prepayment
      Fee Payment
        Commitment Fee
        Arrangement Fee
        Agent Fee
        Participation Fee
        Management Fee
    Trade
      Facility Trade
        Primary Market
        Secondary Market
      Share Transfer
```

## 4. データモデル設計原則

### 4.1 継承戦略
- **Position階層**: Table Per Class（各サブタイプが独立テーブル）
- **Transaction階層**: Table Per Class（各サブタイプが独立テーブル）
- **Payment階層**: Single Table（discriminator使用）

### 4.2 Value Object活用
- **Money**: 金額計算の精度保証
- **Percentage**: 持分比率の正確な管理
- **Currency**: 多通貨対応の基盤

### 4.3 監査・履歴管理
- 全エンティティに監査フィールド（created_at, updated_at）
- 集約ルートに楽観的排他制御（version）
- Transaction系は不変（Immutable）として設計

### 4.4 拡張性考慮
- 抽象基底クラス（Position, Transaction）による柔軟な拡張
- 列挙型による分類の標準化
- 配分管理の二層構造（Share Pie → Amount Pie）

---

**注記**: 
- 現在実装済み: Company, Borrower, Investor, Syndicate, Facility, SharePie, Transaction, FacilityInvestment
- 将来実装予定: Loan, 他のTransaction階層, AmountPie, マスタデータ
- 共通フィールド（created_at, updated_at, version）は図から省略
