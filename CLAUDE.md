# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

# Syndicated Loan Management System - Claude Context

## プロジェクト概要
シンジケートローン管理システム：複数の金融機関が協調して大規模融資を行うシステム。
クリーンアーキテクチャとDDD（ドメイン駆動設計）を採用したSpring Boot REST API。

## 技術スタック
- **Backend**: Spring Boot 3.2.1, Java 17
- **Frontend**: React 18 + TypeScript + Vite
- **Build**: Maven (Backend), Vite (Frontend)
- **Database**: H2 (インメモリ)
- **Dependencies**: Spring Web, Data JPA, Validation, AOP, Lombok, JaCoCo, SpringDoc OpenAPI
- **State Management**: Spring State Machine (Facility・Loan・Party状態管理)
- **Transaction Management**: 統一Transaction階層 (基底クラス継承)

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
│   ├── infrastructure/ # GlobalExceptionHandler
│   └── statemachine/   # State Machine基盤
├── transaction/        # 取引管理（✅完了）
│   ├── entity/         # Transaction基底クラス、TransactionType/Status enum
│   ├── service/        # 統一取引管理・横断機能
│   └── controller/     # 取引履歴・レポートAPI
├── party/              # 参加者管理（✅完了）
├── syndicate/          # シンジケート団管理（✅完了）
├── facility/           # 融資枠管理（✅完了）
├── loan/               # ローン・ドローダウン管理（✅完了）
└── fee/                # 手数料管理（✅完了）
    ├── entity/         # FeePayment, FeeDistribution, FeeType
    ├── service/        # 手数料計算・配分ロジック
    ├── controller/     # 手数料API
    └── dto/            # CreateFeePaymentRequest
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
6. **State Machine実装規約**: 
   - 全ての状態変更はState Machine経由で実行（直接的なsetStatus禁止）
   - State Machine失敗時は業務処理を停止（Fail-Fast原則）
   - 全てのビジネス状態遷移に対応するイベント定義
   - 詳細: [State Machine Implementation Guidelines](docs/state-machine/standards/state-machine-guidelines.md)

