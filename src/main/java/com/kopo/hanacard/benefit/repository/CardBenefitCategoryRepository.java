package com.kopo.hanacard.benefit.repository;

import com.kopo.hanacard.benefit.domain.CardBenefitCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 카드 혜택 카테고리 Repository
 */
@Repository
public interface CardBenefitCategoryRepository extends JpaRepository<CardBenefitCategory, Long> {

    /**
     * 패키지 ID로 카테고리 목록 조회 (정렬 순서대로)
     */
    List<CardBenefitCategory> findByBenefitPackageIdOrderByDisplayOrderAsc(Long packageId);

    /**
     * 패키지 코드로 카테고리 목록 조회
     */
    @Query("SELECT c FROM CardBenefitCategory c " +
           "JOIN c.benefitPackage p " +
           "WHERE p.packageCode = :packageCode " +
           "ORDER BY c.displayOrder ASC")
    List<CardBenefitCategory> findByPackageCodeOrderByDisplayOrder(@Param("packageCode") String packageCode);

    /**
     * 카테고리명으로 조회
     */
    List<CardBenefitCategory> findByCategoryNameContainingIgnoreCase(String categoryName);
}
