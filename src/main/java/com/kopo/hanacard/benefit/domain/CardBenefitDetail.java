package com.kopo.hanacard.benefit.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 카드 혜택 상세 엔티티
 * 전기차 충전소, 대중교통, 리필스테이션 등 개별 혜택
 */
@Entity
@Table(name = "card_benefit_details")
@Getter
@NoArgsConstructor
public class CardBenefitDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detail_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private CardBenefitCategory category;

    @Column(name = "benefit_name", length = 100, nullable = false)
    private String benefitName; // 전기차 충전소, 대중교통

    @Column(name = "benefit_description", columnDefinition = "TEXT")
    private String benefitDescription;

    @Column(name = "cashback_rate", precision = 5, scale = 2, nullable = false)
    private BigDecimal cashbackRate; // 3.00, 2.00

    @Column(name = "merchant_category", length = 50)
    private String merchantCategory; // EV_CHARGING, PUBLIC_TRANSPORT

    @Column(name = "benefit_icon", length = 100)
    private String benefitIcon;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public CardBenefitDetail(CardBenefitCategory category, String benefitName,
                            String benefitDescription, BigDecimal cashbackRate,
                            String merchantCategory, String benefitIcon, Integer displayOrder) {
        this.category = category;
        this.benefitName = benefitName;
        this.benefitDescription = benefitDescription;
        this.cashbackRate = cashbackRate;
        this.merchantCategory = merchantCategory;
        this.benefitIcon = benefitIcon;
        this.displayOrder = displayOrder != null ? displayOrder : 0;
    }

    // 비즈니스 메서드
    public void updateDetail(String benefitName, String benefitDescription,
                            BigDecimal cashbackRate, String merchantCategory,
                            String benefitIcon, Integer displayOrder) {
        this.benefitName = benefitName;
        this.benefitDescription = benefitDescription;
        this.cashbackRate = cashbackRate;
        this.merchantCategory = merchantCategory;
        this.benefitIcon = benefitIcon;
        this.displayOrder = displayOrder != null ? displayOrder : 0;
        this.updatedAt = LocalDateTime.now();
    }
}
