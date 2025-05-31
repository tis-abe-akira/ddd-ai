# Syndicated Loan Management System - Claude Context

## プロジェクト概要
シンジケートローン管理システム：複数の金融機関が協調して大規模融資を行うシステム。
クリーンアーキテクチャとDDD（ドメイン駆動設計）を採用したSpring Boot REST API。

## 技術スタック
- **Framework**: Spring Boot 3.2.1, Java 17
- **Build**: Maven
- **Database**: H2 (インメモリ)
- **Dependencies**: Spring Web, Data JPA, Validation, AOP, Lombok, JaCoCo, SpringDoc OpenAPI

## アーキテクチャ
3層クリーンアーキテクチャ：
- **Domain Layer**: エンティティ、値オブジェクト、ドメインサービス
- **Application Layer**: ユースケース、アプリケーションサービス
- **Infrastructure Layer**: DB実装、REST API、外部連携

## パッケージ構造
```
com.example.syndicatelending/
├── common/             # 共通要素
│   ├── domain/model/   # Money, Percentage
│   ├── application/exception/ # BusinessRuleViolationException, ResourceNotFoundException
│   └── infrastructure/ # GlobalExceptionHandler
└── [feature]/          # 機能別境界コンテキスト
    ├── domain/         # ドメインレイヤー
    ├── application/    # アプリケーションレイヤー
    └── infrastructure/ # インフラレイヤー
```

## 開発コマンド
```bash
# アプリケーション起動
mvn spring-boot:run

# テスト実行
mvn test

# ビルド
mvn clean install
```

## 開発規約
1. **DDD**: エンティティと値オブジェクトを適切に分離
2. **Clean Architecture**: 依存関係は内側に向かう（Domain <- Application <- Infrastructure）
3. **Value Objects**: MoneyとPercentageは不変で金融計算に特化
4. **Exception Handling**: 
   - `BusinessRuleViolationException`: 業務ルール違反（400）
   - `ResourceNotFoundException`: リソース未発見（404）
   - `GlobalExceptionHandler`: 統一的エラーレスポンス
5. **Testing**: レイヤー別テスト戦略（Domain Unit -> Application Service -> Infrastructure Integration）

## データベース
- H2コンソール: http://localhost:8080/h2-console
- 接続情報: jdbc:h2:mem:testdb, sa/password

## API仕様
- SpringDoc OpenAPI統合済み
- 標準RESTful設計
- 構造化エラーレスポンス

## 重要な設計判断
1. **JPA分離**: ドメインエンティティとJPAエンティティは分離
2. **Business ID**: ドメインエンティティはUUID、JPA側は自動生成ID
3. **金融計算**: BigDecimalベースの厳密な計算
4. **境界コンテキスト**: 機能別パッケージングでコードの凝集性を高める