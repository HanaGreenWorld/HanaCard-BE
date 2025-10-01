package com.kopo.hanacard.benefit.repository;

import com.kopo.hanacard.benefit.domain.UserCardBenefit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 사용자 카드 혜택 Repository
 */
@Repository
public interface UserCardBenefitRepository extends JpaRepository<UserCardBenefit, Long> {

    /**
     * 사용자와 카드의 활성화된 혜택 조회
     */
    Optional<UserCardBenefit> findByUserIdAndCardProductIdAndIsActiveTrue(Long userId, Long cardProductId);

    /**
     * 사용자의 모든 카드 혜택 조회
     */
    List<UserCardBenefit> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 특정 카드의 모든 혜택 이력 조회
     */
    List<UserCardBenefit> findByUserIdAndCardProductIdOrderByCreatedAtDesc(Long userId, Long cardProductId);

    /**
     * 현재 유효한 혜택 조회 (오늘 날짜 기준)
     */
    @Query("SELECT ucb FROM UserCardBenefit ucb " +
           "WHERE ucb.userId = :userId AND ucb.cardProductId = :cardProductId " +
           "AND ucb.isActive = true " +
           "AND (ucb.effectiveDate IS NULL OR ucb.effectiveDate <= :today) " +
           "AND (ucb.expiryDate IS NULL OR ucb.expiryDate >= :today)")
    Optional<UserCardBenefit> findCurrentActiveBenefit(@Param("userId") Long userId, 
                                                       @Param("cardProductId") Long cardProductId,
                                                       @Param("today") LocalDate today);

    /**
     * 특정 날짜에 유효한 혜택 조회
     */
    @Query("SELECT ucb FROM UserCardBenefit ucb " +
           "WHERE ucb.userId = :userId AND ucb.cardProductId = :cardProductId " +
           "AND ucb.isActive = true " +
           "AND (ucb.effectiveDate IS NULL OR ucb.effectiveDate <= :date) " +
           "AND (ucb.expiryDate IS NULL OR ucb.expiryDate >= :date)")
    Optional<UserCardBenefit> findActiveBenefitOnDate(@Param("userId") Long userId, 
                                                      @Param("cardProductId") Long cardProductId,
                                                      @Param("date") LocalDate date);

    /**
     * 사용자의 모든 활성화된 혜택 조회
     */
    @Query("SELECT ucb FROM UserCardBenefit ucb " +
           "WHERE ucb.userId = :userId " +
           "AND ucb.isActive = true " +
           "AND (ucb.effectiveDate IS NULL OR ucb.effectiveDate <= :today) " +
           "AND (ucb.expiryDate IS NULL OR ucb.expiryDate >= :today)")
    List<UserCardBenefit> findCurrentActiveBenefitsByUser(@Param("userId") Long userId, 
                                                          @Param("today") LocalDate today);
}
