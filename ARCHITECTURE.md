# 프로젝트 아키텍처 문서

## 📁 프로젝트 구조 (레이어별 아키텍처)

```
src/main/java/com/example/demo/
│
├── DemoApplication.java              # Spring Boot 메인 애플리케이션
├── HelloController.java              # 루트 경로 리다이렉트
│
├── controller/                       # 프레젠테이션 계층
│   ├── BoardController.java         # 게시판 HTTP 요청 처리
│   ├── YouthPolicyController.java   # 청년정책 HTTP 요청 처리
│   └── CustomErrorController.java   # 에러 페이지 처리
│
├── service/                          # 비즈니스 로직 계층
│   ├── BoardService.java            # 게시판 비즈니스 로직
│   ├── YouthPolicyService.java      # 청년정책 비즈니스 로직
│   └── OnTongYouthApiService.java   # 외부 API 연동
│
├── repository/                       # 데이터 접근 계층
│   ├── BoardRepository.java         # 게시판 DB 접근
│   └── YouthPolicyRepository.java   # 청년정책 DB 접근
│
├── domain/                           # 도메인 모델 (엔티티)
│   ├── Board.java                   # 게시판 엔티티
│   └── YouthPolicy.java             # 청년정책 엔티티
│
├── dto/                              # 데이터 전송 객체
│   ├── request/                     # 요청 DTO
│   │   ├── BoardRequestDto.java
│   │   └── YouthPolicyRequestDto.java
│   ├── response/                    # 응답 DTO
│   │   ├── BoardResponseDto.java
│   │   └── YouthPolicyResponseDto.java
│   └── api/                         # 외부 API DTO (향후 확장)
│
├── exception/                        # 예외 처리
│   ├── GlobalExceptionHandler.java  # 전역 예외 핸들러
│   ├── ResourceNotFoundException.java
│   └── ApiException.java
│
└── config/                           # 설정 클래스
    ├── WebClientConfig.java         # WebClient 설정
    └── DataInitializer.java         # 초기 데이터 로드
```

## 🎨 프론트엔드 구조

```
src/main/resources/
│
├── static/                          # 정적 리소스
│   ├── css/
│   │   └── common.css              # 공통 스타일 (CSS 변수, 공통 클래스)
│   ├── js/
│   │   └── (향후 JavaScript 파일)
│   └── images/
│       └── (이미지 파일)
│
└── templates/                       # Thymeleaf 템플릿
    ├── fragments/                   # 재사용 가능한 Fragment
    │   └── layout.html             # head, navbar, footer, scripts
    │
    ├── board/                       # 게시판 화면
    │   ├── list.html               # 목록
    │   ├── view.html               # 상세보기
    │   └── writeForm.html          # 작성 폼
    │
    ├── youth-policy/                # 청년정책 화면
    │   ├── main.html               # 메인 (랜딩)
    │   ├── list.html               # 목록 (검색/필터)
    │   ├── view.html               # 상세보기
    │   ├── form.html               # 등록/수정 폼
    │   └── admin.html              # 관리자 페이지
    │
    └── error.html                   # 에러 페이지
```

### Fragment 시스템

**재사용 가능한 컴포넌트:**
```html
<!-- fragments/layout.html -->
<th:block th:fragment="head">         <!-- 공통 헤더 -->
<nav th:fragment="navbar">             <!-- 네비게이션 바 -->
<footer th:fragment="footer">          <!-- 푸터 -->
<th:block th:fragment="scripts">       <!-- 공통 스크립트 -->
```




