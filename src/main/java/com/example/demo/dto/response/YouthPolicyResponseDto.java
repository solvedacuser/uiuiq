package com.example.demo.dto.response;

import com.example.demo.domain.YouthPolicy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 청년정책 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YouthPolicyResponseDto {
    
    private Long id;
    private String title;
    private String category;
    private String description;
    private String targetAge;
    private String eligibility;
    private String benefits;
    private String organizer;
    private String applicationMethod;
    private LocalDate startDate;
    private LocalDate endDate;
    private String websiteUrl;
    private String contactInfo;
    private String status;
    private String statusDescription;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Entity를 DTO로 변환
     */
    public static YouthPolicyResponseDto from(YouthPolicy policy) {
        return YouthPolicyResponseDto.builder()
                .id(policy.getId())
                .title(policy.getTitle())
                .category(policy.getCategory())
                .description(policy.getDescription())
                .targetAge(policy.getTargetAge())
                .eligibility(policy.getEligibility())
                .benefits(policy.getBenefits())
                .organizer(policy.getOrganizer())
                .applicationMethod(policy.getApplicationMethod())
                .startDate(policy.getStartDate())
                .endDate(policy.getEndDate())
                .websiteUrl(policy.getWebsiteUrl())
                .contactInfo(policy.getContactInfo())
                .status(policy.getStatus().name())
                .statusDescription(policy.getStatus().getDescription())
                .createdAt(policy.getCreatedAt())
                .updatedAt(policy.getUpdatedAt())
                .build();
    }
}

