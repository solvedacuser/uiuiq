package com.example.demo.controller;

import com.example.demo.domain.YouthPolicy;
import com.example.demo.repository.YouthPolicyRepository;
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
    private final YouthPolicyRepository youthPolicyRepository;
    
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
    
    // 정책 등록 폼
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("policy", new YouthPolicy());
        model.addAttribute("categories", youthPolicyService.getAvailableCategories());
        model.addAttribute("statuses", YouthPolicy.PolicyStatus.values());
        return "youth-policy/form";
    }
    
    // 정책 등록 처리
    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("policy") YouthPolicy policy,
                        BindingResult bindingResult,
                        Model model,
                        RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", youthPolicyService.getAvailableCategories());
            model.addAttribute("statuses", YouthPolicy.PolicyStatus.values());
            return "youth-policy/form";
        }
        
        try {
            YouthPolicy savedPolicy = youthPolicyService.save(policy);
            redirectAttributes.addFlashAttribute("successMessage", "정책이 성공적으로 등록되었습니다.");
            return "redirect:/youth-policy/" + savedPolicy.getId();
        } catch (Exception e) {
            log.error("정책 등록 중 오류 발생", e);
            model.addAttribute("errorMessage", "정책 등록 중 오류가 발생했습니다.");
            model.addAttribute("categories", youthPolicyService.getAvailableCategories());
            model.addAttribute("statuses", YouthPolicy.PolicyStatus.values());
            return "youth-policy/form";
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
    
    // 디버깅용 엔드포인트
    @GetMapping("/debug/categories")
    @ResponseBody
    public Map<String, Object> debugCategories() {
        Map<String, Object> debug = new HashMap<>();
        
        // 전체 정책 수
        long totalPolicies = youthPolicyRepository.count();
        debug.put("totalPolicies", totalPolicies);
        
        // 카테고리별 정책 수
        Map<String, Long> categoryStats = youthPolicyService.getCategoryStatistics();
        debug.put("categoryStats", categoryStats);
        
        // 취업 카테고리 상세
        List<YouthPolicy> employmentPolicies = youthPolicyRepository.findByCategory("취업");
        debug.put("employmentPoliciesCount", employmentPolicies.size());
        debug.put("employmentPoliciesActive", employmentPolicies.stream()
            .filter(p -> p.getStatus() == YouthPolicy.PolicyStatus.ACTIVE)
            .count());
        
        return debug;
    }
}

