# GitHub Copilot 開発指示書

このプロジェクトは、シンジケートローン管理システムの DDD ベース実装です。

## 🤖 AI 開発支援ツール向け指示

### 📋 必須確認事項

**コード生成前に必ず以下を確認してください：**

1. **`.clinerules-requirements.md`** - ビジネス要件（最優先）
2. **`.clinerules-techContext.md`** - 技術制約
3. **`.clinerules-architecture.md`** - 設計パターン
4. **`.clinerules-implementation-standards.md`** - 実装標準（重要）
5. **`.clinerules-progress.md`** - 現在の実装状況

### 🎯 実装時の基本方針

#### 標準実装パターンに従う

- **Facility ドメイン** が実装標準のリファレンス
- 詳細な実装パターンは `.clinerules-implementation-standards.md` を参照
- 新しい Bounded Context 作成時は Facility の構造をベースにする

#### 必須実装項目の確認

- ✅ 監査フィールド（created_at, updated_at）
- ✅ 集約ルートにバージョンフィールド（楽観的ロック）
- ✅ `ResourceNotFoundException` と `BusinessRuleViolationException` の適切な使い分け
- ✅ Controller 層で try-catch 禁止（GlobalExceptionHandler に委譲）
- ✅ 全 CRUD 操作実装（ページネーション対応）

詳細な開発ガイドラインは `.clinerules/README.md` を参照してください。
