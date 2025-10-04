package com.example.demo.service;

import com.example.demo.domain.YouthPolicy;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OnTongYouthApiService {
    
    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Value("${ontong.youth.api.key}")
    private String apiKey;
    
    @Value("${ontong.youth.api.url}")
    private String apiUrl;
    
    /**
     * 온통청년 API 연결 테스트
     */
    public boolean testApiConnection() {
        log.info("온통청년 API 연결 테스트 시작");
        try {
            String response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("")
                            .queryParam("apiKeyNm", apiKey)
                            .queryParam("pageNum", 1)
                            .queryParam("pageSize", 1)  // 최소한의 데이터만
                            .queryParam("rtnType", "json")
                            .build(apiUrl))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            log.info("API 응답: {}", response);
            log.info("API 응답 길이: {}", response != null ? response.length() : 0);
            log.info("API 응답 첫 100자: {}", response != null && response.length() > 100 ? 
                response.substring(0, 100) : response);
            
            return response != null && !response.trim().isEmpty();
        } catch (Exception e) {
            log.error("API 연결 테스트 실패: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 온통청년 API에서 청년정책 데이터를 가져옵니다
     */
    public List<YouthPolicy> fetchYouthPoliciesFromApi() {
        log.info("=== 온통청년 API 데이터 조회 시작 ===");
        log.info("API URL: {}", apiUrl);
        log.info("API Key: {}...", apiKey != null ? apiKey.substring(0, Math.min(10, apiKey.length())) : "null");
        
        try {
            // 완전한 URL 생성
            String fullUrl = String.format("%s?apiKeyNm=%s&pageNum=1&pageSize=250&rtnType=json", 
                apiUrl, apiKey);
            log.info("완전한 요청 URL: {}", fullUrl);
            
            // API 호출
            String response = webClient.get()
                    .uri(fullUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnNext(res -> {
                        log.info("✅ API 응답 수신 성공 - 길이: {} bytes", res != null ? res.length() : 0);
                        if (res != null && res.length() > 0) {
                            log.info("API 응답 샘플 (처음 1000자): {}", 
                                res.length() > 1000 ? res.substring(0, 1000) : res);
                        }
                    })
                    .doOnError(error -> {
                        log.error("❌ API 호출 실패 - 에러: {}", error.getMessage());
                        log.error("에러 상세:", error);
                    })
                    .block();
            
            if (response == null || response.trim().isEmpty() || "{}".equals(response.trim())) {
                log.error("API 응답이 비어있습니다. 폴백 데이터를 사용합니다.");
                return createFallbackData();
            }
            
            List<YouthPolicy> policies = parseApiResponse(response);
            
            if (policies.isEmpty()) {
                log.warn("파싱된 정책이 없습니다. 폴백 데이터를 사용합니다.");
                return createFallbackData();
            }
            
            log.info("=== ✅ API 조회 완료: {}개 정책 파싱 성공 ===", policies.size());
            return policies;
            
        } catch (Exception e) {
            log.error("❌ 온통청년 API 호출 중 예외 발생");
            log.error("예외 타입: {}", e.getClass().getName());
            log.error("예외 메시지: {}", e.getMessage());
            log.error("상세 스택:", e);
            
            if (e.getCause() != null) {
                log.error("원인 예외: {}", e.getCause().getClass().getName());
                log.error("원인 메시지: {}", e.getCause().getMessage());
            }
            
            log.error("폴백 데이터를 사용합니다.");
            return createFallbackData();
        }
    }
    
    /**
     * API 응답을 파싱하여 YouthPolicy 객체 리스트로 변환
     */
    private List<YouthPolicy> parseApiResponse(String response) {
        List<YouthPolicy> policies = new ArrayList<>();
        
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            
            // 루트 노드의 모든 필드 출력
            StringBuilder fieldNames = new StringBuilder();
            rootNode.fieldNames().forEachRemaining(name -> fieldNames.append(name).append(", "));
            log.info("=== API 응답 파싱 시작 ===");
            log.info("루트 레벨 필드: {}", fieldNames.toString());
            
            // 온통청년 API 응답 구조에 맞게 파싱
            JsonNode resultNode = rootNode.path("result");
            if (resultNode.isMissingNode()) {
                log.warn("result 노드를 찾을 수 없습니다.");
                log.warn("전체 응답 구조: {}", rootNode.toPrettyString().substring(0, Math.min(1000, rootNode.toPrettyString().length())));
                return createFallbackData();
            }
            
            // 정책 목록 추출
            JsonNode policyArray = resultNode.path("youthPolicyList");
            if (!policyArray.isArray()) {
                log.warn("youthPolicyList가 배열이 아닙니다. 타입: {}", policyArray.getNodeType());
                log.warn("result 노드 필드들: {}", resultNode.fieldNames());
                return createFallbackData();
            }
            
            log.info("정책 배열 크기: {}", policyArray.size());
            
            int successCount = 0;
            int failCount = 0;
            
            for (JsonNode policyNode : policyArray) {
                try {
                    // 연령 정보 조합
                    String minAge = getTextValue(policyNode, "sprtTrgtMinAge", "");
                    String maxAge = getTextValue(policyNode, "sprtTrgtMaxAge", "");
                    String targetAge = minAge.isEmpty() || maxAge.isEmpty() ? 
                        "연령 제한 없음" : minAge + "~" + maxAge + "세";
                    
                    // 카테고리 정보 (대분류, 중분류)
                    String lclsfNm = getTextValue(policyNode, "lclsfNm", "");
                    String mclsfNm = getTextValue(policyNode, "mclsfNm", "");
                    String category = lclsfNm.isEmpty() ? "기타" : 
                        (mclsfNm.isEmpty() ? lclsfNm : lclsfNm);
                    
                    YouthPolicy policy = YouthPolicy.builder()
                            .title(getTextValue(policyNode, "plcyNm", "정책명 없음"))
                            .category(category)
                            .description(getTextValue(policyNode, "plcyExplnCn", "설명 없음"))
                            .targetAge(targetAge)
                            .eligibility(getTextValue(policyNode, "addAplyQlfcCndCn", "자격 요건 정보 없음"))
                            .benefits(getTextValue(policyNode, "plcySprtCn", "지원 내용 없음"))
                            .organizer(getTextValue(policyNode, "operInstCdNm", "주관기관 없음"))
                            .applicationMethod(getTextValue(policyNode, "plcyAplyMthdCn", "신청 방법 없음"))
                            .startDate(parseDate(getTextValue(policyNode, "bizPrdBgngYmd", "")))
                            .endDate(parseDate(getTextValue(policyNode, "bizPrdEndYmd", "")))
                            .websiteUrl(getTextValue(policyNode, "aplyUrlAddr", ""))
                            .contactInfo(getTextValue(policyNode, "operInstPicNm", "문의처 없음"))
                            .status(YouthPolicy.PolicyStatus.ACTIVE)
                            .build();
                    
                    policies.add(policy);
                    successCount++;
                    if (successCount <= 3) {
                        log.info("정책 파싱 예시 #{}: {}", successCount, policy.getTitle());
                    }
                    
                } catch (Exception e) {
                    failCount++;
                    log.warn("개별 정책 파싱 실패 #{}: {}", failCount, e.getMessage());
                }
            }
            
            log.info("=== 파싱 완료: 성공 {}개, 실패 {}개 ===", successCount, failCount);
            
        } catch (Exception e) {
            log.error("API 응답 파싱 중 오류: {}", e.getMessage(), e);
            return createFallbackData();
        }
        
        return policies.isEmpty() ? createFallbackData() : policies;
    }
    
    /**
     * JSON 노드에서 텍스트 값을 안전하게 추출
     */
    private String getTextValue(JsonNode node, String fieldName, String defaultValue) {
        JsonNode fieldNode = node.path(fieldName);
        if (fieldNode.isMissingNode() || fieldNode.isNull()) {
            return defaultValue;
        }
        String value = fieldNode.asText().trim();
        return value.isEmpty() ? defaultValue : value;
    }
    
    /**
     * 날짜 문자열을 LocalDate로 변환
     */
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty() || "상시".equals(dateStr)) {
            return null;
        }
        
        try {
            // 다양한 날짜 형식 지원
            DateTimeFormatter[] formatters = {
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("yyyy.MM.dd"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd"),
                DateTimeFormatter.ofPattern("yyyyMMdd")
            };
            
            for (DateTimeFormatter formatter : formatters) {
                try {
                    return LocalDate.parse(dateStr, formatter);
                } catch (DateTimeParseException ignored) {
                    // 다음 포맷터 시도
                }
            }
            
            log.warn("날짜 파싱 실패: {}", dateStr);
            return null;
            
        } catch (Exception e) {
            log.warn("날짜 파싱 중 오류: {} - {}", dateStr, e.getMessage());
            return null;
        }
    }
    
    /**
     * API 호출 실패 시 사용할 폴백 데이터 생성 (100개)
     */
    private List<YouthPolicy> createFallbackData() {
        log.warn("=== 폴백 데이터 생성 중 (API 호출 실패) ===");
        
        List<YouthPolicy> fallbackPolicies = new ArrayList<>();
        String[] categories = {"취업", "창업", "주거", "복지", "교육", "문화", "참여·권리"};
        String[] organizers = {"고용노동부", "중소벤처기업부", "국토교통부", "보건복지부", "교육부", "문화체육관광부", "여성가족부"};
        
        // 100개의 샘플 정책 생성
        for (int i = 1; i <= 100; i++) {
            String category = categories[i % categories.length];
            String organizer = organizers[i % organizers.length];
            
            fallbackPolicies.add(YouthPolicy.builder()
                    .title(String.format("청년 정책 %d - %s 지원", i, category))
                    .category(category)
                    .description(String.format("청년들의 %s 분야 발전을 위한 종합 지원 정책입니다. 다양한 혜택과 기회를 제공하여 청년들의 성장을 돕습니다.", category))
                    .targetAge(i % 2 == 0 ? "19~34세" : "18~39세")
                    .eligibility(i % 3 == 0 ? 
                        "소득 제한 없음, 청년 누구나" : 
                        "기준 중위소득 120% 이하, 미취업 또는 저소득 청년")
                    .benefits(String.format("%s 분야 맞춤형 지원금 및 서비스 제공", category))
                    .organizer(organizer)
                    .applicationMethod(i % 2 == 0 ? "온라인 신청" : "방문 신청 또는 온라인 신청")
                    .startDate(LocalDate.now().minusMonths(i % 6))
                    .endDate(LocalDate.now().plusMonths((i % 12) + 1))
                    .websiteUrl(String.format("https://www.youthpolicy%d.go.kr", i))
                    .contactInfo(String.format("1%03d", 300 + (i % 100)))
                    .status(i % 10 == 0 ? YouthPolicy.PolicyStatus.UPCOMING : 
                           (i % 15 == 0 ? YouthPolicy.PolicyStatus.CLOSED : YouthPolicy.PolicyStatus.ACTIVE))
                    .build());
        }
        
        // 실제 정책 몇 개 추가
        fallbackPolicies.set(0, YouthPolicy.builder()
                .title("청년 취업성공패키지")
                .category("취업")
                .description("취업에 어려움을 겪는 청년에게 진로설계, 직업훈련, 취업알선 등을 단계별로 지원하는 종합적인 취업지원 서비스입니다.")
                .targetAge("18~34세")
                .eligibility("미취업 청년, 기준 중위소득 120% 이하")
                .benefits("진로상담, 직업훈련 참여지원금, 취업성공수당 등")
                .organizer("고용노동부")
                .applicationMethod("거주지 관할 고용센터 방문 신청")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(12))
                .websiteUrl("https://www.work.go.kr")
                .contactInfo("1350")
                .status(YouthPolicy.PolicyStatus.ACTIVE)
                .build());
        
        fallbackPolicies.set(1, YouthPolicy.builder()
                .title("청년 월세 한시 특별지원")
                .category("주거")
                .description("코로나19로 어려움을 겪는 청년의 주거비 부담을 덜어주기 위해 월세를 지원하는 정책입니다.")
                .targetAge("19~34세")
                .eligibility("무주택자, 부모와 별거, 기준 중위소득 60% 이하")
                .benefits("월 최대 20만원, 최대 12개월 지원")
                .organizer("국토교통부")
                .applicationMethod("복지로 온라인 신청")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(12))
                .websiteUrl("https://www.molit.go.kr")
                .contactInfo("1599-0001")
                .status(YouthPolicy.PolicyStatus.ACTIVE)
                .build());
        
        fallbackPolicies.set(2, YouthPolicy.builder()
                .title("청년도약계좌")
                .category("복지")
                .description("청년의 중장기 자산형성을 지원하기 위한 적립식 상품으로, 5년간 매월 최대 70만원까지 납입 시 정부가 매칭 지원금을 제공합니다.")
                .targetAge("19~34세")
                .eligibility("개인소득 3,600만원 이하, 가구소득 6,000만원 이하")
                .benefits("정부 매칭지원금 + 이자소득 비과세 혜택")
                .organizer("기획재정부")
                .applicationMethod("시중은행 방문 또는 인터넷뱅킹 신청")
                .startDate(LocalDate.now().minusDays(30))
                .endDate(LocalDate.of(2025, 12, 31))
                .websiteUrl("https://www.moef.go.kr")
                .contactInfo("044-215-2114")
                .status(YouthPolicy.PolicyStatus.ACTIVE)
                .build());
        
        fallbackPolicies.set(3, YouthPolicy.builder()
                .title("K-스타트업 창업지원 프로그램")
                .category("창업")
                .description("예비창업자와 초기창업자를 대상으로 창업교육, 멘토링, 사업화 자금을 종합적으로 지원하는 프로그램입니다.")
                .targetAge("만 39세 이하")
                .eligibility("예비창업자 또는 창업 7년 이내 초기창업자")
                .benefits("창업교육, 멘토링, 최대 1억원 사업화 자금 지원")
                .organizer("중소벤처기업부")
                .applicationMethod("온라인 신청")
                .startDate(LocalDate.now())
                .endDate(LocalDate.of(2025, 10, 31))
                .websiteUrl("https://www.k-startup.go.kr")
                .contactInfo("1357")
                .status(YouthPolicy.PolicyStatus.ACTIVE)
                .build());
        
        log.info("폴백 데이터 {}개 생성 완료", fallbackPolicies.size());
        return fallbackPolicies;
    }
}
