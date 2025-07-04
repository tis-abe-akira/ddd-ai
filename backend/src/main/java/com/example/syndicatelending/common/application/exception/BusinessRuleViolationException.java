package com.example.syndicatelending.common.application.exception;

/**
 * 業務ルール違反が発生したことを示すアプリケーション例外。
 * (例: 利用可能額を超えるドローダウン要求など)
 */
public class BusinessRuleViolationException extends RuntimeException { // RuntimeExceptionとして定義

    public BusinessRuleViolationException(String message) {
        super(message);
    }

    public BusinessRuleViolationException(String message, Throwable cause) {
        super(message, cause);
    }
}
