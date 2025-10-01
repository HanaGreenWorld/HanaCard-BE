package com.kopo.hanacard.benefit.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "card_benefit_categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardBenefitCategory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;
    
    @Column(name = "category_name", nullable = false)
    private String categoryName;
    
    @Column(name = "category_description")
    private String categoryDescription;
    
    @Column(name = "display_order")
    private Integer displayOrder;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "package_code")
    private String packageCode;
    
    @Column(name = "cashback_rate")
    private String cashbackRate;
    
    @Column(name = "category_icon")
    private String categoryIcon;
    
    // Many-to-One relationship with CardBenefitPackage
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "benefit_package_id")
    @JsonIgnore
    private CardBenefitPackage benefitPackage;
    
    // One-to-Many relationship with CardBenefitDetail
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<CardBenefitDetail> details = new ArrayList<>();
}