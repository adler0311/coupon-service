package com.example.couponservice.service;

import com.example.couponservice.domain.Coupon;
import com.example.couponservice.domain.CustomerCoupon;
import com.example.couponservice.domain.DiscountType;
import com.example.couponservice.repository.CouponRepository;
import com.example.couponservice.repository.CustomerCouponRepository;
import com.example.couponservice.service.dto.IssueCustomerCouponIn;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.*;


public class CustomerCouponServiceImplTest {
    private CustomerCouponServiceImpl customerCouponService;
    @Mock
    CouponRepository couponRepository;

    @Mock
    private CustomerCouponRepository customerCouponRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        this.customerCouponService = new CustomerCouponServiceImpl(couponRepository, customerCouponRepository);
    }

    @Test
    public void issueCustomerCoupon() {
        // given
        IssueCustomerCouponIn issueCustomerCouponIn = IssueCustomerCouponIn.builder().couponId(1L).userId(1L).build();
        UUID uuid = UUID.randomUUID();
        Coupon coupon = Coupon.builder().name("1").discountAmount(30).discountType(DiscountType.RATE).maxIssuanceCount(2L).usageExpAt(LocalDateTime.MAX).usageStartAt(LocalDateTime.now()).build();
        given(couponRepository.findById(any(Long.class))).willReturn(Optional.of(coupon));
        CustomerCoupon newCustomerCoupon = CustomerCoupon.builder().id(uuid).coupon(coupon).customerId(issueCustomerCouponIn.getUserId()).build();
        given(customerCouponRepository.save(any(CustomerCoupon.class))).willReturn(newCustomerCoupon);

        // when
        UUID customerCouponId = customerCouponService.issueCustomerCoupon(issueCustomerCouponIn);

        // then
        assertThat(customerCouponId).isEqualTo(uuid);
    }

    @Test
    public void issueCustomerCoupon__couponNotFound() {
        // given
        IssueCustomerCouponIn issueCustomerCouponIn = IssueCustomerCouponIn.builder().couponId(1L).userId(1L).build();
        given(couponRepository.findById(any(Long.class))).willReturn(Optional.empty());

        // when, then
        assertThrows(IllegalArgumentException.class, () -> customerCouponService.issueCustomerCoupon(issueCustomerCouponIn));
    }
}
