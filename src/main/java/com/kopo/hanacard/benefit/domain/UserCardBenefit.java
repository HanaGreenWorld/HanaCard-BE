package com.kopo.hanacard.benefit.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 사용자 카드 혜택 설정 엔티티
 * 사용자가 선택한 혜택 패키지 정보
 */
@Entity
@Table(name = "user_card_benefits",
       uniqueConstraints = @UniqueConstraint(
           name = "unique_active_user_card",
           columnNames = {"user_id", "card_product_id", "is_active"}
       ))
@Getter
@NoArgsConstructor
public class UserCardBenefit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_benefit_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "card_product_id", nullable = false)
    private Long cardProductId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "package_id", nullable = false)
    @JsonIgnore
    private CardBenefitPackage benefitPackage;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate; // 혜택 적용 시작일

    @Column(name = "expiry_date")
    private LocalDate expiryDate; // 혜택 만료일 (NULL이면 무제한)

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public UserCardBenefit(Long userId, Long cardProductId, CardBenefitPackage benefitPackage,
                          Boolean isActive, LocalDate effectiveDate, LocalDate expiryDate) {
        this.userId = userId;
        this.cardProductId = cardProductId;
        this.benefitPackage = benefitPackage;
        this.isActive = isActive != null ? isActive : true;
        this.effectiveDate = effectiveDate;
        this.expiryDate = expiryDate;
    }

    // 비즈니스 메서드
    public void activate() {
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
        this.updatedAt = LocalDateTime.now();
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isCurrentlyActive() {
        LocalDate today = LocalDate.now();
        return isActive && 
               (effectiveDate == null || !effectiveDate.isAfter(today)) &&
               (expiryDate == null || !expiryDate.isBefore(today));
    }
}
