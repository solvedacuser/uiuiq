package com.example.demo.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class CustomErrorController implements ErrorController {
    
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        Exception exception = (Exception) request.getAttribute("javax.servlet.error.exception");
        
        model.addAttribute("statusCode", statusCode);
        model.addAttribute("errorMessage", exception != null ? exception.getMessage() : "알 수 없는 오류가 발생했습니다.");
        
        if (statusCode != null) {
            if (statusCode == 404) {
                model.addAttribute("errorTitle", "페이지를 찾을 수 없습니다");
                model.addAttribute("errorDescription", "요청하신 페이지가 존재하지 않습니다.");
            } else if (statusCode == 500) {
                model.addAttribute("errorTitle", "서버 내부 오류");
                model.addAttribute("errorDescription", "서버에서 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            } else {
                model.addAttribute("errorTitle", "오류 발생");
                model.addAttribute("errorDescription", "예기치 않은 오류가 발생했습니다.");
            }
        }
        
        return "error";
    }
}
