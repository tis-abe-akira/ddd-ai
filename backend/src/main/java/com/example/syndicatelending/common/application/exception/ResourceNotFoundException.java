package com.example.syndicatelending.common.application.exception;

/**
 * 参照されたリソースが見つからなかったことを示すアプリケーション例外。
 * (例: 存在しないFacility IDが指定された場合など)
 */
public class ResourceNotFoundException extends RuntimeException { // RuntimeExceptionとして定義

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
