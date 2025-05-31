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
実用的な3層アーキテクチャ（簡素化版）：
- **Controller Layer**: REST API、リクエスト/レスポンス処理
- **Service Layer**: ビジネスロジック、トランザクション管理
- **Repository Layer**: データ永続化（Spring Data JPA）

## パッケージ構造
```
com.example.syndicatelending/
├── common/             # 共通要素
│   ├── domain/model/   # Money, Percentage
│   ├── application/exception/ # BusinessRuleViolationException, ResourceNotFoundException
│   └── infrastructure/ # GlobalExceptionHandler
└── party/              # Party管理機能（完了）
    ├── entity/         # JPA Entity (Company, Borrower, Investor)
    ├── repository/     # Spring Data JPA Repository
    ├── service/        # PartyService (統合サービス)
    ├── controller/     # PartyController (REST API)
    └── dto/           # Request DTO
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
1. **実用的設計**: 機能の複雑さに応じて適切な構造を選択
2. **3層アーキテクチャ**: Controller -> Service -> Repository の明確な責務分離
3. **Value Objects**: MoneyとPercentageは不変で金融計算に特化
4. **Exception Handling**: 
   - `BusinessRuleViolationException`: 業務ルール違反（400）
   - `ResourceNotFoundException`: リソース未発見（404）
   - `GlobalExceptionHandler`: 統一的エラーレスポンス
5. **Testing**: 各層での適切なテスト戦略（Entity -> Service -> API Integration）

## データベース
- H2コンソール: http://localhost:8080/h2-console
- 接続情報: jdbc:h2:mem:testdb, sa/password

## API仕様
- SpringDoc OpenAPI統合済み
- 標準RESTful設計
- 構造化エラーレスポンス

## 重要な設計判断
1. **アーキテクチャ簡素化**: CRUD中心機能では複雑なDDD構造より3層アーキテクチャが効率的
2. **JPA Entity統合**: JPA EntityをドメインEntityとして直接使用し、マッピング層を省略
3. **Business ID**: エンティティはUUID自動生成、データベースは別途自動増分ID
4. **金融計算**: BigDecimalベースの厳密な計算
5. **統合サービス**: 機能単位での統合サービスで複雑さを削減

## 完了機能
- ✅ **Party管理**: 企業・借り手・投資家の作成・検索・一覧（全23テスト成功）
- ✅ **共通基盤**: Money/Percentage値オブジェクト、例外処理、バリデーション