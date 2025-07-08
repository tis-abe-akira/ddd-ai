# アーキテクチャー評価メモ

## ユーザーの質問

```
backendのBoundedContextごとに、
どのような責務を持っていて、
他のBoundedContextとどのようなコラボレーションをしているか？を簡潔に説明してください。
その際に、DDDの語彙をなるべく用いてください。
```

## AIの回答 (Claude Code)

```
  Bounded Context分析レポート

  1. Party Context（参加者コンテキスト）

  責務:
  - Aggregate Root: Company, Borrower, Investor
  - 核心ドメイン: 参加者のライフサイクル管理、資格検証、制約管理
  - ドメイン不変条件: CreditRating上限制約、投資能力制約、参加状態（ACTIVE/RESTRICTED）管理

  コラボレーション:
  - Upstream Dependency: 他のContextから参照されるShared Kernel的役割
  - Cross-Context Integrity: Application Service層でSyndicate/Facilityとの参照整合性保護（削除制約）
  - State Machine Integration: Party状態をRESTRICTEDに遷移（Facility参加時）

  2. Syndicate Context（シンジケート団コンテキスト）

  責務:
  - Aggregate Root: Syndicate
  - 核心ドメイン: シンジケート団組成、Lead Bank資格検証、メンバー構成管理
  - ドメイン不変条件: Lead BankのLEAD_BANK資格必須、1 Syndicate = 1 Facilityルール

  コラボレーション:
  - Anti-Corruption Layer: PartyRepositoryを通じた参加者情報取得（SyndicateDetailResponseDTO生成）
  - Downstream Protection: FacilityRepositoryでの使用チェック（削除制約）
  - Domain Service: Lead Bank資格検証ロジック

  3. Facility Context（融資枠コンテキスト）

  責務:
  - Aggregate Root: Facility（SharePie、FacilityInvestmentを含むAggregate）
  - 核心ドメイン: 融資枠管理、持分比率（SharePie）管理、投資額追跡
  - ドメイン不変条件: SharePie合計100%、DRAFT→FIXED状態遷移の不可逆性

  コラボレーション:
  - State Machine: FacilityStateMachine（DRAFT→FIXED遷移）
  - Orchestration: SyndicateとPartyの複合バリデーション
  - Event Sourcing: Drawdown削除時のFacility状態復帰

  4. Loan Context（ローンコンテキスト）

  責務:
  - Aggregate Root: Loan, Drawdown, Payment, PaymentDetail
  - 核心ドメイン: ドローダウン実行、返済スケジュール管理、投資家配分計算
  - ドメイン不変条件: 利用可能額制約、返済スケジュール整合性

  コラボレーション:
  - State Machine: LoanStateMachine（DRAFT→ACTIVE→COMPLETED）
  - Cross-Context Calculation: FacilityのSharePieベースのAmountPie/PaymentDistribution生成
  - Transaction Integration: 統一Transaction基盤への参加

  5. Fee Context（手数料コンテキスト）

  責務:
  - Aggregate Root: FeePayment（FeeDistributionを含む）
  - 核心ドメイン: 手数料計算、受取人決定、投資家配分
  - Value Object: FeeCalculationRule（手数料計算ロジック）、RecipientType

  コラボレーション:
  - Domain Service: FeeCalculationRule（手数料タイプ別計算ルール）
  - Cross-Context Integration: Facilityの持分比率ベース配分計算
  - Transaction Participation: Transaction基底クラス継承

  6. Transaction Context（取引コンテキスト）

  責務:
  - Shared Kernel: 全取引タイプの基盤提供
  - 横断的関心事: 取引状態管理、監査ログ、統計レポート
  - Entity Hierarchy: Transaction基底クラス（JPA JOINED継承）

  コラボレーション:
  - Infrastructure Layer: 全ドメインに共通する取引インフラ
  - State Management: TransactionStatus統一管理
  - Cross-Cutting Concerns: 取引履歴・統計の横断的提供

  7. Common Context（共通コンテキスト）

  責務:
  - Shared Kernel: Money, Percentage Value Objects
  - Infrastructure: State Machine基盤、例外処理
  - Cross-Cutting: GlobalExceptionHandler

  アーキテクチャ特徴

  DDD Pattern Implementation:

  1. Application Service Layer: Cross-Context依存の制御層
  2. State Machine Integration: エンティティライフサイクルの統一管理
  3. Repository Pattern: 各ContextのAggregate Rootアクセス制御
  4. Anti-Corruption Layer: 他Contextアクセス時のDTO変換

  Context間通信パターン:

  - Repository-Mediated: Application Service層での他Context Repository直接参照
  - Shared Infrastructure: Transaction基盤、State Machine
  - Domain Event（暗黙的）: State遷移時の状態伝播

  設計判断の評価:

  ✅ 実用性重視: 理論的純粋性より確実なビジネスルール実装
  ✅ 統一Transaction基盤: 金融取引の包括的管理
  ✅ State Machine統合: エンティティ状態の一貫性保証
  ⚠️ Cross-Context依存: Application Service層での制御だが将来のマイクロサービス化要検討

  この設計は、DDD原則に基づきながら実用性を重視した、金融ドメインに適した堅実なアーキテクチャとなっています。
```

