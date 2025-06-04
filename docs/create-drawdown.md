# Drawdown登録処理のシーケンスと解説（コードリバースベース）

## シーケンス図（Mermaid形式）

````mermaid
sequenceDiagram
    participant Client
    participant DrawdownController
    participant DrawdownService
    participant TransactionRepository
    participant DB

    Client->>DrawdownController: POST /api/v1/loans/drawdowns (CreateDrawdownRequest)
    DrawdownController->>DrawdownService: createDrawdown(request)
    DrawdownService->>TransactionRepository: save(Drawdown)
    TransactionRepository->>DB: INSERT INTO transaction, drawdown
    DB-->>TransactionRepository: Drawdown（ID, created_at, version付与）
    TransactionRepository-->>DrawdownService: Drawdown
    DrawdownService-->>DrawdownController: Drawdown
    DrawdownController-->>Client: 200 OK + Drawdown
````

---

## コードベースの簡潔な説明

1. **Client** が `CreateDrawdownRequest` をPOST送信  
   → `DrawdownController#createDrawdown` が受け取る

2. **DrawdownController** はリクエストDTOを `DrawdownService#createDrawdown` に渡す

3. **DrawdownService** で
   - `Drawdown` エンティティ生成
   - 監査フィールド（created_at, updated_at）、バージョン（version）自動付与
   - 必要なバリデーション・ビジネスルール違反時は `BusinessRuleViolationException` をスロー

4. **TransactionRepository**（JPA）で `save(Drawdown)`
   - `transaction` テーブル＋`drawdown` テーブルにINSERT
   - DBでID, created_at, versionが付与される

5. **DrawdownService** から **DrawdownController** へ永続化済みエンティティを返却

6. **DrawdownController** が200 OK＋Drawdownエンティティを返す

---

### 備考

- 監査フィールド・バージョンは `Transaction` の@PrePersist/@Versionで自動管理
- 例外はController層でcatchせず、GlobalExceptionHandlerに委譲
- Facilityドメインの標準実装パターンを踏襲