## 重要なURL
- **Backend API**: http://localhost:8080
- **Frontend App**: http://localhost:5173
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **H2コンソール**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:testdb`
  - Username: `sa` / Password: `password`
- **JaCoCo Coverage**: `target/site/jacoco/index.html`（テスト後）

## 主要API エンドポイント

### 取引管理
- **取引履歴**: `GET /api/v1/transactions/facility/{facilityId}` - Facility別取引履歴
- **取引統計**: `GET /api/v1/transactions/facility/{facilityId}/statistics` - 取引統計情報
- **取引状態管理**: `POST /api/v1/transactions/{id}/approve|complete|cancel` - 取引状態変更

### ローン管理
- **ドローダウン**: `POST /api/v1/loans/drawdowns` - ローンの引き出し実行
- **支払い**: `POST /api/v1/loans/payments` - 元本・利息の返済処理
- **支払い履歴**: `GET /api/v1/loans/payments/loan/{loanId}` - 特定ローンの支払い履歴

### 手数料管理
- **手数料支払い**: `POST /api/v1/fees/payments` - 手数料支払い処理
- **手数料履歴**: `GET /api/v1/fees/payments/facility/{facilityId}` - Facility別手数料履歴
- **手数料統計**: `GET /api/v1/fees/payments/facility/{facilityId}/statistics` - 手数料統計

### その他
- **ファシリティ**: `POST /api/v1/facilities` - 融資枠の作成・管理

## 重要な設計判断
1. **アーキテクチャ簡素化**: CRUD中心機能では複雑なDDD構造より3層アーキテクチャが効率的
2. **JPA Entity統合**: JPA EntityをドメインEntityとして直接使用し、マッピング層を省略
3. **Business ID**: エンティティはUUID自動生成、データベースは別途自動増分ID
4. **金融計算**: BigDecimalベースの厳密な計算
5. **統合サービス**: 機能単位での統合サービスで複雑さを削減
6. **統一Transaction基盤の確立**: Transaction基底クラスによる取引管理統一
   - **目的**: 全取引タイプ（Drawdown, Payment, FeePayment等）の一貫管理
   - **継承戦略**: JPA JOINED継承による各サブクラス専用テーブル
   - **Type Safety**: TransactionType/Status enumによる型安全な分類・状態管理
7. **状態管理の導入**: Spring State Machineによる包括的ライフサイクル管理
   - **Facility**: DRAFT → FIXED（ドローダウン後変更禁止）
   - **Loan**: DRAFT → ACTIVE → OVERDUE → COMPLETED（返済ライフサイクル）
   - **Party**: ACTIVE → RESTRICTED（Facility参加後制限）
   - **実装ガイド**: [State Machine Implementation Guidelines](docs/state-machine/standards/state-machine-guidelines.md)
   - **調査結果**: [State Machine Analysis](docs/state-machine/README.md)

## 実装済み機能
- ✅ **Party管理**: 企業・借り手・投資家のCRUD
- ✅ **Syndicate管理**: シンジケート団の組成・管理
- ✅ **Facility管理**: 融資枠作成、SharePie（持分比率）管理、状態管理（State Machine）
- ✅ **Loan管理**: ドローダウン実行、返済スケジュール自動生成
- ✅ **Payment管理**: 元本・利息返済処理、投資家別配分管理、REST API
- ✅ **Transaction管理**: 統一取引基盤、横断的取引履歴・統計、状態管理API
- ✅ **Fee管理**: 7種類手数料タイプ、手数料計算・配分、投資家別配分API
- ✅ **State Machine**: Facility・Loan・Party包括的ライフサイクル管理
- ✅ **Investor投資額管理**: 現在投資額の自動追跡（Drawdown増加・返済減少）
- ✅ **共通基盤**: Money/Percentage値オブジェクト、例外処理、Transaction基底クラス

## 主要なビジネスルール
- **SharePie検証**: ファシリティの持分比率合計は100%必須
- **ドローダウン制限**: 利用可能額を超えた引き出し不可
- **Facility状態管理**: 
  - DRAFT状態でのみ変更可能（持分比率変更、更新等）
  - ドローダウン実行時にFIXED状態に自動遷移
  - FIXED状態では2度目のドローダウン実行を禁止
- **Loan状態管理**:
  - ドローダウン直後はDRAFT状態（返済未開始）
  - 初回返済実行時にACTIVE状態に自動遷移（State Machine）
  - 元本・利息どちらの返済でも状態遷移が発生
- **返済スケジュール**: 月次返済、利率計算自動化
- **AmountPie生成**: ドローダウン時の投資家別配分額自動計算
- **投資額自動管理**: Drawdown時増加、元本返済時減少（利息支払いは影響なし）
- **PaymentDistribution生成**: 返済時の投資家別配分額自動計算（持分比率ベース）
- **Transaction状態管理**: 全取引のライフサイクル制御（PENDING→PROCESSING→COMPLETED）
- **手数料配分ルール**: 
  - 管理手数料・アレンジメント手数料：リードバンク収益
  - コミットメント手数料・遅延手数料：投資家配分（持分比率ベース）
  - 取引手数料・エージェント手数料：エージェントバンク収益
- **手数料計算検証**: 計算基準額×手数料率=手数料額の整合性チェック

## 重要な設計判断

### 参照整合性保護におけるBoundedContext間依存について

**背景**: Syndicate/Facilityに参加中のBorrower/Investorを削除すると「Unknown」表示になり、データ整合性が破綻する問題が発生。

**検討した解決策**:
1. **ドメイン層での相互依存**: 各ドメインが直接他ドメインを参照
2. **Domain Events**: 非同期イベントによる結果整合性
3. **Application Service層での制御**: 上位層で複数ドメインを調整

**採用した解決策**: **Application Service層での制御**

**判断理由**:
- **ビジネス要件**: 金融系システムでデータ整合性は最重要、「Unknown」表示は業務上致命的
- **システム規模**: 単一アプリケーション、限定的エンティティ数、小規模チーム
- **実用性重視**: 理論的純粋性より確実なビジネスルール実装を優先
- **将来への配慮**: ドメイン層は純粋性保持、Application層の依存は最小限に制限

**実装方針**:
```java
// Application Service層で相互依存を受け入れ
@Service 
public class BorrowerService {
    // Syndicate参加チェックのためSyndicateRepositoryを参照
    public void deleteBorrower(Long borrowerId) {
        if (syndicateRepository.existsByBorrowerId(borrowerId)) {
            throw new BusinessRuleViolationException("参加中のSyndicateがあるため削除できません");
        }
        borrowerRepository.deleteById(borrowerId);
    }
}
```

**将来のマイクロサービス化への対応**: Domain Eventsパターンへの移行準備として、現在の実装を最小限に留める。

### 7. **削除処理における状態管理の必須要件**

**教訓**: 「作成時に状態を変更したら、削除時に必ず状態を復旧せよ」

**問題の発生**: 2025年7月9日、Facility削除処理において以下の重大な設計上の欠陥が発見された：
- Facility作成時: Borrower/Investor → RESTRICTED、Syndicate → ACTIVE
- Facility削除時: **状態復旧処理が一切存在しない**
- 結果: 削除後もRESTRICTED状態が残存し、データ整合性が破綻

**根本原因**: 
1. **実装の非対称性**: 作成処理（`onFacilityCreated()`）のみ実装し、削除処理（`onFacilityDeleted()`）を忘却
2. **テストの不備**: 削除後の状態復旧テストが存在しない
3. **文書化の欠如**: 削除時の状態復旧要件が文書化されていない

**修正内容**:
```java
// EntityStateService に追加
public void onFacilityDeleted(Facility facility) {
    // 1. Syndicate: ACTIVE → DRAFT
    // 2. Borrower: RESTRICTED → ACTIVE
    // 3. Investor: RESTRICTED → ACTIVE
}

