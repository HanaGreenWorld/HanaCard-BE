package com.kopo.hanacard.benefit.config;

import com.kopo.hanacard.benefit.domain.CardBenefitPackage;
import com.kopo.hanacard.benefit.domain.CardBenefitCategory;
import com.kopo.hanacard.benefit.domain.CardBenefitDetail;
import com.kopo.hanacard.benefit.repository.CardBenefitPackageRepository;
import com.kopo.hanacard.benefit.repository.CardBenefitCategoryRepository;
import com.kopo.hanacard.benefit.repository.CardBenefitDetailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * 카드 혜택 데이터 초기화
 * 한글 데이터가 올바르게 저장되도록 UTF-8 인코딩으로 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CardBenefitDataInitializer implements CommandLineRunner {

    private final CardBenefitPackageRepository packageRepository;
    private final CardBenefitCategoryRepository categoryRepository;
    private final CardBenefitDetailRepository detailRepository;

    @Override
    public void run(String... args) throws Exception {
        if (packageRepository.count() == 0) {
            log.info("카드 혜택 데이터 초기화 시작...");
            initializeCardBenefitData();
            log.info("카드 혜택 데이터 초기화 완료!");
        } else {
            log.info("카드 혜택 데이터가 이미 존재합니다. 초기화를 건너뜁니다.");
        }
    }

    private void initializeCardBenefitData() {
        // 1. 혜택 패키지 생성
        CardBenefitPackage allGreenLifePackage = CardBenefitPackage.builder()
                .packageCode("ALL_GREEN_LIFE")
                .packageName("올인원 그린라이프 캐시백")
                .packageDescription("친환경 생활 종합 혜택으로 전기차 충전, 대중교통, 공유 모빌리티 이용 시 최대 4% 캐시백을 제공합니다.")
                .packageIcon("hanaIcon3d_17.png")
                .maxCashbackRate(new BigDecimal("4.00"))
                .isActive(true)
                .build();
        
        CardBenefitPackage greenMobilityPackage = CardBenefitPackage.builder()
                .packageCode("GREEN_MOBILITY")
                .packageName("그린 모빌리티 캐시백")
                .packageDescription("친환경 교통수단 이용 시 특별 혜택을 제공하는 패키지입니다.")
                .packageIcon("hanaIcon3d_65.png")
                .maxCashbackRate(new BigDecimal("3.00"))
                .isActive(true)
                .build();
        
        CardBenefitPackage zeroWastePackage = CardBenefitPackage.builder()
                .packageCode("ZERO_WASTE_LIFE")
                .packageName("제로웨이스트 라이프 캐시백")
                .packageDescription("제로웨이스트 라이프스타일을 위한 리필샵, 무포장샵 이용 시 혜택을 제공합니다.")
                .packageIcon("hanaIcon3d_69.png")
                .maxCashbackRate(new BigDecimal("3.00"))
                .isActive(true)
                .build();

        List<CardBenefitPackage> packages = Arrays.asList(
                allGreenLifePackage, greenMobilityPackage, zeroWastePackage
        );
        packageRepository.saveAll(packages);

        // 2. 혜택 카테고리 생성
        // 올인원 그린라이프 패키지의 카테고리들
        CardBenefitCategory evChargingCategory = CardBenefitCategory.builder()
                .benefitPackage(allGreenLifePackage)
                .categoryName("전기차 충전소")
                .categoryDescription("완속/급속 충전소 이용 시 혜택")
                .cashbackRate("3%")
                .categoryIcon("hanaIcon3d_65.png")
                .displayOrder(1)
                .build();

        CardBenefitCategory publicTransportCategory = CardBenefitCategory.builder()
                .benefitPackage(allGreenLifePackage)
                .categoryName("대중교통")
                .categoryDescription("지하철, 버스 이용 시 혜택")
                .cashbackRate("2%")
                .categoryIcon("hanaIcon3d_67.png")
                .displayOrder(2)
                .build();

        CardBenefitCategory sharedMobilityCategory = CardBenefitCategory.builder()
                .benefitPackage(allGreenLifePackage)
                .categoryName("공유 모빌리티")
                .categoryDescription("공유킥보드, 따릉이 이용 시 혜택")
                .cashbackRate("4%")
                .categoryIcon("hanaIcon3d_69.png")
                .displayOrder(3)
                .build();

        // 그린 모빌리티 패키지의 카테고리들
        CardBenefitCategory bikeCategory = CardBenefitCategory.builder()
                .benefitPackage(greenMobilityPackage)
                .categoryName("공공자전거")
                .categoryDescription("따릉이, 공공자전거 이용 시 혜택")
                .cashbackRate("3%")
                .categoryIcon("hanaIcon3d_67.png")
                .displayOrder(1)
                .build();

        CardBenefitCategory electricScooterCategory = CardBenefitCategory.builder()
                .benefitPackage(greenMobilityPackage)
                .categoryName("전기 스쿠터")
                .categoryDescription("전기 스쿠터 이용 시 혜택")
                .cashbackRate("2%")
                .categoryIcon("hanaIcon3d_69.png")
                .displayOrder(2)
                .build();

        // 제로웨이스트 패키지의 카테고리들
        CardBenefitCategory refillCategory = CardBenefitCategory.builder()
                .benefitPackage(zeroWastePackage)
                .categoryName("리필스테이션")
                .categoryDescription("리필샵, 친환경 브랜드 이용 시 혜택")
                .cashbackRate("3%")
                .categoryIcon("hanaIcon3d_17.png")
                .displayOrder(1)
                .build();

        CardBenefitCategory ecoBrandCategory = CardBenefitCategory.builder()
                .benefitPackage(zeroWastePackage)
                .categoryName("친환경 브랜드")
                .categoryDescription("친환경 제품 판매점 이용 시 혜택")
                .cashbackRate("2%")
                .categoryIcon("hanaIcon3d_65.png")
                .displayOrder(2)
                .build();

        List<CardBenefitCategory> categories = Arrays.asList(
                evChargingCategory, publicTransportCategory, sharedMobilityCategory,
                bikeCategory, electricScooterCategory, refillCategory, ecoBrandCategory
        );
        categoryRepository.saveAll(categories);

        // 3. 혜택 상세 정보 생성
        List<CardBenefitDetail> details = Arrays.asList(
                // 전기차 충전소 상세
                CardBenefitDetail.builder()
                        .category(evChargingCategory)
                        .benefitName("전기차 충전소")
                        .benefitDescription("완속/급속 충전소")
                        .cashbackRate(new BigDecimal("3.00"))
                        .merchantCategory("EV_CHARGING")
                        .benefitIcon("hanaIcon3d_65.png")
                        .displayOrder(1)
                        .build(),

                // 대중교통 상세
                CardBenefitDetail.builder()
                        .category(publicTransportCategory)
                        .benefitName("대중교통")
                        .benefitDescription("지하철, 버스")
                        .cashbackRate(new BigDecimal("2.00"))
                        .merchantCategory("PUBLIC_TRANSPORT")
                        .benefitIcon("hanaIcon3d_67.png")
                        .displayOrder(1)
                        .build(),

                // 공유 모빌리티 상세
                CardBenefitDetail.builder()
                        .category(sharedMobilityCategory)
                        .benefitName("공유킥보드, 따릉이")
                        .benefitDescription("공유 모빌리티")
                        .cashbackRate(new BigDecimal("4.00"))
                        .merchantCategory("SHARED_MOBILITY")
                        .benefitIcon("hanaIcon3d_69.png")
                        .displayOrder(1)
                        .build(),

                // 공공자전거 상세
                CardBenefitDetail.builder()
                        .category(bikeCategory)
                        .benefitName("공공자전거")
                        .benefitDescription("따릉이, 공공자전거")
                        .cashbackRate(new BigDecimal("3.00"))
                        .merchantCategory("PUBLIC_BIKE")
                        .benefitIcon("hanaIcon3d_67.png")
                        .displayOrder(1)
                        .build(),

                // 전기 스쿠터 상세
                CardBenefitDetail.builder()
                        .category(electricScooterCategory)
                        .benefitName("전기 스쿠터")
                        .benefitDescription("전기 스쿠터")
                        .cashbackRate(new BigDecimal("2.00"))
                        .merchantCategory("ELECTRIC_SCOOTER")
                        .benefitIcon("hanaIcon3d_69.png")
                        .displayOrder(1)
                        .build(),

                // 리필스테이션 상세
                CardBenefitDetail.builder()
                        .category(refillCategory)
                        .benefitName("리필스테이션")
                        .benefitDescription("리필샵, 친환경 브랜드")
                        .cashbackRate(new BigDecimal("3.00"))
                        .merchantCategory("REFILL_STATION")
                        .benefitIcon("hanaIcon3d_17.png")
                        .displayOrder(1)
                        .build(),

                // 친환경 브랜드 상세
                CardBenefitDetail.builder()
                        .category(ecoBrandCategory)
                        .benefitName("친환경 브랜드")
                        .benefitDescription("친환경 제품 판매점")
                        .cashbackRate(new BigDecimal("2.00"))
                        .merchantCategory("ECO_BRAND")
                        .benefitIcon("hanaIcon3d_65.png")
                        .displayOrder(1)
                        .build()
        );

        detailRepository.saveAll(details);

        log.info("카드 혜택 데이터 초기화 완료: 패키지 {}개, 카테고리 {}개, 상세 혜택 {}개", 
                packages.size(), categories.size(), details.size());
    }
}

