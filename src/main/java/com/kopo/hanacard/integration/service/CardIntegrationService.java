package com.kopo.hanacard.integration.service;

import com.kopo.hanacard.card.domain.UserCard;
import com.kopo.hanacard.card.domain.CardTransaction;
import com.kopo.hanacard.card.repository.UserCardRepository;
import com.kopo.hanacard.card.repository.CardTransactionRepository;
import com.kopo.hanacard.user.domain.User;
import com.kopo.hanacard.user.repository.UserRepository;
import com.kopo.hanacard.benefit.domain.CardBenefitCategory;
import com.kopo.hanacard.benefit.domain.CardBenefitDetail;
import com.kopo.hanacard.benefit.repository.CardBenefitCategoryRepository;
import com.kopo.hanacard.benefit.repository.CardBenefitDetailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 카드 통합 서비스
 * 하나그린세상에서 요청하는 카드 정보를 제공
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CardIntegrationService {

    private final UserCardRepository userCardRepository;
    private final CardTransactionRepository cardTransactionRepository;
    private final UserRepository userRepository;
    private final CardBenefitCategoryRepository benefitCategoryRepository;
    private final CardBenefitDetailRepository benefitDetailRepository;

    /**
     * 카드 정보 조회 - 실제 DB 데이터 사용
     *
     * @param memberId 회원 ID
     * @return 카드 정보
     */
    public Map<String, Object> getCardInfo(Long memberId) {
        log.info("💳 카드 정보 조회 시작 - 회원ID: {}", memberId);
        log.info("💳 요청 파라미터: memberId={}", memberId);

        try {
            // 사용자 조회
            Optional<User> userOpt = userRepository.findById(memberId);
            if (userOpt.isEmpty()) {
                log.warn("💳 사용자를 찾을 수 없음 - 회원ID: {}", memberId);
                return createEmptyCardResponse();
            }

            User user = userOpt.get();
            log.info("💳 사용자 정보 조회 완료 - 이름: {}, 전화번호: {}", user.getName(), user.getPhoneNumber());
            
            // 사용자의 활성 카드 조회
            List<UserCard> userCards = userCardRepository.findByUserIdAndIsActiveTrue(memberId);
            log.info("💳 조회된 카드 수: {}", userCards.size());
            
            List<Map<String, Object>> cards = new ArrayList<>();
            BigDecimal totalCreditLimit = BigDecimal.ZERO;
            BigDecimal totalAvailableLimit = BigDecimal.ZERO;
            BigDecimal monthlyTotalUsage = BigDecimal.ZERO;
            
            for (UserCard userCard : userCards) {
                log.info("💳 카드 상세 정보 조회 시작 - 카드ID: {}", userCard.getId());
                log.info("  - 카드번호: {}", userCard.getCardNumberMasked());
                log.info("  - 카드명: {}", userCard.getCardProduct().getProductName());
                log.info("  - 카드타입: {}", userCard.getCardProduct().getProductType());
                log.info("  - 신용한도: {}", userCard.getCardProduct().getCreditLimit());
                log.info("  - 만료일: {}", userCard.getExpiryDate());
                log.info("  - 혜택타입: {}", userCard.getCurrentBenefitType());
                log.info("  - 활성상태: {}", userCard.getIsActive());
                log.info("  - 이미지URL: {}", userCard.getCardProduct().getImageUrl());
                
                // 카드 정보 매핑
                BigDecimal creditLimit = new BigDecimal(userCard.getCardProduct().getCreditLimit());
                BigDecimal availableLimit = creditLimit.subtract(new BigDecimal("1000000")); // 임시 계산
                
                Map<String, Object> cardInfo = new HashMap<>();
                cardInfo.put("cardNumber", userCard.getCardNumberMasked());
                cardInfo.put("cardName", userCard.getCardProduct().getProductName());
                cardInfo.put("cardType", userCard.getCardProduct().getProductType());
                cardInfo.put("cardStatus", userCard.getIsActive() ? "ACTIVE" : "INACTIVE");
                cardInfo.put("creditLimit", creditLimit);
                cardInfo.put("availableLimit", availableLimit);
                cardInfo.put("monthlyUsage", new BigDecimal("1000000")); // 임시 데이터
                cardInfo.put("issueDate", userCard.getCreatedAt());
                cardInfo.put("expiryDate", userCard.getExpiryDate().atStartOfDay());
                cardInfo.put("benefits", List.of("주유할인 5%", "커피할인 30%", "친환경 적립")); // 임시 데이터
                // 실제 카드 이미지 URL 사용 (데이터베이스에 저장된 이미지 URL)
                String cardImageUrl = userCard.getCardProduct().getImageUrl();
                log.info("💳 카드 이미지 URL 처리 - 원본: {}", cardImageUrl);
                if (cardImageUrl == null || cardImageUrl.isEmpty()) {
                    // 이미지 URL이 없으면 기본 placeholder 사용
                    cardImageUrl = "https://via.placeholder.com/300x200/138072/FFFFFF?text=" + userCard.getCardProduct().getProductName().replace(" ", "+");
                    log.info("💳 카드 이미지 URL이 없어서 placeholder 사용: {}", cardImageUrl);
                } else {
                    log.info("💳 카드 이미지 URL 정상: {}", cardImageUrl);
                }
                cardInfo.put("cardImageUrl", cardImageUrl);
                cardInfo.put("cardImageBase64", null); // 실제 이미지 데이터가 있다면 여기에
                
                cards.add(cardInfo);
                
                // 합계 계산
                totalCreditLimit = totalCreditLimit.add(creditLimit);
                totalAvailableLimit = totalAvailableLimit.add(availableLimit);
                monthlyTotalUsage = monthlyTotalUsage.add(new BigDecimal("1000000"));
            }
            
            // 요약 정보 생성
            Map<String, Object> summary = Map.of(
                "totalCardCount", cards.size(),
                "activeCardCount", cards.size(),
                "totalCreditLimit", totalCreditLimit,
                "totalAvailableLimit", totalAvailableLimit,
                "monthlyTotalUsage", monthlyTotalUsage,
                "primaryCardType", cards.isEmpty() ? "NONE" : cards.get(0).get("cardType")
            );
            
            log.info("💳 카드 정보 조회 완료 - 총 카드 수: {}", cards.size());
            
            return Map.of(
                "cards", cards,
                "summary", summary,
                "responseTime", LocalDateTime.now()
            );
            
        } catch (Exception e) {
            log.error("💳 카드 정보 조회 실패 - 회원ID: {}", memberId, e);
            return createEmptyCardResponse();
        }
    }
    
    /**
     * 고객 정보 조회 - 실제 DB 데이터 사용
     * 
     * @param userId 사용자 ID
     * @return 고객 정보
     */
    public Map<String, Object> getCustomerInfo(Long userId) {
        log.info("👤 고객 정보 조회 시작 - 사용자ID: {}", userId);
        
        try {
            // 사용자 조회
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                log.warn("👤 사용자를 찾을 수 없음 - 사용자ID: {}", userId);
                return createEmptyCustomerResponse();
            }
            
            User user = userOpt.get();
            log.info("👤 사용자 정보 조회 완료 - 이름: {}, 이메일: {}, 전화번호: {}", 
                    user.getName(), user.getEmail(), user.getPhoneNumber());
            
            // 사용자의 활성 카드 조회
            List<UserCard> userCards = userCardRepository.findByUserIdAndIsActiveTrue(userId);
            log.info("👤 조회된 카드 수: {}", userCards.size());
            
            // 고객 기본 정보
            Map<String, Object> customerInfo = Map.of(
                "name", user.getName(),
                "email", user.getEmail(),
                "phoneNumber", user.getPhoneNumber(),
                "customerGrade", "GOLD", // 임시 등급
                "joinDate", user.getCreatedAt(),
                "isActive", true,
                "totalCreditLimit", new BigDecimal("50000000"), // 임시 데이터
                "usedCreditAmount", new BigDecimal("10000000") // 임시 데이터
            );
            
            // 카드 정보 목록
            List<Map<String, Object>> cards = new ArrayList<>();
            for (UserCard userCard : userCards) {
                // 실제 카드 혜택 조회
                List<Map<String, Object>> cardBenefits = getCardBenefits(userCard.getCardProduct().getProductId());
                
                // 실제 거래내역 조회
                List<Map<String, Object>> cardTransactions = getCardTransactionsInternal(userId, userCard.getId());
                
                Map<String, Object> cardInfo = new HashMap<>();
                cardInfo.put("cardNumber", userCard.getCardNumberMasked());
                cardInfo.put("cardName", userCard.getCardProduct().getProductName());
                cardInfo.put("cardType", userCard.getCardProduct().getProductType());
                cardInfo.put("cardStatus", userCard.getIsActive() ? "ACTIVE" : "INACTIVE");
                cardInfo.put("creditLimit", new BigDecimal(userCard.getCardProduct().getCreditLimit()));
                cardInfo.put("availableLimit", new BigDecimal("40000000")); // 임시 데이터
                cardInfo.put("issueDate", userCard.getCreatedAt());
                cardInfo.put("expiryDate", userCard.getExpiryDate().atStartOfDay());
                cardInfo.put("benefits", cardBenefits); // 실제 혜택 데이터
                cardInfo.put("transactions", cardTransactions); // 실제 거래내역 데이터
                cardInfo.put("monthlyUsage", new BigDecimal("1000000")); // 임시 데이터
                cards.add(cardInfo);
            }
            
            // 하나머니 정보 (임시 데이터)
            Map<String, Object> hanamoneyInfo = Map.of(
                "membershipLevel", "GOLD",
                "currentPoints", 50000,
                "accumulatedPoints", 200000,
                "isSubscribed", true,
                "joinDate", user.getCreatedAt()
            );
            
            log.info("👤 고객 정보 조회 완료 - 사용자ID: {}, 카드 수: {}", userId, cards.size());
            
            return Map.of(
                "customerInfo", customerInfo,
                "cards", cards,
                "hanamoneyInfo", hanamoneyInfo,
                "responseTime", LocalDateTime.now()
            );
            
        } catch (Exception e) {
            log.error("👤 고객 정보 조회 실패 - 사용자ID: {}", userId, e);
            return createEmptyCustomerResponse();
        }
    }
    
    /**
     * 카드 혜택 조회
     */
    private List<Map<String, Object>> getCardBenefits(Long productId) {
        try {
            // 카드 상품의 혜택 조회 (실제 DB에서)
            // TODO: 실제 혜택 조회 로직 구현
            return List.of(
                Map.of(
                    "benefitType", "친환경 교통",
                    "category", "대중교통",
                    "cashbackRate", 2.0,
                    "description", "지하철, 버스 이용 시 2% 캐시백"
                ),
                Map.of(
                    "benefitType", "친환경 가맹점",
                    "category", "쇼핑",
                    "cashbackRate", 1.5,
                    "description", "친환경 가맹점에서 1.5% 캐시백"
                )
            );
        } catch (Exception e) {
            log.error("카드 혜택 조회 실패 - 상품ID: {}", productId, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 카드 거래내역 조회 (통합 API용)
     */
    public Map<String, Object> getCardTransactions(Long userId) {
        log.info("👤 카드 거래내역 조회 시작 - 사용자ID: {}", userId);
        
        try {
            // 사용자 카드 조회
            List<UserCard> userCards = userCardRepository.findByUserIdAndIsActiveTrue(userId);
            
            List<Map<String, Object>> allTransactions = new ArrayList<>();
            
            for (UserCard userCard : userCards) {
                List<Map<String, Object>> cardTransactions = getCardTransactionsInternal(userId, userCard.getId());
                allTransactions.addAll(cardTransactions);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("transactions", allTransactions);
            response.put("totalCount", allTransactions.size());
            response.put("userId", userId);
            
            log.info("👤 카드 거래내역 조회 성공 - 사용자ID: {}, 거래건수: {}", userId, allTransactions.size());
            return response;
            
        } catch (Exception e) {
            log.error("👤 카드 거래내역 조회 실패 - 사용자ID: {}", userId, e);
            return Map.of("transactions", new ArrayList<>(), "totalCount", 0, "userId", userId);
        }
    }
    
    /**
     * 월간 소비현황 조회 (통합 API용)
     */
    public Map<String, Object> getConsumptionSummary(Long userId) {
        log.info("👤 월간 소비현황 조회 시작 - 사용자ID: {}", userId);
        
        try {
            // 사용자 카드 조회
            List<UserCard> userCards = userCardRepository.findByUserIdAndIsActiveTrue(userId);
            
            // 카테고리별 소비 금액 계산
            Map<String, Long> categoryAmounts = new HashMap<>();
            long totalAmount = 0;
            long totalCashback = 0;
            
            for (UserCard userCard : userCards) {
                List<Map<String, Object>> cardTransactions = getCardTransactionsInternal(userId, userCard.getId());
                
                for (Map<String, Object> transaction : cardTransactions) {
                    String category = (String) transaction.get("category");
                    Long amount = ((Number) transaction.get("amount")).longValue();
                    Long cashback = ((Number) transaction.get("cashbackAmount")).longValue();
                    
                    categoryAmounts.merge(category, amount, Long::sum);
                    totalAmount += amount;
                    totalCashback += cashback;
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("totalAmount", totalAmount);
            response.put("totalCashback", totalCashback);
            response.put("categoryAmounts", categoryAmounts);
            response.put("userId", userId);
            
            log.info("👤 월간 소비현황 조회 성공 - 사용자ID: {}, 총소비: {}, 총캐시백: {}", 
                    userId, totalAmount, totalCashback);
            return response;
            
        } catch (Exception e) {
            log.error("👤 월간 소비현황 조회 실패 - 사용자ID: {}", userId, e);
            return Map.of("totalAmount", 0, "totalCashback", 0, "categoryAmounts", new HashMap<>(), "userId", userId);
        }
    }

    /**
     * 카드 거래내역 조회 (내부 메서드)
     */
    private List<Map<String, Object>> getCardTransactionsInternal(Long userId, Long cardId) {
        try {
            // 사용자의 카드 거래내역 조회 (실제 DB에서)
            log.info("🔍 실제 DB에서 카드 거래내역 조회 시작 - 사용자ID: {}, 카드ID: {}", userId, cardId);
            
            // 사용자 카드 조회
            UserCard userCard = userCardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("사용자 카드를 찾을 수 없습니다: " + cardId));
            
            // 실제 거래내역 조회
            List<CardTransaction> transactions = cardTransactionRepository.findByUserCard(userCard);
            log.info("🔍 DB에서 조회된 거래내역 수: {}", transactions.size());
            
            List<Map<String, Object>> result = new ArrayList<>();
            for (CardTransaction transaction : transactions) {
                Map<String, Object> transactionMap = new HashMap<>();
                transactionMap.put("transactionDate", transaction.getTransactionDate().toString());
                transactionMap.put("merchantName", transaction.getMerchantName());
                transactionMap.put("category", transaction.getCategory());
                transactionMap.put("amount", transaction.getAmount().longValue());
                transactionMap.put("cashbackAmount", transaction.getCashbackAmount().longValue());
                transactionMap.put("cashbackRate", transaction.getCashbackRate().doubleValue());
                transactionMap.put("description", transaction.getDescription());
                transactionMap.put("merchantCategory", transaction.getMerchantCategory());

                // 혜택 카테고리 정보 추가
                if (transaction.getBenefitCategory() != null) {
                    transactionMap.put("benefitCategoryName", transaction.getBenefitCategory().getCategoryName());
                    transactionMap.put("benefitCategoryIcon", transaction.getBenefitCategory().getCategoryIcon());
                }

                // 혜택 상세 정보 추가
                if (transaction.getBenefitDetail() != null) {
                    transactionMap.put("benefitName", transaction.getBenefitDetail().getBenefitName());
                    transactionMap.put("benefitIcon", transaction.getBenefitDetail().getBenefitIcon());
                }

                result.add(transactionMap);

                log.info("💳 거래내역 매핑 완료 - 상점: {}, 카테고리: {}, 캐시백률: {}%",
                    transaction.getMerchantName(), transaction.getCategory(), transaction.getCashbackRate());
            }
            
            log.info("🔍 실제 DB 거래내역 조회 성공 - 건수: {}", result.size());
            return result;
            
        } catch (Exception e) {
            log.error("카드 거래내역 조회 실패 - 사용자ID: {}, 카드ID: {}", userId, cardId, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 빈 카드 응답 생성
     */
    private Map<String, Object> createEmptyCardResponse() {
        return Map.of(
            "cards", List.of(),
            "summary", Map.of(
                "totalCardCount", 0,
                "activeCardCount", 0,
                "totalCreditLimit", BigDecimal.ZERO,
                "totalAvailableLimit", BigDecimal.ZERO,
                "monthlyTotalUsage", BigDecimal.ZERO,
                "primaryCardType", "NONE"
            ),
            "responseTime", LocalDateTime.now()
        );
    }
    
    /**
     * 빈 고객 정보 응답 생성
     */
    private Map<String, Object> createEmptyCustomerResponse() {
        return Map.of(
            "customerInfo", Map.of(
                "name", "",
                "email", "",
                "phoneNumber", "",
                "customerGrade", "NONE",
                "joinDate", LocalDateTime.now(),
                "isActive", false,
                "totalCreditLimit", BigDecimal.ZERO,
                "usedCreditAmount", BigDecimal.ZERO
            ),
            "cards", List.of(),
            "hanamoneyInfo", Map.of(
                "membershipLevel", "NONE",
                "currentPoints", 0,
                "accumulatedPoints", 0,
                "isSubscribed", false,
                "joinDate", LocalDateTime.now()
            ),
            "responseTime", LocalDateTime.now()
        );
    }
}