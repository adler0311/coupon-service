package com.example.couponservice.service;

import com.example.couponservice.domain.Coupon;
import com.example.couponservice.domain.CustomerCoupon;
import com.example.couponservice.domain.DiscountType;
import com.example.couponservice.repository.CouponRepository;
import com.example.couponservice.repository.CustomerCouponRepository;
import com.example.couponservice.service.dto.IssueCustomerCouponIn;
import com.example.couponservice.service.dto.UseCustomerCouponIn;
import com.example.couponservice.service.exception.CouponAlreadyIssued;
import com.example.couponservice.service.exception.CouponOutOfStock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;


public class CustomerCouponServiceImplTest {
    private CustomerCouponServiceImpl customerCouponService;
    @Mock
    CouponRepository couponRepository;

    @Mock
    private CustomerCouponRepository customerCouponRepository;

    @Mock
    private StringRedisTemplate redisTemplate;


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        this.customerCouponService = new CustomerCouponServiceImpl(couponRepository, customerCouponRepository, redisTemplate);
    }

    @Test
    public void issueCustomerCoupon() throws CouponOutOfStock, CouponAlreadyIssued {
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

    @Test
    public void issueCustomerCoupon__duplicateRequest() throws CouponOutOfStock, CouponAlreadyIssued {
        // given
        UUID uuid = UUID.randomUUID();
        Coupon coupon = Coupon.builder().name("1").discountAmount(30).discountType(DiscountType.RATE).maxIssuanceCount(2L).usageExpAt(LocalDateTime.MAX).usageStartAt(LocalDateTime.now()).build();
        IssueCustomerCouponIn issueCustomerCouponIn = IssueCustomerCouponIn.builder().couponId(1L).userId(1L).build();
        CustomerCoupon newCustomerCoupon = CustomerCoupon.builder().id(uuid).coupon(coupon).customerId(issueCustomerCouponIn.getUserId()).build();

        given(couponRepository.findById(any(Long.class))).willReturn(Optional.of(coupon));
        given(customerCouponRepository.save(any())).willReturn(newCustomerCoupon);
        AtomicInteger callCount = new AtomicInteger(0);
        when(redisTemplate.execute(any(SessionCallback.class))).thenAnswer(invocation -> {
            if (callCount.getAndIncrement() == 1) {
                return IssueCouponResult.ISSUED_BEFORE;
            }
            return IssueCouponResult.SUCCESS;
        });

        customerCouponService.issueCustomerCoupon(issueCustomerCouponIn);

        // when, then
        assertThrows(CouponAlreadyIssued.class, () -> customerCouponService.issueCustomerCoupon(issueCustomerCouponIn));
    }

    @Test
    public void issueCustomerCoupon__outOfStock() {
        // given
        IssueCustomerCouponIn issueCustomerCouponIn = IssueCustomerCouponIn.builder().couponId(1L).userId(1L).build();
        UUID uuid = UUID.randomUUID();
        Coupon coupon = Coupon.builder().name("1").issuedCount(2L).discountAmount(30).discountType(DiscountType.RATE).maxIssuanceCount(2L).usageExpAt(LocalDateTime.MAX).usageStartAt(LocalDateTime.now()).build();
        given(couponRepository.findById(any(Long.class))).willReturn(Optional.of(coupon));
        CustomerCoupon newCustomerCoupon = CustomerCoupon.builder().id(uuid).coupon(coupon).customerId(issueCustomerCouponIn.getUserId()).build();
        given(customerCouponRepository.save(any(CustomerCoupon.class))).willReturn(newCustomerCoupon);
        given(redisTemplate.execute(any(SessionCallback.class))).willReturn(IssueCouponResult.OUT_OF_STOCK);

        // when, then
        CouponOutOfStock thrownException = assertThrows(CouponOutOfStock.class, () -> customerCouponService.issueCustomerCoupon(issueCustomerCouponIn));
        assertEquals("재고가 부족합니다", thrownException.getMessage());
    }


    @Test
    public void useCustomerCouponTest() {
        // given
        Long customerId = 1L;
        Coupon coupon = Coupon.builder().name("1").discountAmount(30).discountType(DiscountType.RATE).maxIssuanceCount(2L).usageExpAt(LocalDateTime.MAX).usageStartAt(LocalDateTime.now()).build();
        CustomerCoupon customerCoupon = CustomerCoupon.builder().id(UUID.randomUUID()).coupon(coupon).customerId(customerId).build();
        UseCustomerCouponIn useCustomerCouponIn = UseCustomerCouponIn.builder().customerId(customerId).build();
        given(couponRepository.findById(any(Long.class))).willReturn(Optional.of(coupon));
        given(customerCouponRepository.findByIdForUpdate(any(UUID.class))).willReturn(Optional.of(customerCoupon));
        given(redisTemplate.execute(any(SessionCallback.class))).willReturn(IssueCouponResult.SUCCESS);

        // when
        customerCouponService.useCustomerCoupon(customerCoupon.getId(), useCustomerCouponIn);

        // then
        assertNotNull(customerCoupon.getUsedAt());
    }
    @Test
    public void useCustomerCouponTest__failedAtExpiredCoupon() {
        // given
        Long customerId = 1L;
        Coupon coupon = Coupon.builder().name("1").issuedCount(2L).discountAmount(30).discountType(DiscountType.RATE).maxIssuanceCount(2L).usageExpAt(LocalDateTime.now().minusDays(1)).usageStartAt(LocalDateTime.now()).build();
        CustomerCoupon customerCoupon = CustomerCoupon.builder().id(UUID.randomUUID()).customerId(customerId).build();
        customerCoupon.setCoupon(coupon);
        UseCustomerCouponIn useCustomerCouponIn = UseCustomerCouponIn.builder().customerId(customerId).build();
        given(customerCouponRepository.findByIdForUpdate(any(UUID.class))).willReturn(Optional.of(customerCoupon));

        // when, then
        IllegalArgumentException thrownException = assertThrows(IllegalArgumentException.class, () -> customerCouponService.useCustomerCoupon(customerCoupon.getId(), useCustomerCouponIn));
        assertEquals("기간이 만료된 쿠폰입니다", thrownException.getMessage());
    }
}
