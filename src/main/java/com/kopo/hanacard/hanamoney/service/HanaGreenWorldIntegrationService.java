package com.kopo.hanacard.hanamoney.service;

import com.kopo.hanacard.common.exception.BusinessException;
import com.kopo.hanacard.common.exception.ErrorCode;
import com.kopo.hanacard.hanamoney.domain.HanamoneyMembership;
import com.kopo.hanacard.hanamoney.domain.HanamoneyTransaction;
import com.kopo.hanacard.hanamoney.repository.HanamoneyMembershipRepository;
import com.kopo.hanacard.hanamoney.repository.HanamoneyTransactionRepository;
import com.kopo.hanacard.user.domain.User;
import com.kopo.hanacard.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HanaGreenWorldIntegrationService {

    private final HanamoneyMembershipRepository hanamoneyMembershipRepository;
    private final HanamoneyTransactionRepository hanamoneyTransactionRepository;
    private final UserService userService;
    private final RestTemplate restTemplate;

    @Value("${hana-green-world.api.base-url:http://localhost:8080}")
    private String hanaGreenWorldBaseUrl;

    /**
     * 하나그린세상에서 고객의 하나머니 정보를 조회하여 하나카드의 하나머니 멤버십을 생성/업데이트
     */
    @Transactional
    public HanamoneyMembership syncHanaMoneyFromGreenWorld(Long userId) {
        User user = userService.getUserById(userId);
        
        try {
            // 하나그린세상에서 고객 정보 조회
            Map<String, Object> greenWorldCustomer = findGreenWorldCustomer(user);
            
            if (greenWorldCustomer == null) {
                log.warn("하나그린세상에서 고객을 찾을 수 없습니다. userId: {}", userId);
                // 기본 멤버십 생성
                return createDefaultHanamoneyMembership(user);
            }
            
            Long hanaMoney = extractHanaMoneyFromCustomer(greenWorldCustomer);
            
            // 하나카드에서 기존 멤버십 조회 또는 생성
            HanamoneyMembership membership = hanamoneyMembershipRepository.findByUser_Id(userId)
                    .orElseGet(() -> createDefaultHanamoneyMembership(user));
            
            // 하나그린세상의 하나머니 잔액으로 동기화
            syncBalance(membership, hanaMoney);
            
            return membership;
            
        } catch (Exception e) {
            log.error("하나그린세상과의 동기화 중 오류 발생. userId: {}", userId, e);
            // 오류 발생 시 기본 멤버십 반환
            return hanamoneyMembershipRepository.findByUser_Id(userId)
                    .orElseGet(() -> createDefaultHanamoneyMembership(user));
        }
    }

    /**
     * 하나그린세상에서 고객 정보 조회
     */
    private Map<String, Object> findGreenWorldCustomer(User user) {
        try {
            String url = hanaGreenWorldBaseUrl + "/api/members/find-by-phone";
            
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("phoneNumber", user.getPhoneNumber());
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            
            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, 
                    HttpMethod.POST, 
                    request, 
                    Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            
        } catch (Exception e) {
            log.error("하나그린세상 고객 조회 실패. phoneNumber: {}", user.getPhoneNumber(), e);
        }
        
        return null;
    }

    /**
     * 고객 정보에서 하나머니 잔액 추출
     */
    private Long extractHanaMoneyFromCustomer(Map<String, Object> customer) {
        try {
            // member_profiles에서 hana_money 필드 추출
            Object hanaMoneyObj = customer.get("hanaMoney");
            if (hanaMoneyObj instanceof Number) {
                return ((Number) hanaMoneyObj).longValue();
            } else if (hanaMoneyObj instanceof String) {
                return Long.parseLong((String) hanaMoneyObj);
            }
        } catch (Exception e) {
            log.error("하나머니 잔액 추출 실패", e);
        }
        
        return 0L; // 기본값
    }

    /**
     * 기본 하나머니 멤버십 생성
     */
    private HanamoneyMembership createDefaultHanamoneyMembership(User user) {
        HanamoneyMembership membership = HanamoneyMembership.builder()
                .user(user)
                .membershipId(UUID.randomUUID().toString())
                .build();
        
        return hanamoneyMembershipRepository.save(membership);
    }

    /**
     * 하나그린세상의 잔액으로 동기화
     */
    private void syncBalance(HanamoneyMembership membership, Long greenWorldBalance) {
        Long currentBalance = membership.getBalance();
        Long difference = greenWorldBalance - currentBalance;
        
        if (difference > 0) {
            // 하나그린세상에서 더 많은 잔액이 있는 경우 (적립)
            membership.earn(difference);
            createTransaction(membership, difference, HanamoneyTransaction.TransactionType.EARN, 
                    "하나그린세상 동기화 - 적립");
        } else if (difference < 0) {
            // 하나카드에서 더 많은 잔액이 있는 경우 (차감)
            Long absDifference = Math.abs(difference);
            if (membership.getBalance() >= absDifference) {
                membership.spend(absDifference);
                createTransaction(membership, absDifference, HanamoneyTransaction.TransactionType.SPEND, 
                        "하나그린세상 동기화 - 차감");
            }
        }
        
        hanamoneyMembershipRepository.save(membership);
    }

    /**
     * 거래 내역 생성
     */
    private void createTransaction(HanamoneyMembership membership, Long amount, 
                                 HanamoneyTransaction.TransactionType transactionType, String description) {
        HanamoneyTransaction transaction = HanamoneyTransaction.builder()
                .membership(membership)
                .amount(amount)
                .balanceAfter(membership.getBalance())
                .transactionType(transactionType)
                .description(description)
                .build();
        
        hanamoneyTransactionRepository.save(transaction);
    }

    /**
     * 하나그린세상에 하나머니 변경사항 전송
     */
    @Transactional
    public void syncToGreenWorld(Long userId, Long amount, String transactionType, String description) {
        try {
            User user = userService.getUserById(userId);
            String url = hanaGreenWorldBaseUrl + "/api/members/update-hana-money";
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("phoneNumber", user.getPhoneNumber());
            requestBody.put("amount", amount);
            requestBody.put("transactionType", transactionType);
            requestBody.put("description", description);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            restTemplate.exchange(url, HttpMethod.POST, request, Map.class);
            
            log.info("하나그린세상 동기화 완료. userId: {}, amount: {}, type: {}", userId, amount, transactionType);
            
        } catch (Exception e) {
            log.error("하나그린세상 동기화 실패. userId: {}, amount: {}, type: {}", userId, amount, transactionType, e);
            // 동기화 실패해도 하나카드 내부 처리는 계속 진행
        }
    }
}
