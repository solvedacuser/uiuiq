package com.example.demo.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "youth_policy")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YouthPolicy {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "정책명은 필수입니다")
    @Column(nullable = false, length = 200)
    private String title; // 정책명
    
    @NotBlank(message = "정책 분야는 필수입니다")
    @Column(length = 100)
    private String category; // 정책 분야 (취업, 주거, 복지, 교육 등)
    
    @NotBlank(message = "정책 설명은 필수입니다")
    @Column(columnDefinition = "TEXT")
    private String description; // 정책 설명
    
    @Column(length = 100)
    private String targetAge; // 대상 연령 (예: 19~34세)
    
    @Column(columnDefinition = "TEXT")
    private String eligibility; // 신청 자격
    
    @Column(columnDefinition = "TEXT")
    private String benefits; // 지원 내용
    
    @NotBlank(message = "주관 기관은 필수입니다")
    @Column(length = 100)
    private String organizer; // 주관 기관
    
    @Column(length = 200)
    private String applicationMethod; // 신청 방법
    
    private LocalDate startDate; // 신청 시작일
    
    private LocalDate endDate; // 신청 마감일
    
    @Column(length = 500)
    private String websiteUrl; // 관련 웹사이트 URL
    
    @Column(length = 500)
    private String contactInfo; // 문의처
    
    @NotNull(message = "정책 상태는 필수입니다")
    @Enumerated(EnumType.STRING)
    private PolicyStatus status; // 정책 상태
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum PolicyStatus {
        ACTIVE("진행중"),
        CLOSED("마감"),
        UPCOMING("예정"),
        SUSPENDED("중단");
        
        private final String description;
        
        PolicyStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
