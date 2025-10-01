package com.kopo.hanacard.benefit.repository;

import com.kopo.hanacard.benefit.domain.CardBenefitPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 카드 혜택 패키지 Repository
 */
@Repository
public interface CardBenefitPackageRepository extends JpaRepository<CardBenefitPackage, Long> {

    /**
     * 패키지 코드로 조회
     */
    Optional<CardBenefitPackage> findByPackageCode(String packageCode);

    /**
     * 활성화된 모든 패키지 조회
     */
    List<CardBenefitPackage> findByIsActiveTrueOrderByCreatedAtAsc();

    /**
     * 패키지명으로 조회
     */
    List<CardBenefitPackage> findByPackageNameContainingIgnoreCase(String packageName);

    /**
     * 특정 사용자의 활성화된 혜택 패키지 조회
     */
    @Query("SELECT p FROM CardBenefitPackage p " +
           "JOIN p.userCardBenefits ucb " +
           "WHERE ucb.userId = :userId AND ucb.cardProductId = :cardProductId AND ucb.isActive = true")
    Optional<CardBenefitPackage> findActivePackageByUserAndCard(@Param("userId") Long userId, 
                                                               @Param("cardProductId") Long cardProductId);

    /**
     * 사용자가 선택 가능한 패키지 목록 조회 (현재 선택된 것 제외)
     */
    @Query("SELECT p FROM CardBenefitPackage p " +
           "WHERE p.isActive = true " +
           "AND p.id NOT IN (" +
           "    SELECT ucb.benefitPackage.id FROM UserCardBenefit ucb " +
           "    WHERE ucb.userId = :userId AND ucb.cardProductId = :cardProductId AND ucb.isActive = true" +
           ")")
    List<CardBenefitPackage> findAvailablePackagesForUser(@Param("userId") Long userId, 
                                                          @Param("cardProductId") Long cardProductId);
}
