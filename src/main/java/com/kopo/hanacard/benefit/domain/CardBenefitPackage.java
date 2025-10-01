package com.kopo.hanacard.benefit.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 카드 혜택 패키지 엔티티
 * 올인원 그린라이프, 그린 모빌리티, 제로웨이스트 라이프 등
 */
@Entity
@Table(name = "card_benefit_packages")
@Getter
@NoArgsConstructor
public class CardBenefitPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "package_id")
    private Long id;

    @Column(name = "package_code", length = 50, nullable = false, unique = true)
    private String packageCode; // ALL_GREEN_LIFE, GREEN_MOBILITY, ZERO_WASTE_LIFE

    @Column(name = "package_name", length = 100, nullable = false)
    private String packageName; // 올인원 그린라이프 캐시백

    @Column(name = "package_description", columnDefinition = "TEXT")
    private String packageDescription;

    @Column(name = "package_icon", length = 100)
    private String packageIcon; // 아이콘 파일명

    @Column(name = "max_cashback_rate", precision = 5, scale = 2)
    private BigDecimal maxCashbackRate; // 최대 캐시백 비율

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 연관관계
    @OneToMany(mappedBy = "benefitPackage", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<CardBenefitCategory> categories = new ArrayList<>();

    @OneToMany(mappedBy = "benefitPackage", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<UserCardBenefit> userCardBenefits = new ArrayList<>();

    @Builder
    public CardBenefitPackage(String packageCode, String packageName, String packageDescription,
                             String packageIcon, BigDecimal maxCashbackRate, Boolean isActive) {
        this.packageCode = packageCode;
        this.packageName = packageName;
        this.packageDescription = packageDescription;
        this.packageIcon = packageIcon;
        this.maxCashbackRate = maxCashbackRate;
        this.isActive = isActive != null ? isActive : true;
    }

    // 비즈니스 메서드
    public void updatePackage(String packageName, String packageDescription, 
                             String packageIcon, BigDecimal maxCashbackRate) {
        this.packageName = packageName;
        this.packageDescription = packageDescription;
        this.packageIcon = packageIcon;
        this.maxCashbackRate = maxCashbackRate;
        this.updatedAt = LocalDateTime.now();
    }

    public void activate() {
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }
}
