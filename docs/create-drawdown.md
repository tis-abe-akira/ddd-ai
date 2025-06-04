# Drawdown登録処理のシーケンスと解説（Loan生成を含むコードリバースベース）

## シーケンス図（Mermaid形式）

````mermaid
sequenceDiagram
    participant Client
    participant DrawdownController
    participant DrawdownService
    participant LoanRepository
    participant DrawdownRepository
    participant DB

    Client->>DrawdownController: POST /api/v1/loans/drawdowns (CreateDrawdownRequest)
    DrawdownController->>DrawdownService: createDrawdown(request)
    DrawdownService->>LoanRepository: save(Loan)
    LoanRepository->>DB: INSERT INTO loan
    DB-->>LoanRepository: Loan（ID, created_at, version付与）
    LoanRepository-->>DrawdownService: Loan
    DrawdownService->>DrawdownRepository: save(Drawdown(loanId))
    DrawdownRepository->>DB: INSERT INTO drawdown, transaction
    DB-->>DrawdownRepository: Drawdown（ID, created_at, version付与）
    DrawdownRepository-->>DrawdownService: Drawdown
    DrawdownService-->>DrawdownController: Drawdown
    DrawdownController-->>Client: 200 OK + Drawdown
````

---

## コードベースの簡潔な説明

1. **Client** が `CreateDrawdownRequest` をPOST送信  
   → `DrawdownController#createDrawdown` が受け取る

2. **DrawdownController** はリクエストDTOを `DrawdownService#createDrawdown` に渡す

3. **DrawdownService** で
   - バリデーション実施
   - `Loan` エンティティを生成し、`LoanRepository.save()` で永続化
   - 生成されたLoanのIDを使って `Drawdown` エンティティを生成し、`DrawdownRepository.save()` で永続化

4. **LoanRepository** で `loan` テーブルにINSERT（ID, created_at, version自動付与）

5. **DrawdownRepository** で `drawdown` テーブル＋`transaction` テーブルにINSERT（ID, created_at, version自動付与）

6. **DrawdownService** から **DrawdownController** へ永続化済みDrawdownエンティティを返却

7. **DrawdownController** が200 OK＋Drawdownエンティティを返す

---

### 備考

- 監査フィールド・バージョンは `Transaction` の@PrePersist/@Versionで自動管理
- 例外はController層でcatchせず、GlobalExceptionHandlerに委譲
- Facilityドメインの標準実装パターンを踏襲
- Drawdown登録時に必ずLoanも新規作成される点に注意
