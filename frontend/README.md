# Frontend Architecture & Guidelines

## 技術スタック
- **Framework**: React 18 + TypeScript + Vite
- **Styling**: Tailwind CSS v3 (IntelliJ IDEA Dark色彩)
- **Form Management**: Zod + React Hook Form
- **State Management**: React useState/useEffect
- **API Client**: Custom api.ts module

## UI/UX方針

### 言語・表記
- **UI表記**: 英語統一（ユーザー向けテキスト）
- **コメント**: 日本語OK（ソースコード内）
- **日付形式**: YYYY-M-D (ダッシュ区切り)

### デザインシステム
- **カラーテーマ**: IntelliJ IDEA Dark準拠
- **フォント**: システムデフォルト（monospace for IDs）
- **アイコン**: Heroicons (SVGベース)

## アーキテクチャ

### ディレクトリ構成
```
src/
├── components/          # UIコンポーネント
│   ├── layout/         # レイアウト関連
│   ├── forms/          # フォームコンポーネント
│   ├── [domain]/       # ドメイン別コンポーネント
│   └── ...
├── pages/              # ページコンポーネント
├── schemas/            # Zodスキーマ定義
├── types/              # TypeScript型定義
├── lib/                # ユーティリティ・API
└── App.tsx             # エントリーポイント
```

### コンポーネント設計
- **Atomic Design**: Atoms → Molecules → Organisms → Pages
- **Props Interface**: 明示的な型定義
- **State Management**: ローカル状態優先、必要に応じてContext

### フォーム管理
- **Validation**: Zodスキーマベース
- **Multi-step Forms**: ステップ定義をschemas/で管理
- **Error Handling**: 統一的なエラーメッセージ表示

### API通信
- **Base URL**: http://localhost:8080/api/v1
- **Error Handling**: GlobalExceptionHandlerとの連携
- **Loading States**: 各テーブル・フォームで管理

## ナビゲーション

### ルーティング
- **List Pages**: `/drawdowns`, `/facilities`, `/syndicates`
- **Detail Pages**: `/drawdowns/:id` - Drawdown詳細とRelated Loan情報
- **Row Click**: テーブル行クリックで詳細ページに遷移

## 開発規約

### コーディング規約
1. **ファイル命名**: PascalCase (components), camelCase (utils)
2. **Props Interface**: コンポーネント名 + Props
3. **useState**: 明示的な型指定
4. **Import Order**: React → 3rd party → local

### UI一貫性
1. **Button Styles**: Tailwind classnames統一
2. **Form Elements**: 共通スタイリング適用
3. **Status Badges**: カラーコード統一
4. **Loading States**: スピナー + テキスト

### 多言語化対応
- **User-facing Text**: 英語必須
- **Validation Messages**: schemas/内で英語定義
- **Comments**: 日本語OK（実装背景説明等）

## バックエンド連携

### API仕様
- **Base URL**: Spring Boot (localhost:8080)
- **Format**: REST API + JSON
- **Authentication**: 未実装（将来対応予定）
- **CORS**: 開発環境で設定済み

### データ型
- **Money**: BigDecimal → number変換
- **Date**: ISO 8601 → Date オブジェクト
- **ID**: Auto-increment (DB) + UUID (business)

### Status Management
- **Entity Status**: `BorrowerStatus = 'DRAFT' | 'ACTIVE'`, `InvestorStatus = 'DRAFT' | 'ACTIVE'`
- **Transaction Status**: `TransactionStatus = 'DRAFT' | 'ACTIVE' | 'COMPLETED' | 'FAILED' | 'CANCELLED' | 'REFUNDED'`
- **Payment Status**: `PaymentStatus = 'PENDING' | 'PAID' | 'OVERDUE'`

---

## 重要な実装変更履歴

### Investor/Borrower選択機能の改善（2025年7月11日）

**背景**: Investor/Borrowerの状態管理変更により、DRAFT状態のエンティティが選択できない問題が発生。
Facility作成時に「No LEAD_BANK type investors found」エラーが発生していた。

**変更内容**:

#### 1. InvestorCards.tsx
```typescript
// 変更前: ACTIVE状態のみ表示
setInvestors(response.data.content.filter(investor => investor.status === 'ACTIVE'));

// 変更後: DRAFT/ACTIVE両方表示
setInvestors(response.data.content.filter(investor => 
  investor.status === 'ACTIVE' || investor.status === 'DRAFT'
));
```

#### 2. SharePieAllocation.tsx  
```typescript
// 変更前: ACTIVE状態のみ表示
const allInvestors = investorsResponse.data.content.filter(investor => investor.status === 'ACTIVE');

// 変更後: DRAFT/ACTIVE両方表示
const allInvestors = investorsResponse.data.content.filter(investor => 
  investor.status === 'ACTIVE' || investor.status === 'DRAFT'
);
```

**ビジネス価値**:
- ✅ **Facility作成の円滑化**: LEAD_BANK選択問題の解決
- ✅ **投資家選択の柔軟性**: DRAFT状態の投資家も参加可能
- ✅ **状態管理の一貫性**: バックエンドの状態変更に対応

**設計思想**:
投資家・借り手は**継続的に存在する法人格**であり、一つのFacilityの終了により「完了」状態になるのは不適切。
そのため、DRAFT（未参加）とACTIVE（参加中）の2状態のみを持ち、複数のFacilityに参加可能な設計とした。

---

## 開発コマンド

```bash
# 開発サーバー起動
npm run dev

# ビルド
npm run build

# 型チェック
npm run type-check

# リント
npm run lint
```

## 重要なURL
- **アプリケーション**: http://localhost:5173
- **バックエンドAPI**: http://localhost:8080/api/v1
- **Swagger**: http://localhost:8080/swagger-ui.html
