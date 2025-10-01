package com.kopo.hanacard.benefit.service;

import com.kopo.hanacard.benefit.domain.*;
import com.kopo.hanacard.benefit.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Optional;

/**
 * 카드 혜택 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CardBenefitService {

    private final CardBenefitPackageRepository packageRepository;
    private final CardBenefitCategoryRepository categoryRepository;
    private final CardBenefitDetailRepository detailRepository;
    private final UserCardBenefitRepository userCardBenefitRepository;
    private final BenefitChangeHistoryRepository changeHistoryRepository;

    /**
     * 모든 활성화된 혜택 패키지 조회
     */
    public List<CardBenefitPackage> getAllActivePackages() {
        log.info("모든 활성화된 혜택 패키지 조회");
        return packageRepository.findByIsActiveTrueOrderByCreatedAtAsc();
    }

    /**
     * 패키지 코드로 혜택 패키지 조회
     */
    public Optional<CardBenefitPackage> getPackageByCode(String packageCode) {
        log.info("패키지 코드로 조회: {}", packageCode);
        return packageRepository.findByPackageCode(packageCode);
    }

    /**
     * 패키지의 모든 카테고리와 상세 혜택 조회
     */
    public Map<String, Object> getPackageWithDetails(String packageCode) {
        log.info("패키지 상세 정보 조회: {}", packageCode);
        
        Optional<CardBenefitPackage> packageOpt = packageRepository.findByPackageCode(packageCode);
        if (packageOpt.isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 패키지입니다: " + packageCode);
        }

        CardBenefitPackage packageEntity = packageOpt.get();
        List<CardBenefitCategory> categories = categoryRepository.findByBenefitPackageIdOrderByDisplayOrderAsc(packageEntity.getId());
        
        Map<String, Object> result = new HashMap<>();
        result.put("package", packageEntity);
        result.put("categories", categories);
        
        // 각 카테고리의 상세 혜택도 포함
        List<Map<String, Object>> categoriesWithDetails = new ArrayList<>();
        for (CardBenefitCategory category : categories) {
            Map<String, Object> categoryMap = new HashMap<>();
            categoryMap.put("category", category);
            
            List<CardBenefitDetail> details = detailRepository.findByCategoryIdOrderByDisplayOrderAsc(category.getId());
            categoryMap.put("details", details);
            
            categoriesWithDetails.add(categoryMap);
        }
        result.put("categoriesWithDetails", categoriesWithDetails);
        
        return result;
    }

    /**
     * 사용자의 현재 활성화된 혜택 패키지 조회
     */
    public Optional<CardBenefitPackage> getCurrentActivePackage(Long userId, Long cardProductId) {
        log.info("사용자 현재 활성화된 혜택 패키지 조회: userId={}, cardProductId={}", userId, cardProductId);
        return packageRepository.findActivePackageByUserAndCard(userId, cardProductId);
    }

    /**
     * 사용자가 선택 가능한 패키지 목록 조회
     */
    public List<CardBenefitPackage> getAvailablePackagesForUser(Long userId, Long cardProductId) {
        log.info("사용자 선택 가능한 패키지 목록 조회: userId={}, cardProductId={}", userId, cardProductId);
        return packageRepository.findAvailablePackagesForUser(userId, cardProductId);
    }

    /**
     * 혜택 패키지 변경
     */
    @Transactional
    public void changeBenefitPackage(Long userId, Long cardProductId, String newPackageCode, String changeReason) {
        log.info("혜택 패키지 변경: userId={}, cardProductId={}, newPackageCode={}", userId, cardProductId, newPackageCode);
        
        // 새로운 패키지 조회
        Optional<CardBenefitPackage> newPackageOpt = packageRepository.findByPackageCode(newPackageCode);
        if (newPackageOpt.isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 패키지입니다: " + newPackageCode);
        }
        
        CardBenefitPackage newPackage = newPackageOpt.get();
        
        // 기존 활성화된 혜택 비활성화
        Optional<UserCardBenefit> currentBenefitOpt = userCardBenefitRepository
                .findByUserIdAndCardProductIdAndIsActiveTrue(userId, cardProductId);
        
        CardBenefitPackage fromPackage = null;
        if (currentBenefitOpt.isPresent()) {
            UserCardBenefit currentBenefit = currentBenefitOpt.get();
            fromPackage = currentBenefit.getBenefitPackage();
            currentBenefit.deactivate();
            userCardBenefitRepository.save(currentBenefit);
        }
        
        // 새로운 혜택 설정 (다음달 1일부터 적용)
        LocalDate nextMonth = LocalDate.now().plusMonths(1).withDayOfMonth(1);
        UserCardBenefit newBenefit = UserCardBenefit.builder()
                .userId(userId)
                .cardProductId(cardProductId)
                .benefitPackage(newPackage)
                .isActive(true)
                .effectiveDate(nextMonth)
                .build();
        
        userCardBenefitRepository.save(newBenefit);
        
        // 변경 이력 저장
        BenefitChangeHistory changeHistory = BenefitChangeHistory.createChangeHistory(
                userId, cardProductId, fromPackage, newPackage, changeReason, nextMonth);
        changeHistoryRepository.save(changeHistory);
        
        log.info("혜택 패키지 변경 완료: {} -> {}", 
                fromPackage != null ? fromPackage.getPackageName() : "없음", 
                newPackage.getPackageName());
    }

    /**
     * 사용자의 혜택 변경 이력 조회
     */
    public List<BenefitChangeHistory> getBenefitChangeHistory(Long userId, Long cardProductId) {
        log.info("혜택 변경 이력 조회: userId={}, cardProductId={}", userId, cardProductId);
        return changeHistoryRepository.findByUserIdAndCardProductIdOrderByChangeDateDesc(userId, cardProductId);
    }

    /**
     * 프론트엔드용 혜택 패키지 목록 조회 (API 응답 형태)
     */
    public Map<String, Object> getBenefitPackagesForApi(Long userId) {
        log.info("API용 혜택 패키지 목록 조회: userId={}", userId);
        
        Map<String, Object> result = new HashMap<>();
        
        // 모든 활성화된 패키지 조회
        List<CardBenefitPackage> allPackages = getAllActivePackages();
        
        // 사용자의 현재 활성화된 패키지 (첫 번째 카드 기준)
        Optional<CardBenefitPackage> currentPackage = getCurrentActivePackage(userId, 1L);
        
        result.put("currentPackage", currentPackage.map(CardBenefitPackage::getPackageName).orElse("올인원 그린라이프"));
        
        // 패키지 목록 구성
        List<Map<String, Object>> packages = new ArrayList<>();
        for (CardBenefitPackage packageEntity : allPackages) {
            Map<String, Object> packageMap = new HashMap<>();
            packageMap.put("packageName", packageEntity.getPackageName());
            packageMap.put("packageDescription", packageEntity.getPackageDescription());
            packageMap.put("packageIcon", packageEntity.getPackageIcon());
            packageMap.put("maxCashback", "최대 " + packageEntity.getMaxCashbackRate() + "% 캐시백");
            packageMap.put("isActive", currentPackage.map(p -> p.getId().equals(packageEntity.getId())).orElse(false));
            
            // 혜택 목록 구성
            List<CardBenefitCategory> categories = categoryRepository.findByBenefitPackageIdOrderByDisplayOrderAsc(packageEntity.getId());
            List<Map<String, Object>> benefits = new ArrayList<>();
            
            for (CardBenefitCategory category : categories) {
                List<CardBenefitDetail> details = detailRepository.findByCategoryIdOrderByDisplayOrderAsc(category.getId());
                for (CardBenefitDetail detail : details) {
                    Map<String, Object> benefit = new HashMap<>();
                    benefit.put("category", detail.getBenefitName());
                    benefit.put("cashbackRate", detail.getCashbackRate() + "%");
                    benefit.put("description", detail.getBenefitDescription());
                    benefit.put("icon", detail.getBenefitIcon());
                    benefits.add(benefit);
                }
            }
            
            packageMap.put("benefits", benefits);
            packages.add(packageMap);
        }
        
        result.put("packages", packages);
        return result;
    }
}
