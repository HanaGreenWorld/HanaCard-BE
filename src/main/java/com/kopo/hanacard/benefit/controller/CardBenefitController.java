package com.kopo.hanacard.benefit.controller;

import com.kopo.hanacard.benefit.domain.CardBenefitPackage;
import com.kopo.hanacard.benefit.service.CardBenefitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 카드 혜택 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/card-benefits")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CardBenefitController {

    private final CardBenefitService cardBenefitService;

    /**
     * 모든 활성화된 혜택 패키지 조회
     */
    @GetMapping("/packages")
    public ResponseEntity<List<CardBenefitPackage>> getAllPackages() {
        log.info("모든 혜택 패키지 조회 요청");
        List<CardBenefitPackage> packages = cardBenefitService.getAllActivePackages();
        return ResponseEntity.ok(packages);
    }

    /**
     * 특정 패키지 상세 정보 조회
     */
    @GetMapping("/packages/{packageCode}")
    public ResponseEntity<Map<String, Object>> getPackageDetails(@PathVariable String packageCode) {
        log.info("패키지 상세 정보 조회 요청: {}", packageCode);
        try {
            Map<String, Object> packageDetails = cardBenefitService.getPackageWithDetails(packageCode);
            return ResponseEntity.ok(packageDetails);
        } catch (IllegalArgumentException e) {
            log.error("패키지 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 사용자의 현재 활성화된 혜택 패키지 조회
     */
    @GetMapping("/users/{userId}/current")
    public ResponseEntity<CardBenefitPackage> getCurrentPackage(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Long cardProductId) {
        log.info("사용자 현재 혜택 패키지 조회 요청: userId={}, cardProductId={}", userId, cardProductId);
        Optional<CardBenefitPackage> currentPackage = cardBenefitService.getCurrentActivePackage(userId, cardProductId);
        return currentPackage.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 사용자가 선택 가능한 패키지 목록 조회
     */
    @GetMapping("/users/{userId}/available")
    public ResponseEntity<List<CardBenefitPackage>> getAvailablePackages(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Long cardProductId) {
        log.info("사용자 선택 가능한 패키지 목록 조회 요청: userId={}, cardProductId={}", userId, cardProductId);
        List<CardBenefitPackage> availablePackages = cardBenefitService.getAvailablePackagesForUser(userId, cardProductId);
        return ResponseEntity.ok(availablePackages);
    }

    /**
     * 혜택 패키지 변경
     */
    @PostMapping("/users/{userId}/change")
    public ResponseEntity<Map<String, Object>> changeBenefitPackage(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Long cardProductId,
            @RequestBody Map<String, String> request) {
        
        String newPackageCode = request.get("packageCode");
        String changeReason = request.getOrDefault("changeReason", "사용자 요청");
        
        log.info("혜택 패키지 변경 요청: userId={}, cardProductId={}, newPackageCode={}", 
                userId, cardProductId, newPackageCode);
        
        try {
            cardBenefitService.changeBenefitPackage(userId, cardProductId, newPackageCode, changeReason);
            
            Map<String, Object> response = Map.of(
                    "success", true,
                    "message", "혜택 패키지가 성공적으로 변경되었습니다.",
                    "userId", userId,
                    "cardProductId", cardProductId,
                    "newPackageCode", newPackageCode
            );
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("혜택 패키지 변경 실패: {}", e.getMessage());
            Map<String, Object> errorResponse = Map.of(
                    "success", false,
                    "message", e.getMessage()
            );
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            log.error("혜택 패키지 변경 중 오류 발생", e);
            Map<String, Object> errorResponse = Map.of(
                    "success", false,
                    "message", "혜택 패키지 변경 중 오류가 발생했습니다."
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 사용자의 혜택 변경 이력 조회
     */
    @GetMapping("/users/{userId}/history")
    public ResponseEntity<List<Map<String, Object>>> getBenefitChangeHistory(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Long cardProductId) {
        log.info("혜택 변경 이력 조회 요청: userId={}, cardProductId={}", userId, cardProductId);
        // 이력 조회 로직은 나중에 구현
        return ResponseEntity.ok(List.of());
    }

    /**
     * 프론트엔드용 혜택 패키지 목록 조회 (기존 API와 호환)
     */
    @GetMapping("/users/{userId}/packages")
    public ResponseEntity<Map<String, Object>> getBenefitPackagesForApi(@PathVariable Long userId) {
        log.info("API용 혜택 패키지 목록 조회 요청: userId={}", userId);
        try {
            Map<String, Object> packages = cardBenefitService.getBenefitPackagesForApi(userId);
            return ResponseEntity.ok(packages);
        } catch (Exception e) {
            log.error("혜택 패키지 목록 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 패키지 코드로 패키지 조회
     */
    @GetMapping("/packages/code/{packageCode}")
    public ResponseEntity<CardBenefitPackage> getPackageByCode(@PathVariable String packageCode) {
        log.info("패키지 코드로 조회 요청: {}", packageCode);
        Optional<CardBenefitPackage> packageOpt = cardBenefitService.getPackageByCode(packageCode);
        return packageOpt.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
