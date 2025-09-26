package com.example.demo.repository;

import com.example.demo.domain.YouthPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface YouthPolicyRepository extends JpaRepository<YouthPolicy, Long> {
    
    // 정책 상태별 조회
    List<YouthPolicy> findByStatus(YouthPolicy.PolicyStatus status);
    
    // 카테고리별 조회
    List<YouthPolicy> findByCategory(String category);
    
    // 제목으로 검색 (부분 일치)
    Page<YouthPolicy> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    
    // 카테고리와 상태로 조회
    Page<YouthPolicy> findByCategoryAndStatus(String category, YouthPolicy.PolicyStatus status, Pageable pageable);
    
    // 제목 또는 설명에서 키워드 검색
    @Query("SELECT y FROM YouthPolicy y WHERE " +
           "LOWER(y.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(y.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<YouthPolicy> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    // 진행중인 정책만 조회
    @Query("SELECT y FROM YouthPolicy y WHERE y.status = 'ACTIVE' ORDER BY y.createdAt DESC")
    List<YouthPolicy> findActivePolicies();
    
    // 카테고리별 정책 수 조회
    @Query("SELECT y.category, COUNT(y) FROM YouthPolicy y GROUP BY y.category")
    List<Object[]> countByCategory();
    
    // 최신 정책 조회 (상위 N개)
    List<YouthPolicy> findTop10ByOrderByCreatedAtDesc();
}

