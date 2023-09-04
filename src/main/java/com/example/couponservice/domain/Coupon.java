package com.example.couponservice.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Long maxIssuanceCount;

    @Column(nullable = false)
    private LocalDateTime usageStartAt;

    @Column(nullable = false)
    private LocalDateTime usageExpAt;

    @Column(nullable = false)
    private Integer discountAmount;

    @Column(nullable = false)
    private DiscountType discountType;

    @Column(nullable = false)
    @ColumnDefault("0")
    private Long issuedCount;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public boolean isOutOfStock() {
        if (maxIssuanceCount == -1L) {
            return false;
        }
        
        return Objects.equals(issuedCount, maxIssuanceCount);
    }
}
