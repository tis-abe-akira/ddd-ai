package com.example.syndicatelending.common.infrastructure;

import com.example.syndicatelending.common.application.exception.BusinessRuleViolationException;
import com.example.syndicatelending.common.application.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException; // Bean Validationエラー
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest; // Request情報にアクセスする場合

/**
 * アプリケーション全体で発生する例外を処理するグローバルハンドラー。
 * 例外を捕捉し、適切なHTTPレスポンスにマッピングする。
 */
@RestControllerAdvice // REST Controller全体に適用される
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // レスポンスボディのフォーマットを定義するクラス (Inner Class または 別途定義)
    public static class ErrorResponse {
        private int status;
        private String error;
        private String message;
        // Timestamp, path など追加可能

        public ErrorResponse(int status, String error, String message) {
            this.status = status;
            this.error = error;
            this.message = message;
        }

        // Getters (for JSON serialization)
        public int getStatus() { return status; }
        public String getError() { return error; }
        public String getMessage() { return message; }
    }

    /**
     * ResourceNotFoundExceptionを処理 (HTTP 404 Not Found)
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        log.warn("Resource Not Found: {}", ex.getMessage()); // Warnレベルでログ出力

        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage()
        );
        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * BusinessRuleViolationExceptionを処理 (HTTP 400 Bad Request または 422 Unprocessable Entity)
     * 業務ルール違反はクライアント側の入力や状態に起因することが多いため400系が適切。
     * 400 (Bad Request) または 422 (Unprocessable Entity) が考えられます。
     * ここでは400を使用します。
     */
    @ExceptionHandler(BusinessRuleViolationException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRuleViolationException(BusinessRuleViolationException ex, WebRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST; // Or HttpStatus.UNPROCESSABLE_ENTITY (422)
        log.warn("Business Rule Violation: {}", ex.getMessage()); // Warnレベルでログ出力

         ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage()
        );
        return new ResponseEntity<>(errorResponse, status);
    }

     /**
     * Bean Validation失敗 (MethodArgumentNotValidException) を処理 (HTTP 400 Bad Request)
     * Controllerの@Validアノテーションによるバリデーションエラー
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, WebRequest request) {
         HttpStatus status = HttpStatus.BAD_REQUEST;
         String errorMessage = "Validation failed: " + ex.getBindingResult().getFieldError().getDefaultMessage(); // Simple message

         log.warn("Validation failed: {}", ex.getMessage()); // Warnレベルでログ出力

         ErrorResponse errorResponse = new ErrorResponse(
             status.value(),
             status.getReasonPhrase(),
             errorMessage // More detailed error info could be included from BindingResult
         );
         return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * その他の予期しない例外を処理 (HTTP 500 Internal Server Error)
     * 想定外のエラーはシステムの問題として扱い、詳細をクライアントに返しすぎない。
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllUncaughtException(Exception ex, WebRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        log.error("An unexpected error occurred", ex); // Errorレベルでスタックトレース込みでログ出力

         ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                "An internal server error occurred. Please try again later." // ユーザーには一般的なメッセージ
        );
        return new ResponseEntity<>(errorResponse, status);
    }

    // 他にもSpringが投げる様々な例外（例: HttpMessageNotReadableException, NoHandlerFoundExceptionなど）
    // をハンドリングすることも可能ですが、まずは主要なものから。
}
