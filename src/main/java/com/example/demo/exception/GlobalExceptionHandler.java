package com.example.demo.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * 전역 예외 처리 핸들러
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    /**
     * 리소스를 찾을 수 없을 때
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleResourceNotFoundException(ResourceNotFoundException e, Model model) {
        log.error("리소스를 찾을 수 없습니다: {}", e.getMessage());
        
        model.addAttribute("errorTitle", "리소스를 찾을 수 없습니다");
        model.addAttribute("errorDescription", e.getMessage());
        model.addAttribute("statusCode", 404);
        
        return "error";
    }
    
    /**
     * API 호출 실패 시
     */
    @ExceptionHandler(ApiException.class)
    public String handleApiException(ApiException e, Model model) {
        log.error("API 호출 중 오류 발생: {}", e.getMessage());
        
        model.addAttribute("errorTitle", "외부 API 호출 오류");
        model.addAttribute("errorDescription", "외부 서비스와의 통신 중 문제가 발생했습니다.");
        model.addAttribute("errorMessage", e.getMessage());
        model.addAttribute("statusCode", 502);
        
        return "error";
    }
    
    /**
     * IllegalArgumentException 처리
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(IllegalArgumentException e, Model model) {
        log.error("잘못된 인자: {}", e.getMessage());
        
        model.addAttribute("errorTitle", "잘못된 요청");
        model.addAttribute("errorDescription", e.getMessage());
        model.addAttribute("statusCode", 400);
        
        return "error";
    }
    
    /**
     * 그 외 모든 예외
     */
    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception e, Model model) {
        log.error("예상치 못한 오류 발생", e);
        
        model.addAttribute("errorTitle", "서버 오류");
        model.addAttribute("errorDescription", "예상치 못한 오류가 발생했습니다.");
        model.addAttribute("errorMessage", e.getMessage());
        model.addAttribute("statusCode", 500);
        
        return "error";
    }
}