## ユーザーの質問

```
相互のContextに依存を持っているところがよろしくない点である（改良すべき点である）と考えれば良いでしょうか？
現状でどの程度の問題点があり、すぐに手を打つべき点、将来的な課題とすべき点などに分類できますか？
```


## AIの回答 (Claude Code)

```
Cross-Context依存問題の分析と改善計画

  🚨 Critical Issues（即座に対応すべき問題）

  1. PartyService における参照整合性制約

  問題: PartyServiceが他ContextのRepositoryを直接参照
  // PartyServiceが SyndicateRepository, FacilityRepository に依存
  if (syndicateRepository.existsByBorrowerId(id)) {
      throw new BusinessRuleViolationException("削除できません");
  }

  問題の重大度: ⭐⭐⭐⭐⭐ (Critical)
  - Bounded Context境界の破綻
  - テスタビリティの低下
  - 将来のマイクロサービス分割阻害

  2. SyndicateService における Party情報取得

  問題: SyndicateServiceがBorrowerRepository, InvestorRepositoryに依存
  // SyndicateDetailResponseDTO生成時の他Context参照
  String borrowerName = borrowerRepository.findById(syndicate.getBorrowerId())
      .map(Borrower::getName)
      .orElse("Unknown Borrower");

  問題の重大度: ⭐⭐⭐⭐ (High)
  - Anti-Corruption Layerの不在
  - データ整合性リスク（"Unknown"表示）

  ⚠️ Moderate Issues（計画的改善が必要な問題）

  3. FeePaymentService における Facility依存

  問題: 手数料計算でFacilityRepositoryを参照
  // 手数料配分計算時にFacilityの持分比率を参照
  private final FacilityRepository facilityRepository;

  問題の重大度: ⭐⭐⭐ (Medium)
  - ビジネスロジック的には必要な依存
  - Domain Eventで改善可能

  4. State Machine の Cross-Context調整

  問題: 複数Context間での状態遷移調整
  // FacilityService での Syndicate状態遷移
  syndicateStateMachine.sendEvent(SyndicateEvent.ACTIVATE);

  問題の重大度: ⭐⭐⭐ (Medium)
  - 複雑な状態依存関係
  - イベント駆動アーキテクチャで改善可能

  📋 Future Considerations（将来的課題）

  5. Transaction基盤の Shared Kernel依存

  問題の重大度: ⭐⭐ (Low)
  - 設計意図通りのShared Kernel
  - 現時点では適切な設計

  ---
  🛠️ 改善戦略と優先度

  Phase 1: 緊急対応（即座に実装）

  1. Domain Event導入
  // PartyDeletionAttempted イベント
  @DomainEvent
  public class PartyDeletionAttempted {
      private final Long partyId;
      private final PartyType partyType;
  }
  2. Event Handler実装
  @EventHandler
  public class SyndicateIntegrityHandler {
      public void handle(PartyDeletionAttempted event) {
          // Syndicate参加チェック
          // 違反時にBusiness Rule Violation Exception
      }
  }

  Phase 2: 中期改善（3-6ヶ月）

  1. Read Model分離
  // SyndicateDetailQueryService（CQRS）
  public class SyndicateDetailQueryService {
      // 非正規化されたRead専用モデル
      // PartyName等を事前結合
  }
  2. Integration Event導入
  // Party情報変更時のイベント
  @IntegrationEvent
  public class PartyInformationChanged {
      private final Long partyId;
      private final String newName;
  }

  Phase 3: 長期構想（1年以上）

  1. Microservices分割準備
    - API Gateway導入
    - Service Mesh検討
    - Distributed Transactionパターン
  2. Event Sourcing検討
    - Transaction履歴のEvent Store化
    - Read Modelの完全分離

  ---
  🎯 推奨アクション

  今すぐ実装すべき:

  ✅ Domain Event基盤構築
  ✅ PartyServiceの依存関係解消
  ✅ Unit TestでのMock化改善

  計画的改善:

  🔄 CQRSパターン導入
  🔄 Read Model分離
  🔄 Integration Event設計

  将来検討:

  🔮 Microservices分割
  🔮 Event Sourcing
  🔮 Distributed Architecture

  現在の設計は金融システムの実用性を重視した妥当な判断ですが、将来の拡張性を考慮すると段階的な改善が必要です。特にPhase 1の Domain 
  Event導入は、現在のモノリス構造を維持しながら境界を明確化できる最適解と考えます。

> しかし、Cross-Contextの依存は必要不可欠ではないでしょうか？\
  たとえば、\
    1. PartyService における参照整合性制約

    問題: PartyServiceが他ContextのRepositoryを直接参照\
  \
  については、Partyの状態を変更して良いか？（変更したり削除したりということ）は、\
  その時点で、Partyが他のコンテキストで使われているか？をチェックしないとわからないように思います。\
  \
  ひょっとすると、それが相互に依存を招いているので、DomainEventを記録してそれをチェックする、または、\
  他のコンテキストがPartyへの参照をしているよというのをPartyのStateに表す、（Partyを変更可能かどうかは、Party自体のStateだけで判断できるようにする）というようなことでしょうか？

⏺ 素晴らしい指摘です！その通りです。Cross-Context依存は必要不可欠な場合があり、重要なのはどのように依存を管理するかです。あなたの提案した解決策は非常にDDD的で理想的です。

  なぜCross-Context依存が必要不可欠なのか

  ビジネス現実としての依存関係

  Borrower削除可能性 = f(Syndicate参加状況, Facility参加状況, Loan残高状況)

  これは純粋なビジネスルールであり、技術的都合ではありません。金融システムではデータ整合性が最優先事項です。

  ---
  🎯 DDD的解決策の選択肢

  Option 1: Domain Event + Event Sourcing

  メリット: 結果整合性、スケーラビリティ
  デメリット: 複雑性、一時的不整合リスク

  // Party削除試行時
  @DomainEvent
  public class PartyDeletionRequested {
      private final Long partyId;
      private final PartyType partyType;
  }

  // 各Contextでイベント処理
  @EventHandler
  public class SyndicateParticipationChecker {
      public void handle(PartyDeletionRequested event) {
          if (hasActiveParticipation(event.getPartyId())) {
              throw new BusinessRuleViolationException("参加中のため削除不可");
          }
      }
  }

  金融システムでの問題: 非同期処理中の一時的不整合が業務上致命的

  ---
  Option 2: Party State内での参照状態管理 ⭐⭐⭐⭐⭐

  あなたの提案通り！最もDDD的で実用的

  @Entity
  public class Party {
      @Enumerated(EnumType.STRING)
      private PartyStatus status; // ACTIVE, RESTRICTED, LOCKED

      @ElementCollection
      private Set<ParticipationReference> participations;

      public boolean isDeletable() {
          return status == PartyStatus.ACTIVE && participations.isEmpty();
      }

      public void addParticipation(ContextType context, Long referenceId) {
          participations.add(new ParticipationReference(context, referenceId));
          if (status == PartyStatus.ACTIVE) {
              status = PartyStatus.RESTRICTED; // State Machine遷移
          }
      }
  }

  @Embeddable
  public class ParticipationReference {
      private ContextType contextType; // SYNDICATE, FACILITY, LOAN
      private Long referenceId;
      private LocalDateTime participationDate;
  }

  State Machine Integration

  // Party状態遷移
  ACTIVE → RESTRICTED (参加時)
  RESTRICTED → ACTIVE (全参加解除時)
  RESTRICTED → LOCKED (複数参加時)

  ---
  Option 3: Aggregate間の協調

  Domain Service活用

  @DomainService
  public class PartyDeletionService {

      public void attemptDeletion(Party party) {
          // 1. Party自身の削除可能性チェック
          if (!party.isDeletable()) {
              throw new BusinessRuleViolationException("Party状態により削除不可");
          }

          // 2. Cross-Context整合性チェック（必要最小限）
          validateCrossContextIntegrity(party);

          // 3. Domain Event発行
          party.markForDeletion();
      }
  }

  ---
  🏆 推奨解決策: Option 2（Party State管理）

  実装方針

  1. Party Aggregateの拡張

  public class Party {
      // 参加状況をParty自身が管理
      private Set<ParticipationReference> activeParticipations;
      private PartyStatus status;

      // ビジネスロジック
      public boolean canBeDeleted() {
          return activeParticipations.isEmpty() &&
                 status.allowsDeletion();
      }

      public void joinSyndicate(Long syndicateId) {
          activeParticipations.add(
              ParticipationReference.syndicate(syndicateId)
          );
          transitionToRestricted();
      }
  }

  2. 他Context側での参加記録

  // SyndicateService
  public void addMember(Long syndicateId, Long investorId) {
      // Syndicate側の処理
      syndicate.addMember(investorId);

      // Party側への通知（Domain Event）
      domainEventPublisher.publish(
          new PartyParticipationAdded(investorId, SYNDICATE, syndicateId)
      );
  }

  3. Domain Event Handler

  @EventHandler
  public class PartyParticipationHandler {

      public void handle(PartyParticipationAdded event) {
          Party party = partyRepository.findById(event.getPartyId());
          party.addParticipation(event.getContextType(), event.getReferenceId());
          partyRepository.save(party);
      }
  }

  ---
  🎯 メリット

  DDD原則への準拠

  ✅ Single Source of Truth: Party状態はParty Aggregateが管理
  ✅ Bounded Context独立性: 各Contextは自身の責務に集中
  ✅ Domain Model表現力: 参加状況がドメインモデルで表現される

  実用性

  ✅ 即座の整合性: Party削除可能性は即座に判定可能
  ✅ 運用安全性: 金融システムに必要なデータ整合性保証
  ✅ 段階的移行: 既存システムから段階的に移行可能

  ---
  あなたの指摘通り、Party自身のStateで削除可能性を判断するアプローチが最も健全です。これにより：

  1. Business Rule: PartyはSyndicateに参加中は削除不可 → **Party.isDeletable()**で判定
  2. Cross-Context通信: Domain Eventで参加状況を通知
  3. 状態管理: State Machineで適切な状態遷移

  この方式なら、DDD原則を保ちながら実用的な解決策となります。
  
```
