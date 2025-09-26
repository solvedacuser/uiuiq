package com.example.demo.service;

import com.example.demo.domain.YouthPolicy;
import com.example.demo.repository.YouthPolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class YouthPolicyService {
    
    private final YouthPolicyRepository youthPolicyRepository;
    
    // 모든 정책 조회 (페이징)
    public Page<YouthPolicy> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return youthPolicyRepository.findAll(pageable);
    }
    
    // 정책 상세 조회
    public Optional<YouthPolicy> findById(Long id) {
        return youthPolicyRepository.findById(id);
    }
    
    // 정책 등록
    @Transactional
    public YouthPolicy save(YouthPolicy youthPolicy) {
        return youthPolicyRepository.save(youthPolicy);
    }
    
    // 정책 수정
    @Transactional
    public YouthPolicy update(Long id, YouthPolicy updatePolicy) {
        YouthPolicy existingPolicy = youthPolicyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("정책을 찾을 수 없습니다. ID: " + id));
        
        existingPolicy.setTitle(updatePolicy.getTitle());
        existingPolicy.setCategory(updatePolicy.getCategory());
        existingPolicy.setDescription(updatePolicy.getDescription());
        existingPolicy.setTargetAge(updatePolicy.getTargetAge());
        existingPolicy.setEligibility(updatePolicy.getEligibility());
        existingPolicy.setBenefits(updatePolicy.getBenefits());
        existingPolicy.setOrganizer(updatePolicy.getOrganizer());
        existingPolicy.setApplicationMethod(updatePolicy.getApplicationMethod());
        existingPolicy.setStartDate(updatePolicy.getStartDate());
        existingPolicy.setEndDate(updatePolicy.getEndDate());
        existingPolicy.setWebsiteUrl(updatePolicy.getWebsiteUrl());
        existingPolicy.setContactInfo(updatePolicy.getContactInfo());
        existingPolicy.setStatus(updatePolicy.getStatus());
        
        return youthPolicyRepository.save(existingPolicy);
    }
    
    // 정책 삭제
    @Transactional
    public void deleteById(Long id) {
        youthPolicyRepository.deleteById(id);
    }
    
    // 키워드로 검색
    public Page<YouthPolicy> searchByKeyword(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return youthPolicyRepository.findByKeyword(keyword, pageable);
    }
    
    // 카테고리별 조회
    public Page<YouthPolicy> findByCategory(String category, int page, int size) {
        log.info("카테고리별 조회 시작: category={}, page={}, size={}", category, page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        // 전체 카테고리 데이터 수 확인
        long totalByCategory = youthPolicyRepository.findByCategory(category).size();
        log.info("해당 카테고리 전체 정책 수: {}", totalByCategory);
        
        // ACTIVE 상태만 조회
        Page<YouthPolicy> result = youthPolicyRepository.findByCategoryAndStatus(category, YouthPolicy.PolicyStatus.ACTIVE, pageable);
        log.info("ACTIVE 상태 정책 수: {}", result.getTotalElements());
        
        return result;
    }
    
    // 진행중인 정책만 조회
    public List<YouthPolicy> findActivePolicies() {
        return youthPolicyRepository.findActivePolicies();
    }
    
    // 최신 정책 조회
    public List<YouthPolicy> findLatestPolicies() {
        return youthPolicyRepository.findTop10ByOrderByCreatedAtDesc();
    }
    
    // 카테고리별 통계
    public Map<String, Long> getCategoryStatistics() {
        List<Object[]> results = youthPolicyRepository.countByCategory();
        return results.stream()
                .collect(Collectors.toMap(
                    result -> (String) result[0],
                    result -> (Long) result[1]
                ));
    }
    
    // 사용 가능한 카테고리 목록
    public List<String> getAvailableCategories() {
        return List.of("취업", "창업", "주거", "복지", "교육", "문화", "참여·권리");
    }
}

