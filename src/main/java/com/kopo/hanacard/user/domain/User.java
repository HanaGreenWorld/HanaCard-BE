package com.kopo.hanacard.user.domain;

import com.kopo.hanacard.common.domain.DateTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends DateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "phone_number", nullable = false, unique = true)
    private String phoneNumber;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "birth_date", nullable = false)
    private String birthDate;

    @Column(name = "address")
    private String address;

    @Column(name = "customer_grade", length = 20)
    private String customerGrade;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "group_customer_token", unique = true)
    private String groupCustomerToken;

    @Builder
    public User(String username, String email, String phoneNumber, String name, 
                String birthDate, String address, String customerGrade, Boolean isActive, String groupCustomerToken) {
        this.username = username;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.name = name;
        this.birthDate = birthDate;
        this.address = address;
        this.customerGrade = customerGrade;
        this.isActive = isActive != null ? isActive : true;
        this.groupCustomerToken = groupCustomerToken;
    }

    public void updateUserInfo(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public String getGroupCustomerToken() {
        return this.groupCustomerToken;
    }

    /**
     * 그룹 고객 토큰 업데이트
     */
    public void setGroupCustomerToken(String groupCustomerToken) {
        this.groupCustomerToken = groupCustomerToken;
    }
}




