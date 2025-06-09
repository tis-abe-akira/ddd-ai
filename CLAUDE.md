# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

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

## 現在の機能モジュール構造
```
com.example.syndicatelending/
├── common/             # 共通要素
│   ├── domain/model/   # Money, Percentage値オブジェクト
│   ├── application/exception/ # ビジネス例外クラス
│   └── infrastructure/ # GlobalExceptionHandler
├── party/              # 参加者管理（✅完了）
├── syndicate/          # シンジケート団管理（✅完了）
├── facility/           # 融資枠管理（✅完了）
├── loan/               # ローン・ドローダウン管理（✅完了）
└── transaction/        # 取引基底クラス（🔄一部実装）
```

各モジュールは標準的な3層構造（controller/service/repository/entity/dto）を採用。

## 開発コマンド
```bash
# アプリケーション起動
mvn spring-boot:run

# テスト実行
mvn test

# 特定のテストクラス実行
mvn test -Dtest=FacilityServiceTest

# 特定のテストメソッド実行
mvn test -Dtest=FacilityServiceTest#testCreateFacility

# ビルド
mvn clean install

# カバレッジレポート生成
mvn test jacoco:report
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

## 重要なURL
- **アプリケーション**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **H2コンソール**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:testdb`
  - Username: `sa` / Password: `password`
- **JaCoCo Coverage**: `target/site/jacoco/index.html`（テスト後）

## 重要な設計判断
1. **アーキテクチャ簡素化**: CRUD中心機能では複雑なDDD構造より3層アーキテクチャが効率的
2. **JPA Entity統合**: JPA EntityをドメインEntityとして直接使用し、マッピング層を省略
3. **Business ID**: エンティティはUUID自動生成、データベースは別途自動増分ID
4. **金融計算**: BigDecimalベースの厳密な計算
5. **統合サービス**: 機能単位での統合サービスで複雑さを削減

## 実装済み機能
- ✅ **Party管理**: 企業・借り手・投資家のCRUD
- ✅ **Syndicate管理**: シンジケート団の組成・管理
- ✅ **Facility管理**: 融資枠作成、SharePie（持分比率）管理
- ✅ **Loan管理**: ドローダウン実行、返済スケジュール自動生成
- ✅ **Payment管理**: 元本・利息返済処理、投資家別配分管理
- ✅ **Investor投資額管理**: 現在投資額の自動追跡（Drawdown増加・返済減少）
- ✅ **共通基盤**: Money/Percentage値オブジェクト、例外処理

## 主要なビジネスルール
- **SharePie検証**: ファシリティの持分比率合計は100%必須
- **ドローダウン制限**: 利用可能額を超えた引き出し不可
- **返済スケジュール**: 月次返済、利率計算自動化
- **AmountPie生成**: ドローダウン時の投資家別配分額自動計算
- **投資額自動管理**: Drawdown時増加、元本返済時減少（利息支払いは影響なし）
- **PaymentDistribution生成**: 返済時の投資家別配分額自動計算（持分比率ベース）