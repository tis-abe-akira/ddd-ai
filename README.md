# シンジケートローン管理システム

複数の金融機関が協調して大規模融資を行うシンジケートローンの管理システムです。Spring Boot + Java 17で構築された軽量級DDDアーキテクチャを採用しています。

## 🎯 システム概要

シンジケートローンは、複数の投資家（金融機関）が協調して単一の借り手に対して融資を行う仕組みです。本システムでは以下の主要機能を提供します：

- **参加者管理**: 企業、借り手、投資家の情報管理
- **シンジケート団組成**: リードバンクを中心とした投資家グループの管理
- **融資枠管理**: ファシリティの作成と投資家間の持分比率管理
- **投資記録管理**: ファシリティ組成時の投資家投資記録自動生成
- **ドローダウン処理**: 融資実行、ローン作成、返済スケジュール自動生成
- **返済処理**: 元本・利息返済、投資家別配分管理
- **手数料管理**: 各種手数料の計算・配分・支払い処理
- **取引統合管理**: 全取引タイプの統一的な追跡・監査・レポート機能

## 🏗️ アーキテクチャ

軽量級DDD（ドメイン駆動設計）を採用した3層アーキテクチャ：

```
Controller → Service → Repository → Entity
     ↓         ↓         ↓         ↓
   REST API  業務ロジック データアクセス  ドメインモデル
```

### 主要なBounded Context
- **Party**: 参加者管理（Company, Borrower, Investor）
- **Syndicate**: シンジケート団管理
- **Facility**: 融資枠管理
- **Transaction**: 取引基底クラス（統一Transaction基盤）
- **Loan**: ローン・ドローダウン・返済管理
- **Fee**: 手数料管理（Fee Payment・配分処理）
- **Common**: 共通Value Objects（Money, Percentage）

## 🚀 クイックスタート

### 前提条件
- Java 17
- Maven 3.x

### 起動方法
```bash
# 依存関係のインストール
mvn clean install

# アプリケーション起動
mvn spring-boot:run
```

### アクセス先
- **API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **H2 Console**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:testdb`
  - Username: `sa`
  - Password: `password`

## 📊 現在の実装状況

| 機能領域 | 実装状況 | 説明 |
|---------|---------|------|
| 参加者管理 | ✅ 完了 | Company, Borrower, Investor の CRUD操作 |
| シンジケート管理 | ✅ 完了 | シンジケート団の組成・管理 |
| 融資枠管理 | ✅ 完了 | Facility作成、SharePie（持分比率）管理、状態管理 |
| 投資記録管理 | ✅ 完了 | FacilityInvestment自動生成（Facility組成時） |
| ドローダウン処理 | ✅ 完了 | Drawdown実行、Loan作成、返済スケジュール自動生成 |
| 返済処理 | ✅ 完了 | Payment実行、元本・利息返済、投資家別配分管理 |
| 手数料管理 | ✅ 完了 | Fee Payment、手数料計算・配分・投資家分配 |
| 投資額管理 | ✅ 完了 | Investor現在投資額の自動追跡（Drawdown増加・返済減少） |
| 取引統合管理 | ✅ 完了 | Transaction基底クラス、全取引タイプの統一管理・追跡 |
| レポーティング | 🚧 未実装 | 各種レポート・分析機能 |

**開発完了度**: 約95%（基本的なシンジケートローン管理機能完成、取引統合管理・手数料処理含む）

## 🛠️ 技術スタック

- **Framework**: Spring Boot 3.2.1
- **Language**: Java 17
- **Database**: H2 (In-memory)
- **ORM**: Spring Data JPA
- **Documentation**: SpringDoc OpenAPI
- **Testing**: JUnit 5, Mockito
- **Build**: Maven

## 📚 詳細ドキュメント

システムの詳細な設計・実装情報は `.clinerules` フォルダに整理されています：

### 📋 要件・設計
- **[業務要件](.clinerules/.clinerules-requirements.md)**: シンジケートローンの業務概要とエンティティ定義
- **[アーキテクチャ設計](.clinerules/.clinerules-architecture.md)**: システム設計パターンと技術的決定事項
- **[技術コンテキスト](.clinerules/.clinerules-techContext.md)**: 技術スタック・環境設定・制約事項

### 🔧 開発・実装
- **[実装標準](.clinerules/.clinerules-implementation-standards.md)**: コーディング規約・実装パターン
- **[開発進捗](.clinerules/.clinerules-progress.md)**: 実装状況・完了機能・今後の予定
- **[ガイドライン](.clinerules/README.md)**: 開発時の参照順序・更新ポリシー

### 📊 データモデル
- **[データモデル図](data-model.md)**: 現在の実装と将来予定のMermaid ERD

## 🧪 テスト

```bash
# 全テスト実行
mvn test

