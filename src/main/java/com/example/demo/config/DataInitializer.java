package com.example.demo.config;

import com.example.demo.repository.YouthPolicyRepository;
import com.example.demo.service.YouthPolicyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 애플리케이션 시작 시 청년정책 데이터를 자동으로 로드하는 컴포넌트
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {
    
    private final YouthPolicyService youthPolicyService;
    private final YouthPolicyRepository youthPolicyRepository;
    
    @Value("${youth.policy.auto-load.enabled:true}")
    private boolean autoLoadEnabled;
    
    @Value("${youth.policy.auto-load.size:100}")
    private int loadSize;
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!autoLoadEnabled) {
            log.info("자동 데이터 로드가 비활성화되어 있습니다.");
            return;
        }
        
        log.info("========================================");
        log.info("애플리케이션 시작 - 청년정책 데이터 초기화 시작");
        log.info("목표 로드 건수: {}개", loadSize);
        log.info("========================================");
        
        try {
            // 이미 데이터가 있는지 확인
            long existingCount = youthPolicyRepository.count();
            if (existingCount > 0) {
                log.info("이미 {}개의 정책 데이터가 존재합니다. 추가 로드를 건너뜁니다.", existingCount);
                log.info("데이터를 새로 로드하려면 관리자 페이지에서 '전체 데이터 새로고침'을 이용하세요.");
                return;
            }
            
            // 온통청년 API에서 정책 데이터 로드
            log.info("온통청년 API에서 데이터 로드 중...");
            int loadedCount = youthPolicyService.loadPoliciesFromApi();
            
            if (loadedCount > 0) {
                log.info("✅ 청년정책 데이터 로드 완료: {}개의 정책이 저장되었습니다.", loadedCount);
                log.info("📊 현재 데이터베이스 정책 수: {}개", youthPolicyRepository.count());
            } else {
                log.warn("⚠️ 온통청년 API에서 데이터를 가져오지 못했습니다.");
                log.warn("폴백 데이터(샘플 {}개)가 사용되었을 수 있습니다.", youthPolicyRepository.count());
            }
            
        } catch (Exception e) {
            log.error("❌ 청년정책 데이터 로드 중 오류 발생: {}", e.getMessage());
            log.error("애플리케이션은 계속 실행되지만, 초기 데이터가 없을 수 있습니다.");
            log.error("오류 상세: ", e);
        }
        
        log.info("========================================");
        log.info("데이터 초기화 완료 - 애플리케이션 준비됨");
        log.info("========================================");
    }
}

