package com.example.couponservice.service;


import com.example.couponservice.domain.Coupon;
import com.example.couponservice.domain.DiscountType;
import com.example.couponservice.repository.CouponRepository;
import com.example.couponservice.service.dto.CreateCouponIn;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.*;


public class CouponServiceImplTest {
    private CouponServiceImpl couponService;

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String > valueOperations;


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        this.couponService = new CouponServiceImpl(couponRepository, redisTemplate);
    }

    @Test
    public void createCouponTest() {
        // given
        CreateCouponIn createCouponIn = CreateCouponIn
                .builder()
                .name("coupon 1")
                .discountAmount(100)
                .discountType(DiscountType.RATE)
                .maxIssuanceCount(-1L)
                .usageExpDt(LocalDateTime.MAX)
                .usageStartDt(LocalDateTime.now())
                .build();
        Coupon newCoupon = Coupon.builder().id(1L).name("coupon 1").build();
        given(couponRepository.save(any(Coupon.class))).willReturn(newCoupon);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doNothing().when(valueOperations).set(anyString(), anyString());


        // when
        Long couponId = couponService.createCoupon(createCouponIn);

        // then
        assertThat(couponId).isEqualTo(1L);
    }
}
