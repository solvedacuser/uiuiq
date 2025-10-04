package com.example.demo.exception;

/**
 * 외부 API 호출 시 발생하는 예외
 */
public class ApiException extends RuntimeException {
    
    public ApiException(String message) {
        super(message);
    }
    
    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }
}

