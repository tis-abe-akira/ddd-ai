package com.example.syndicatelending.loan.entity;

/**
 * 返済方法を表すEnum。
 * ローンの返済スケジュール計算方法を定義する。
 */
public enum RepaymentMethod {
    /** 元利均等返済 */
    EQUAL_INSTALLMENT,

    /** 元本一括返済（満期時一括） */
    BULLET_PAYMENT
}
