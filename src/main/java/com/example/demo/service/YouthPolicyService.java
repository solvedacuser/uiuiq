package com.example.demo.service;

import com.example.demo.domain.YouthPolicy;
import com.example.demo.exception.ApiException;
import com.example.demo.exception.ResourceNotFoundException;
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
    private final OnTongYouthApiService onTongYouthApiService;
    
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
                .orElseThrow(() -> new com.example.demo.exception.ResourceNotFoundException("정책을 찾을 수 없습니다. ID: " + id));
        
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
        if (!youthPolicyRepository.existsById(id)) {
            throw new ResourceNotFoundException("삭제할 정책을 찾을 수 없습니다. ID: " + id);
        }
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
    
    // 조회수 기준 TOP 5 정책 조회
    public List<YouthPolicy> findTop5ByInqCnt() {
        Pageable pageable = PageRequest.of(0, 5);
        return youthPolicyRepository.findTop5ByOrderByInqCntDesc(pageable);
    }
    
    /**
     * 카테고리별 정책 수 조회 (디버그용)
     */
    public long countByCategory(String category) {
        return youthPolicyRepository.findByCategory(category).size();
    }
    
    /**
     * 전체 정책 수 조회
     */
    public long countAll() {
        return youthPolicyRepository.count();
    }
    
    /**
     * 온통청년 API 연결 테스트
     */
    public boolean testApiConnection() {
        return onTongYouthApiService.testApiConnection();
    }
    
    /**
     * 온통청년 API에서 데이터를 가져와서 H2 데이터베이스에 저장 (중복 제거)
     */
    @Transactional
    public int loadPoliciesFromApi() {
        log.info("온통청년 API에서 청년정책 데이터 로드 시작");
        
        try {
            // 온통청년 API에서 데이터 가져오기
            List<YouthPolicy> apiPolicies = onTongYouthApiService.fetchYouthPoliciesFromApi();
            
            if (apiPolicies.isEmpty()) {
                log.warn("온통청년 API에서 가져온 데이터가 없습니다");
                return 0;
            }
            
            log.info("API에서 {}개의 정책을 가져왔습니다", apiPolicies.size());
            
            // 기존 정책 제목 목록 조회 (중복 체크용)
            List<YouthPolicy> existingPolicies = youthPolicyRepository.findAll();
            java.util.Set<String> existingTitles = existingPolicies.stream()
                .map(p -> p.getTitle() + "|" + p.getOrganizer())
                .collect(java.util.stream.Collectors.toSet());
            
            // 중복되지 않은 정책만 필터링
            List<YouthPolicy> newPolicies = apiPolicies.stream()
                .filter(p -> !existingTitles.contains(p.getTitle() + "|" + p.getOrganizer()))
                .collect(java.util.stream.Collectors.toList());
            
            if (newPolicies.isEmpty()) {
                log.info("추가할 새로운 정책이 없습니다 (모두 중복)");
                return 0;
            }
            
            // 새 데이터만 저장
            List<YouthPolicy> savedPolicies = youthPolicyRepository.saveAll(newPolicies);
            
            log.info("중복 제외 후 {}개의 신규 정책을 저장했습니다", savedPolicies.size());
            return savedPolicies.size();
            
        } catch (Exception e) {
            log.error("온통청년 API 데이터 로드 중 오류 발생: {}", e.getMessage(), e);
            throw new ApiException("온통청년 API 데이터 로드 실패", e);
        }
    }
    
    /**
     * 데이터베이스 초기화 (기존 데이터 삭제 후 온통청년 API 데이터 로드)
     */
    @Transactional
    public int refreshAllPoliciesFromApi() {
        log.info("데이터베이스 초기화 및 온통청년 API 데이터 새로 로드");
        
        // 기존 데이터 모두 삭제
        youthPolicyRepository.deleteAll();
        log.info("기존 데이터 삭제 완료");
        
        // 온통청년 API에서 새 데이터 로드
        return loadPoliciesFromApi();
    }
    
    /**
     * 데이터베이스 상태 확인
     */
    public Map<String, Object> getDatabaseStatus() {
        long totalCount = youthPolicyRepository.count();
        long activeCount = youthPolicyRepository.findActivePolicies().size();
        Map<String, Long> categoryStats = getCategoryStatistics();
        
        return Map.of(
            "totalPolicies", totalCount,
            "activePolicies", activeCount,
            "categoryStatistics", categoryStats,
            "lastUpdated", java.time.LocalDateTime.now(),
            "dataSource", "온통청년 API"
        );
    }
}

