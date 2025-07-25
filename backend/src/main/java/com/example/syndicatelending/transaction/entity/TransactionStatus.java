package com.example.syndicatelending.transaction.entity;

/**
 * 取引状態を表すenum
 * 
 * シンジケートローン管理システムにおける取引のライフサイクル管理を実現し、
 * 業務プロセスに応じた適切な状態制御を提供する。
 */
public enum TransactionStatus {
    /**
     * 下書き状態
     * - 取引が作成されたが、まだ処理が開始されていない状態
     * - 変更可能、承認待ち等の初期状態
     */
    DRAFT,
    
    /**
     * アクティブ状態
     * - 取引処理が開始され、現在実行中または運用中の状態
     * - 資金移動、配分計算等が進行中または完了済み
     */
    ACTIVE,
    
    /**
     * 完了
     * - 取引が正常に完了し、全ての処理が終了した状態
     * - 資金移動、配分、記録更新が全て完了
     */
    COMPLETED,
    
    /**
     * 失敗
     * - 取引処理中にエラーが発生し、処理が失敗した状態
     * - システムエラー、業務ルール違反、資金不足等
     */
    FAILED,
    
    /**
     * キャンセル
     * - 取引が意図的にキャンセルされた状態
     * - ユーザー操作、業務判断による取引中止
     */
    CANCELLED,
    
    /**
     * 返金済み（将来拡張用）
     * - 完了した取引が後に返金処理された状態
     * - 誤処理修正、契約変更等による返金
     */
    REFUNDED
}