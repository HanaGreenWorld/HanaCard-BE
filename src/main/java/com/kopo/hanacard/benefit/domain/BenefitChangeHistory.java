package com.kopo.hanacard.benefit.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 혜택 변경 이력 엔티티
 * 사용자의 혜택 패키지 변경 기록
 */
@Entity
@Table(name = "benefit_change_history")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BenefitChangeHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "card_product_id", nullable = false)
    private Long cardProductId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_package_id")
    private CardBenefitPackage fromPackage; // 변경 전 패키지

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "to_package_id", nullable = false)
    private CardBenefitPackage toPackage; // 변경 후 패키지

    @Column(name = "change_reason", length = 200)
    private String changeReason; // 변경 사유

    @Column(name = "change_date", nullable = false)
    private LocalDateTime changeDate; // 변경 일시

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate; // 적용 시작일


    // 정적 팩토리 메서드
    public static BenefitChangeHistory createChangeHistory(Long userId, Long cardProductId,
                                                          CardBenefitPackage fromPackage,
                                                          CardBenefitPackage toPackage,
                                                          String changeReason,
                                                          LocalDate effectiveDate) {
        return BenefitChangeHistory.builder()
                .userId(userId)
                .cardProductId(cardProductId)
                .fromPackage(fromPackage)
                .toPackage(toPackage)
                .changeReason(changeReason)
                .changeDate(LocalDateTime.now())
                .effectiveDate(effectiveDate)
                .build();
    }
}
