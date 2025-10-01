package com.kopo.hanacard.benefit.repository;

import com.kopo.hanacard.benefit.domain.CardBenefitDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 카드 혜택 상세 Repository
 */
@Repository
public interface CardBenefitDetailRepository extends JpaRepository<CardBenefitDetail, Long> {

    /**
     * 카테고리 ID로 상세 혜택 목록 조회 (정렬 순서대로)
     */
    List<CardBenefitDetail> findByCategoryIdOrderByDisplayOrderAsc(Long categoryId);

    /**
     * 패키지 코드로 모든 상세 혜택 조회
     */
    @Query("SELECT d FROM CardBenefitDetail d " +
           "JOIN d.category c " +
           "JOIN c.benefitPackage p " +
           "WHERE p.packageCode = :packageCode " +
           "ORDER BY c.displayOrder ASC, d.displayOrder ASC")
    List<CardBenefitDetail> findByPackageCodeOrderByDisplayOrder(@Param("packageCode") String packageCode);

    /**
     * 가맹점 카테고리로 조회
     */
    List<CardBenefitDetail> findByMerchantCategory(String merchantCategory);

    /**
     * 혜택명으로 조회
     */
    List<CardBenefitDetail> findByBenefitNameContainingIgnoreCase(String benefitName);
}
