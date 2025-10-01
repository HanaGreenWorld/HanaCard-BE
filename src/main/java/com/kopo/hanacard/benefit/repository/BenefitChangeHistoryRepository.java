package com.kopo.hanacard.benefit.repository;

import com.kopo.hanacard.benefit.domain.BenefitChangeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 혜택 변경 이력 Repository
 */
@Repository
public interface BenefitChangeHistoryRepository extends JpaRepository<BenefitChangeHistory, Long> {

    /**
     * 사용자의 혜택 변경 이력 조회 (최신순)
     */
    List<BenefitChangeHistory> findByUserIdOrderByChangeDateDesc(Long userId);

    /**
     * 특정 카드의 혜택 변경 이력 조회
     */
    List<BenefitChangeHistory> findByUserIdAndCardProductIdOrderByChangeDateDesc(Long userId, Long cardProductId);

    /**
     * 특정 기간의 혜택 변경 이력 조회
     */
    @Query("SELECT bch FROM BenefitChangeHistory bch " +
           "WHERE bch.userId = :userId " +
           "AND bch.changeDate >= :startDate " +
           "AND bch.changeDate <= :endDate " +
           "ORDER BY bch.changeDate DESC")
    List<BenefitChangeHistory> findByUserIdAndDateRange(@Param("userId") Long userId,
                                                        @Param("startDate") LocalDate startDate,
                                                        @Param("endDate") LocalDate endDate);

    /**
     * 특정 패키지로의 변경 이력 조회
     */
    List<BenefitChangeHistory> findByToPackageIdOrderByChangeDateDesc(Long toPackageId);

    /**
     * 특정 패키지에서의 변경 이력 조회
     */
    List<BenefitChangeHistory> findByFromPackageIdOrderByChangeDateDesc(Long fromPackageId);
}
