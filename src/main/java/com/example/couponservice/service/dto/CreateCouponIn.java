package com.example.couponservice.service.dto;

import com.example.couponservice.domain.Coupon;
import com.example.couponservice.domain.DiscountType;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;
import java.time.LocalDateTime;

@Builder
@Getter
@Setter
public class CreateCouponIn {
    @NonNull
    private String name;
    @NonNull
    private Long maxIssuanceCount;
    @NonNull
    private LocalDateTime usageStartDt;
    @NonNull
    private LocalDateTime usageExpDt;
    @NonNull
    private Integer discountAmount;
    @NonNull
    private DiscountType discountType;


    public Coupon toEntity() {
        return Coupon.builder()
                .name(name)
                .maxIssuanceCount(maxIssuanceCount)
                .usageExpAt(usageExpDt)
                .usageStartAt(usageStartDt)
                .discountAmount(discountAmount)
                .discountType(discountType)
                .issuedCount(0L)
                .build();
    }
}
