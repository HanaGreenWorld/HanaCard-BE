package com.kopo.hanacard.integration.controller;

import com.kopo.hanacard.common.dto.ApiResponse;
import com.kopo.hanacard.hanamoney.domain.HanamoneyMembership;
import com.kopo.hanacard.hanamoney.dto.HanamoneyMembershipResponse;
import com.kopo.hanacard.hanamoney.service.HanamoneyService;
import com.kopo.hanacard.integration.service.CardIntegrationService;
import com.kopo.hanacard.user.domain.User;
import com.kopo.hanacard.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 하나카드 통합 API 컨트롤러
 * 하나그린세상에서 하나카드 서비스 정보를 조회하는 API
 */
@RestController
@RequestMapping("/api/integration")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Card Integration API", description = "하나카드 통합 정보 조회 API")
public class CardIntegrationController {

    private final HanamoneyService hanamoneyService;
    private final CardIntegrationService cardIntegrationService;
    private final UserRepository userRepository;

    /**
     * 하나머니 정보 조회 API (하나그린세상용)
     * 
     * @param requestBody 요청 바디 (customerInfoToken, requestingService, consentToken, infoType)
     * @return 하나머니 정보 응답
     */
    @PostMapping("/hanamoney-info")
    @Operation(
        summary = "하나머니 정보 조회 (통합)",
        description = "하나그린세상에서 하나머니 정보를 조회하는 통합 API입니다."
    )
    public ResponseEntity<Map<String, Object>> getHanamoneyInfo(
            @RequestBody Map<String, String> requestBody) {
        
        try {
            log.info("하나머니 정보 조회 요청 - 요청서비스: {}", requestBody.get("requestingService"));
            
            // 요청 검증
            if (!"GREEN_WORLD".equals(requestBody.get("requestingService"))) {
                throw new IllegalArgumentException("허용되지 않은 요청 서비스입니다.");
            }
            
            // 요청에서 memberId 추출
            Long userId = extractMemberIdFromRequest(requestBody);
            
            // 하나머니 멤버십 조회
            HanamoneyMembership membership = hanamoneyService.getHanamoneyMembershipByUserId(userId);
            
            log.info("하나머니 멤버십 조회 성공 - 사용자ID: {}, 잔액: {}, 총적립: {}", 
                    userId, membership.getBalance(), membership.getTotalEarned());
            
            // 응답 데이터 구성
            Map<String, Object> responseData = Map.of(
                "membershipLevel", membership.getMembershipLevel(),
                "currentPoints", membership.getBalance(),
                "accumulatedPoints", membership.getTotalEarned(),
                "isSubscribed", membership.isActive(),
                "joinDate", membership.getCreatedAt().toString()
            );
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "하나머니 정보 조회 성공",
                "data", responseData
            );
            
            log.info("하나머니 정보 조회 성공 - 사용자ID: {}, 잔액: {}", userId, membership.getBalance());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("하나머니 정보 조회 실패", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "하나머니 정보 조회 실패: " + (e.getMessage() != null ? e.getMessage() : "알 수 없는 오류"));
            errorResponse.put("data", null);
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 카드 정보 조회 API (하나그린세상용)
     * 
     * @param memberId 회원 ID
     * @param consent 동의 여부
     * @return 카드 정보 응답
     */
    @GetMapping("/cards/{memberId}")
    @Operation(
        summary = "카드 정보 조회 (통합)",
        description = "하나그린세상에서 카드 정보를 조회하는 통합 API입니다."
    )
    public ResponseEntity<Map<String, Object>> getCardInfo(
            @PathVariable Long memberId,
            @RequestParam(defaultValue = "true") Boolean consent) {
        
        try {
            if (!Boolean.TRUE.equals(consent)) {
                throw new IllegalArgumentException("고객 동의가 필요합니다.");
            }
            
            log.info("카드 정보 조회 요청 - 회원ID: {}", memberId);
            
            // 카드 정보 조회 (실제로는 카드 서비스 구현 필요)
            Map<String, Object> cardData = cardIntegrationService.getCardInfo(memberId);
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "카드 정보 조회 성공",
                "data", cardData
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("카드 정보 조회 실패", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "카드 정보 조회 실패: " + (e.getMessage() != null ? e.getMessage() : "알 수 없는 오류"));
            errorResponse.put("data", null);
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 고객 정보 조회 API (하나그린세상용)
     * 
     * @param requestBody 요청 바디 (customerInfoToken, requestingService, consentToken, infoType)
     * @return 고객 정보 응답
     */
    @PostMapping("/customer-info")
    @Operation(
        summary = "고객 정보 조회 (통합)",
        description = "하나그린세상에서 고객 정보를 조회하는 통합 API입니다."
    )
    public ResponseEntity<Map<String, Object>> getCustomerInfo(
            @RequestBody Map<String, String> requestBody) {
        
        try {
            log.info("고객 정보 조회 요청 - 요청서비스: {}", requestBody.get("requestingService"));
            
            // 요청 검증
            if (!"GREEN_WORLD".equals(requestBody.get("requestingService"))) {
                throw new IllegalArgumentException("허용되지 않은 요청 서비스입니다.");
            }
            
            // 요청에서 memberId 추출
            Long userId = extractMemberIdFromRequest(requestBody);
            
            // 고객 정보 조회
            Map<String, Object> customerData = cardIntegrationService.getCustomerInfo(userId);
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "고객 정보 조회 성공",
                "data", customerData
            );
            
            log.info("고객 정보 조회 성공 - 사용자ID: {}", userId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("고객 정보 조회 실패", e);
            
            Map<String, Object> errorResponse = Map.of(
                "success", false,
                "message", "고객 정보 조회 실패: " + e.getMessage(),
                "data", null
            );
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 카드 거래내역 조회 API (하나그린세상용)
     * 
     * @param memberId 회원 ID
     * @return 카드 거래내역 응답
     */
    @GetMapping("/cards/{memberId}/transactions")
    @Operation(
        summary = "카드 거래내역 조회 (통합)",
        description = "하나그린세상에서 카드 거래내역을 조회하는 통합 API입니다."
    )
    public ResponseEntity<Map<String, Object>> getCardTransactions(@PathVariable Long memberId) {
        try {
            log.info("🔍 [통합 API] 카드 거래내역 조회 요청 - 회원ID: {}", memberId);
            log.info("🔍 [통합 API] 요청 URL: /api/integration/cards/{}/transactions", memberId);
            
            // 카드 거래내역 조회
            Map<String, Object> transactionData = cardIntegrationService.getCardTransactions(memberId);
            log.info("🔍 [통합 API] 거래내역 조회 결과: {}", transactionData);
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "카드 거래내역 조회 성공",
                "data", transactionData
            );
            
            log.info("카드 거래내역 조회 성공 - 회원ID: {}", memberId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("카드 거래내역 조회 실패 - 회원ID: {}", memberId, e);
            
            Map<String, Object> errorResponse = Map.of(
                "success", false,
                "message", "카드 거래내역 조회 실패: " + e.getMessage(),
                "data", null
            );
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * 월간 소비현황 조회 API (하나그린세상용)
     * 
     * @param memberId 회원 ID
     * @return 월간 소비현황 응답
     */
    @GetMapping("/cards/{memberId}/consumption/summary")
    @Operation(
        summary = "월간 소비현황 조회 (통합)",
        description = "하나그린세상에서 월간 소비현황을 조회하는 통합 API입니다."
    )
    public ResponseEntity<Map<String, Object>> getConsumptionSummary(@PathVariable Long memberId) {
        try {
            log.info("🔍 [통합 API] 월간 소비현황 조회 요청 - 회원ID: {}", memberId);
            log.info("🔍 [통합 API] 요청 URL: /api/integration/cards/{}/consumption/summary", memberId);
            
            // 월간 소비현황 조회
            Map<String, Object> consumptionData = cardIntegrationService.getConsumptionSummary(memberId);
            log.info("🔍 [통합 API] 소비현황 조회 결과: {}", consumptionData);
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "월간 소비현황 조회 성공",
                "data", consumptionData
            );
            
            log.info("월간 소비현황 조회 성공 - 회원ID: {}", memberId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("월간 소비현황 조회 실패 - 회원ID: {}", memberId, e);
            
            Map<String, Object> errorResponse = Map.of(
                "success", false,
                "message", "월간 소비현황 조회 실패: " + e.getMessage(),
                "data", null
            );
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 하나머니 적립 API (하나그린세상용)
     * 
     * @param requestBody 요청 바디 (customerInfoToken, amount, description)
     * @return 하나머니 적립 결과
     */
    @PostMapping("/hanamoney-earn")
    @Operation(
        summary = "하나머니 적립 (통합)",
        description = "하나그린세상에서 하나머니를 적립하는 통합 API입니다."
    )
    public ResponseEntity<Map<String, Object>> earnHanamoney(
            @RequestBody Map<String, Object> requestBody) {
        
        try {
            log.info("하나머니 적립 요청 - 요청: {}", requestBody);
            
            // 요청 검증
            if (!"GREEN_WORLD".equals(requestBody.get("requestingService"))) {
                throw new IllegalArgumentException("허용되지 않은 요청 서비스입니다.");
            }
            
            // 고객 정보 토큰에서 사용자 ID 추출
            Long userId = extractUserIdFromToken(requestBody.get("customerInfoToken").toString());
            Long amount = Long.valueOf(requestBody.get("amount").toString());
            String description = requestBody.get("description").toString();
            
            // 하나머니 적립
            HanamoneyMembership membership = hanamoneyService.earn(userId, amount, description);
            
            log.info("하나머니 적립 성공 - 사용자ID: {}, 적립금액: {}, 잔액: {}", 
                    userId, amount, membership.getBalance());
            
            // 응답 데이터 구성
            Map<String, Object> responseData = Map.of(
                "membershipLevel", membership.getMembershipLevel(),
                "currentPoints", membership.getBalance(),
                "accumulatedPoints", membership.getTotalEarned(),
                "isSubscribed", membership.isActive(),
                "joinDate", membership.getCreatedAt().toString()
            );
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "하나머니 적립 성공",
                "data", responseData
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("하나머니 적립 실패", e);
            
            Map<String, Object> errorResponse = Map.of(
                "success", false,
                "message", "하나머니 적립 실패: " + e.getMessage(),
                "data", null
            );
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 고객 정보 토큰에서 사용자 ID 추출
     * 실제로는 JWT 토큰 검증 및 파싱 로직이 필요
     */
    private Long extractUserIdFromToken(String customerInfoToken) {
        // 임시로 토큰에서 사용자 ID 추출 (실제로는 토큰 검증 필요)
        if (customerInfoToken == null || customerInfoToken.isEmpty()) {
            throw new IllegalArgumentException("고객 정보 토큰이 필요합니다.");
        }
        
        // 임시로 기본 사용자 ID 반환 (실제로는 토큰에서 추출)
        // TODO: 실제 토큰 검증 로직 구현 필요
        log.info("고객 정보 토큰에서 사용자 ID 추출 - 토큰: {}", customerInfoToken);
        return 1L;
    }
    
    /**
     * 통합 토큰 업데이트 (하나그린세상에서 호출)
     */
    @PostMapping("/update-unified-token")
    @Operation(
        summary = "통합 토큰 업데이트",
        description = "하나그린세상에서 통합 토큰을 업데이트하는 API입니다."
    )
    public ResponseEntity<Map<String, Object>> updateUnifiedToken(
            @RequestBody Map<String, String> requestBody) {
        try {
            String phoneNumber = requestBody.get("phoneNumber");
            String unifiedToken = requestBody.get("unifiedToken");
            
            log.info("통합 토큰 업데이트 요청 - 전화번호: {}, 토큰: {}", phoneNumber, unifiedToken);
            
            // 사용자 조회
            Optional<User> userOpt = userRepository.findByPhoneNumber(phoneNumber);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                
                // 통합 토큰 업데이트
                user.setGroupCustomerToken(unifiedToken);
                userRepository.save(user);
                
                log.info("통합 토큰 업데이트 완료 - 사용자ID: {}, 토큰: {}", user.getId(), unifiedToken);
                
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "통합 토큰 업데이트 완료",
                    "userId", user.getId(),
                    "phoneNumber", phoneNumber
                ));
            } else {
                log.warn("통합 토큰 업데이트 실패 - 사용자를 찾을 수 없음: {}", phoneNumber);
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "사용자를 찾을 수 없습니다"
                ));
            }
            
        } catch (Exception e) {
            log.error("통합 토큰 업데이트 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "통합 토큰 업데이트 중 오류가 발생했습니다"
            ));
        }
    }

    /**
     * 요청에서 memberId 추출
     */
    private Long extractMemberIdFromRequest(Map<String, String> requestBody) {
        String memberIdStr = requestBody.get("memberId");
        if (memberIdStr != null && !memberIdStr.isEmpty()) {
            try {
                return Long.valueOf(memberIdStr);
            } catch (NumberFormatException e) {
                log.warn("잘못된 memberId 형식: {}", memberIdStr);
            }
        }
        return 1L; // 기본값
    }
}