// FacilityService.deleteFacility() の完全な再実装
@Transactional
public void deleteFacility(Long id) {
    // 1. 存在確認
    // 2. ビジネスルール検証
    // 3. 状態復旧処理 ← 追加
    // 4. 関連データ削除
    // 5. 物理削除
}
```

**設計原則**: 
- **状態遷移の対称性**: 全ての状態変更は、その逆方向の処理も必ず実装する
- **削除処理の4段階**: 検証 → 状態復旧 → 関連データ削除 → 物理削除
- **State Machine統合**: 削除時も必ずState Machineイベント（`FACILITY_DELETED`）を発火

**予防策**:
1. **実装チェックリスト**: 状態変更を伴う機能では、作成・削除の両方向処理を必ず実装
2. **テスト戦略**: 削除後の状態復旧テストを必須とする
3. **コードレビュー**: 削除処理では状態復旧の有無を重点的に確認

この教訓により、将来的な同様の問題を防止し、データ整合性を保つことができる。

---

## タスク完了時の文書更新確認プロセス

**重要**: タスク完了時は必ず以下を確認し、ユーザーに質問する

### 更新確認が必要なケース
1. **アーキテクチャ変更**: 新しい技術・パターン・設計判断があった場合
2. **機能追加**: 新しいモジュール・重要な機能が追加された場合  
3. **ビジネスルール変更**: 重要なビジネスロジック・制約が変更された場合
4. **技術スタック変更**: 新しいライブラリ・フレームワークが導入された場合

### 更新対象ドキュメント
- **CLAUDE.md**: プロジェクト全体への影響がある変更
- **.clinerules-architecture.md**: アーキテクチャ設計・パターンの変更
- **.clinerules-progress.md**: 実装進捗・完了機能の更新
- **.clinerules-requirements.md**: 要件変更時
- **.clinerules-techContext.md**: 技術スタック変更時

### 確認質問の例
「この実装により[変更内容]が追加されました。関連ドキュメント（CLAUDE.md、.clinerules等）の更新も必要でしょうか？」

**このプロセスにより、ドキュメントと実装の整合性を常に保ち、プロジェクトの知識が確実に蓄積されます。**

---

## フロントエンド関連ドキュメント

フロントエンドのアーキテクチャ、UI/UX方針、開発規約については以下を参照：
- **frontend/README.md**: フロントエンド専用の技術仕様・開発ガイドライン