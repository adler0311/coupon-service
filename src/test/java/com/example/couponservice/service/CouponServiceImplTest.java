package com.example.couponservice.service;


import com.example.couponservice.domain.Coupon;
import com.example.couponservice.repository.CouponRepository;
import com.example.couponservice.service.dto.CreateCouponIn;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;


public class CouponServiceImplTest {
    private CouponServiceImpl couponService;

    @Mock
    private CouponRepository couponRepository;


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        this.couponService = new CouponServiceImpl(couponRepository);
    }

    @Test
    public void createCouponTest() {
        // given
        CreateCouponIn createCouponIn = CreateCouponIn.builder().build();
        Coupon newCoupon = Coupon.builder().id(1L).name("coupon 1").build();
        given(couponRepository.save(any(Coupon.class))).willReturn(newCoupon);

        // when
        Long couponId = couponService.createCoupon(createCouponIn);

        // then
        assertThat(couponId).isEqualTo(1L);
    }
}