# カバレッジレポート生成
mvn jacoco:report
```

テストカバレッジレポート: `target/site/jacoco/index.html`

## 🔄 API仕様

### 主要エンドポイント

#### 参加者管理
- `GET /api/v1/parties/companies` - 企業一覧
- `POST /api/v1/parties/companies` - 企業作成
- `GET /api/v1/parties/borrowers` - 借り手一覧
- `POST /api/v1/parties/borrowers` - 借り手作成
- `GET /api/v1/parties/investors` - 投資家一覧
- `POST /api/v1/parties/investors` - 投資家作成

#### シンジケート管理
- `GET /api/v1/syndicates` - シンジケート一覧
- `POST /api/v1/syndicates` - シンジケート作成
- `PUT /api/v1/syndicates/{id}` - シンジケート更新

#### 融資枠管理
- `GET /api/v1/facilities` - ファシリティ一覧
- `POST /api/v1/facilities` - ファシリティ作成（FacilityInvestment自動生成）
- `PUT /api/v1/facilities/{id}` - ファシリティ更新

#### ドローダウン処理
- `POST /api/v1/loans/drawdowns` - ドローダウン実行（Loan自動生成）
- `GET /api/v1/loans/drawdowns` - ドローダウン一覧
- `GET /api/v1/loans/drawdowns/{id}` - ドローダウン詳細
- `GET /api/v1/loans/drawdowns/facility/{facilityId}` - ファシリティ別ドローダウン一覧

#### 返済処理
- `POST /api/v1/loans/payments` - 返済実行（投資家配分自動計算）
- `GET /api/v1/loans/payments/loan/{loanId}` - ローン別返済履歴

#### 手数料管理
- `POST /api/v1/fees/payments` - 手数料支払い作成（投資家配分自動処理）
- `GET /api/v1/fees/payments/facility/{facilityId}` - ファシリティ別手数料履歴
- `GET /api/v1/fees/payments/type/{feeType}` - 手数料タイプ別検索

#### 取引統合管理
- `GET /api/v1/transactions/facility/{facilityId}` - ファシリティ別全取引履歴
- `GET /api/v1/transactions/type/{transactionType}` - 取引タイプ別検索
- `GET /api/v1/transactions/facility/{facilityId}/statistics` - 取引統計

詳細なAPI仕様は [Swagger UI](http://localhost:8080/swagger-ui.html) で確認できます。

## 🎨 主要な設計パターン

### Value Objects
- **Money**: 金額計算の精度保証（BigDecimal基盤）
- **Percentage**: 持分比率の正確な管理（0-1の範囲）

### エラーハンドリング
- **ResourceNotFoundException**: リソースが見つからない場合（HTTP 404）
- **BusinessRuleViolationException**: ビジネスルール違反（HTTP 400）
- **GlobalExceptionHandler**: 統一的なエラーレスポンス

### データ整合性
- 楽観的排他制御（`@Version`）による同時更新制御
- 監査フィールド（created_at, updated_at）による変更履歴
- 複雑なビジネスバリデーション（SharePie合計100%チェック等）

## 🚧 今後の開発予定

### Phase 1: 高度な機能（優先度: Medium）
- **レポーティング機能**: 投資家別収益レポート、ファシリティ分析
- **バッチ処理**: 定期手数料の自動計算・生成
- **監査ログ**: 詳細な変更履歴追跡

### Phase 2: ビジネス拡張（優先度: Medium）
- **セカンダリー取引**: ファシリティ持分の売買機能
- **複雑な配分ルール**: ウォーターフォール配分・優先劣後構造
- **リスク管理**: 信用リスク・流動性リスク管理

### Phase 3: アーキテクチャ進化（優先度: Low）
- **Event Sourcing導入**: イベント駆動アーキテクチャ
- **CQRS パターン適用**: 読み書き分離
- **マイクロサービス化**: サービス境界の分離

## 🤝 開発ガイドライン

新しい機能を開発する際は、以下の順序で `.clinerules` ドキュメントを参照してください：

1. **要件確認**: [requirements.md](.clinerules/.clinerules-requirements.md)
2. **技術制約**: [techContext.md](.clinerules/.clinerules-techContext.md)
3. **設計パターン**: [architecture.md](.clinerules/.clinerules-architecture.md)
4. **実装標準**: [implementation-standards.md](.clinerules/.clinerules-implementation-standards.md)
5. **実装状況**: [progress.md](.clinerules/.clinerules-progress.md)

## 📄 ライセンス

このプロジェクトは学習・研究目的で作成されています。

---

**Note**: このシステムは実際の金融取引には使用せず、学習・デモンストレーション目的でのみご利用ください。
