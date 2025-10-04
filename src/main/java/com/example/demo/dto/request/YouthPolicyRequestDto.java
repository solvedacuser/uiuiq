package com.example.demo.dto.request;

import com.example.demo.domain.YouthPolicy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 청년정책 등록/수정 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YouthPolicyRequestDto {
    
    @NotBlank(message = "정책명은 필수입니다")
    private String title;
    
    @NotBlank(message = "정책 분야는 필수입니다")
    private String category;
    
    @NotBlank(message = "정책 설명은 필수입니다")
    private String description;
    
    private String targetAge;
    private String eligibility;
    private String benefits;
    
    @NotBlank(message = "주관 기관은 필수입니다")
    private String organizer;
    
    private String applicationMethod;
    private LocalDate startDate;
    private LocalDate endDate;
    private String websiteUrl;
    private String contactInfo;
    
    @NotNull(message = "정책 상태는 필수입니다")
    private YouthPolicy.PolicyStatus status;
    
    /**
     * DTO를 Entity로 변환
     */
    public YouthPolicy toEntity() {
        return YouthPolicy.builder()
                .title(title)
                .category(category)
                .description(description)
                .targetAge(targetAge)
                .eligibility(eligibility)
                .benefits(benefits)
                .organizer(organizer)
                .applicationMethod(applicationMethod)
                .startDate(startDate)
                .endDate(endDate)
                .websiteUrl(websiteUrl)
                .contactInfo(contactInfo)
                .status(status)
                .build();
    }
}

