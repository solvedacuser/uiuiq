package com.example.demo.controller;

import com.example.demo.domain.YouthPolicy;
import com.example.demo.service.YouthPolicyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/youth-policy")
@RequiredArgsConstructor
@Slf4j
public class YouthPolicyController {
    
    private final YouthPolicyService youthPolicyService;
    
    // 메인 페이지 (정책 목록)
    @GetMapping({"", "/", "/list"})
    public String list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            Model model) {
        
        Page<YouthPolicy> policies;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            policies = youthPolicyService.searchByKeyword(keyword.trim(), page, size);
            model.addAttribute("keyword", keyword);
        } else if (category != null && !category.trim().isEmpty()) {
            log.info("카테고리 조회 요청: {}", category);
            policies = youthPolicyService.findByCategory(category, page, size);
            log.info("조회된 정책 수: {}", policies.getTotalElements());
            model.addAttribute("selectedCategory", category);
        } else {
            policies = youthPolicyService.findAll(page, size);
        }
        
        // 카테고리 목록과 통계 정보
        List<String> categories = youthPolicyService.getAvailableCategories();
        Map<String, Long> categoryStats = youthPolicyService.getCategoryStatistics();
        
        model.addAttribute("policies", policies);
        model.addAttribute("categories", categories);
        model.addAttribute("categoryStats", categoryStats);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", policies.getTotalPages());
        model.addAttribute("totalElements", policies.getTotalElements());
        
        return "youth-policy/list";
    }
    
    // 정책 상세 페이지
    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        Optional<YouthPolicy> policy = youthPolicyService.findById(id);
        
        if (policy.isPresent()) {
            model.addAttribute("policy", policy.get());
            return "youth-policy/view";
        } else {
            return "redirect:/youth-policy/list?error=notfound";
        }
    }
    
    // 정책 수정 폼
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Optional<YouthPolicy> policy = youthPolicyService.findById(id);
        
        if (policy.isPresent()) {
            model.addAttribute("policy", policy.get());
            model.addAttribute("categories", youthPolicyService.getAvailableCategories());
            model.addAttribute("statuses", YouthPolicy.PolicyStatus.values());
            return "youth-policy/form";
        } else {
            return "redirect:/youth-policy/list?error=notfound";
        }
    }
    
    // 정책 수정 처리
    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                        @Valid @ModelAttribute("policy") YouthPolicy policy,
                        BindingResult bindingResult,
                        Model model,
                        RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", youthPolicyService.getAvailableCategories());
            model.addAttribute("statuses", YouthPolicy.PolicyStatus.values());
            return "youth-policy/form";
        }
        
        try {
            youthPolicyService.update(id, policy);
            redirectAttributes.addFlashAttribute("successMessage", "정책이 성공적으로 수정되었습니다.");
            return "redirect:/youth-policy/" + id;
        } catch (Exception e) {
            log.error("정책 수정 중 오류 발생", e);
            model.addAttribute("errorMessage", "정책 수정 중 오류가 발생했습니다.");
            model.addAttribute("categories", youthPolicyService.getAvailableCategories());
            model.addAttribute("statuses", YouthPolicy.PolicyStatus.values());
            return "youth-policy/form";
        }
    }
    
    // 정책 삭제
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            youthPolicyService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "정책이 성공적으로 삭제되었습니다.");
        } catch (Exception e) {
            log.error("정책 삭제 중 오류 발생", e);
            redirectAttributes.addFlashAttribute("errorMessage", "정책 삭제 중 오류가 발생했습니다.");
        }
        
        return "redirect:/youth-policy/list";
    }
    
    // 검색 순위 TOP 5 페이지
    @GetMapping("/ranking")
    public String ranking(Model model) {
        try {
            List<YouthPolicy> topPolicies = youthPolicyService.findTop5ByInqCnt();
            model.addAttribute("topPolicies", topPolicies);
            return "youth-policy/ranking";
        } catch (Exception e) {
            log.error("검색 순위 페이지 로딩 중 오류 발생", e);
            model.addAttribute("errorTitle", "페이지 로딩 오류");
            model.addAttribute("errorDescription", "검색 순위 페이지를 불러오는 중 오류가 발생했습니다.");
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }
    }
    
    // 메인 페이지 리다이렉트
    @GetMapping("/main")
    public String main(Model model) {
        try {
            List<YouthPolicy> latestPolicies = youthPolicyService.findLatestPolicies();
            List<YouthPolicy> activePolicies = youthPolicyService.findActivePolicies();
            Map<String, Long> categoryStats = youthPolicyService.getCategoryStatistics();
            
            model.addAttribute("latestPolicies", latestPolicies != null ? latestPolicies : List.of());
            model.addAttribute("activePolicies", activePolicies != null ? activePolicies.stream().limit(5).toList() : List.of());
            model.addAttribute("categoryStats", categoryStats != null ? categoryStats : Map.of());
            model.addAttribute("categories", youthPolicyService.getAvailableCategories());
            
            return "youth-policy/main";
        } catch (Exception e) {
            log.error("메인 페이지 로딩 중 오류 발생", e);
            model.addAttribute("errorTitle", "페이지 로딩 오류");
            model.addAttribute("errorDescription", "메인 페이지를 불러오는 중 오류가 발생했습니다.");
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }
    }
    
    // 온통청년 API 관리자 페이지
    @GetMapping("/admin")
    public String adminPage(Model model) {
        try {
            Map<String, Object> status = youthPolicyService.getDatabaseStatus();
            model.addAttribute("dbStatus", status);
            return "youth-policy/admin";
        } catch (Exception e) {
            log.error("관리자 페이지 로딩 중 오류 발생", e);
            model.addAttribute("errorMessage", "관리자 페이지를 불러오는 중 오류가 발생했습니다.");
            return "error";
        }
    }
    
    // 온통청년 API에서 데이터 로드 (기존 데이터 유지)
    @PostMapping("/admin/load-api-data")
    public String loadApiData(RedirectAttributes redirectAttributes) {
        try {
            log.info("=== API 데이터 로드 시작 ===");
            int loadedCount = youthPolicyService.loadPoliciesFromApi();
            log.info("=== API 데이터 로드 완료: {}개 ===", loadedCount);
            
            if (loadedCount > 0) {
                redirectAttributes.addFlashAttribute("successMessage", 
                    String.format("온통청년 API에서 %d개의 정책을 성공적으로 로드했습니다.", loadedCount));
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "API에서 가져온 데이터가 없습니다. API 키를 확인하거나 로그를 확인해주세요.");
            }
        } catch (Exception e) {
            log.error("온통청년 API 데이터 로드 중 오류 발생", e);
            redirectAttributes.addFlashAttribute("errorMessage", 
                "온통청년 API 데이터 로드 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return "redirect:/youth-policy/admin";
    }
    
    // 데이터베이스 초기화 후 온통청년 API 데이터 로드
    @PostMapping("/admin/refresh-all-data")
    public String refreshAllData(RedirectAttributes redirectAttributes) {
        try {
            int loadedCount = youthPolicyService.refreshAllPoliciesFromApi();
            redirectAttributes.addFlashAttribute("successMessage", 
                String.format("데이터베이스를 초기화하고 온통청년 API에서 %d개의 정책을 새로 로드했습니다.", loadedCount));
        } catch (Exception e) {
            log.error("데이터베이스 초기화 및 온통청년 API 데이터 로드 중 오류 발생", e);
            redirectAttributes.addFlashAttribute("errorMessage", 
                "데이터 초기화 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return "redirect:/youth-policy/admin";
    }
    
    // 데이터베이스 상태 확인 API
    @GetMapping("/admin/status")
    @ResponseBody
    public Map<String, Object> getDatabaseStatus() {
        return youthPolicyService.getDatabaseStatus();
    }
    
    // 온통청년 API 연결 테스트
    @GetMapping("/admin/test-api")
    @ResponseBody
    public Map<String, Object> testApiConnection() {
        try {
            boolean isConnected = youthPolicyService.testApiConnection();
            return Map.of(
                "success", isConnected,
                "message", isConnected ? "API 연결 성공!" : "API 연결 실패",
                "timestamp", java.time.LocalDateTime.now()
            );
        } catch (Exception e) {
            log.error("API 테스트 중 오류", e);
            return Map.of(
                "success", false,
                "message", "API 테스트 중 오류: " + e.getMessage(),
                "timestamp", java.time.LocalDateTime.now()
            );
        }
    }
    
    // 디버깅용 엔드포인트
    @GetMapping("/debug/categories")
    @ResponseBody
    public Map<String, Object> debugCategories() {
        Map<String, Object> debug = new HashMap<>();
        
        // 전체 정책 수
        long totalPolicies = youthPolicyService.countAll();
        debug.put("totalPolicies", totalPolicies);
        
        // 카테고리별 정책 수
        Map<String, Long> categoryStats = youthPolicyService.getCategoryStatistics();
        debug.put("categoryStats", categoryStats);
        
        // 취업 카테고리 상세
        long employmentPoliciesCount = youthPolicyService.countByCategory("취업");
        debug.put("employmentPoliciesCount", employmentPoliciesCount);
        
        return debug;
    }
    
    // API 호출 테스트 엔드포인트
    @GetMapping("/debug/test-api-call")
    @ResponseBody
    public Map<String, Object> testApiCall() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("===== API 호출 테스트 시작 =====");
            int count = youthPolicyService.loadPoliciesFromApi();
            result.put("success", true);
            result.put("loadedCount", count);
            result.put("totalPolicies", youthPolicyService.countAll());
            result.put("message", "API 호출 성공: " + count + "개 로드됨");
            log.info("===== API 호출 테스트 완료: {}개 =====", count);
        } catch (Exception e) {
            log.error("===== API 호출 테스트 실패 =====", e);
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("errorType", e.getClass().getName());
            result.put("stackTrace", e.getStackTrace()[0].toString());
            result.put("message", "API 호출 실패: " + e.getMessage());
            
            // Caused by 확인
            if (e.getCause() != null) {
                result.put("cause", e.getCause().getMessage());
                result.put("causeType", e.getCause().getClass().getName());
            }
        }
        
        return result;
    }
    
    // API 연결만 테스트 (데이터 저장 안함)
    @GetMapping("/debug/raw-api-test")
    @ResponseBody
    public Map<String, Object> rawApiTest() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("===== Raw API 테스트 시작 =====");
            boolean connected = youthPolicyService.testApiConnection();
            result.put("apiConnected", connected);
            result.put("message", connected ? "API 연결 성공" : "API 연결 실패");
            log.info("===== Raw API 테스트 완료: {} =====", connected);
        } catch (Exception e) {
            log.error("===== Raw API 테스트 실패 =====", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
}

