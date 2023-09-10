package com.example.couponservice.service;

import com.example.couponservice.domain.Coupon;
import com.example.couponservice.domain.CustomerCoupon;
import com.example.couponservice.domain.DiscountType;
import com.example.couponservice.repository.CouponRepository;
import com.example.couponservice.repository.CustomerCouponRepository;
import com.example.couponservice.service.dto.CreateCouponIn;
import com.example.couponservice.service.dto.CustomerCouponOut;
import com.example.couponservice.service.dto.IssueCustomerCouponIn;
import com.example.couponservice.service.dto.UseCustomerCouponIn;
import com.example.couponservice.service.exception.CouponOutOfStock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        this.customerCouponService = new CustomerCouponServiceImpl(couponRepository, customerCouponRepository);
    }

    @Test
    public void issueCustomerCoupon() throws CouponOutOfStock {
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
    public void issueCustomerCoupon__duplicateRequest() throws CouponOutOfStock {
        // given
        UUID uuid = UUID.randomUUID();
        Coupon coupon = Coupon.builder().name("1").discountAmount(30).discountType(DiscountType.RATE).maxIssuanceCount(2L).usageExpAt(LocalDateTime.MAX).usageStartAt(LocalDateTime.now()).build();
        IssueCustomerCouponIn issueCustomerCouponIn = IssueCustomerCouponIn.builder().couponId(1L).userId(1L).build();
        CustomerCoupon newCustomerCoupon = CustomerCoupon.builder().id(uuid).coupon(coupon).customerId(issueCustomerCouponIn.getUserId()).build();
        AtomicInteger callCount = new AtomicInteger(0);
        given(couponRepository.findById(any(Long.class))).willReturn(Optional.of(coupon));
        given(customerCouponRepository.save(any())).willReturn(newCustomerCoupon);
        when(customerCouponRepository.existsCustomerCouponByCouponAndCustomerId(any(), any())).thenAnswer(invocation -> {
            if (callCount.incrementAndGet() == 1) {
                return false;
            }
            return true;
        });

        customerCouponService.issueCustomerCoupon(issueCustomerCouponIn);

        // when, then
        IllegalArgumentException thrownException = assertThrows(IllegalArgumentException.class, () -> customerCouponService.issueCustomerCoupon(issueCustomerCouponIn));
        assertEquals("이미 발급 받은 쿠폰입니다", thrownException.getMessage());
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

        // when, then
        IllegalArgumentException thrownException = assertThrows(IllegalArgumentException.class, () -> customerCouponService.issueCustomerCoupon(issueCustomerCouponIn));
        assertEquals("재고가 부족합니다", thrownException.getMessage());
    }


    @Test
    public void useCustomerCouponTest() {
        // given
        Long customerId = 1L;
        CustomerCoupon customerCoupon = CustomerCoupon.builder().id(UUID.randomUUID()).customerId(customerId).build();
        UseCustomerCouponIn useCustomerCouponIn = UseCustomerCouponIn.builder().customerId(customerId).build();
        given(customerCouponRepository.findById(any(UUID.class))).willReturn(Optional.of(customerCoupon));

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
        given(customerCouponRepository.findById(any(UUID.class))).willReturn(Optional.of(customerCoupon));

        // when, then
        IllegalArgumentException thrownException = assertThrows(IllegalArgumentException.class, () -> customerCouponService.useCustomerCoupon(customerCoupon.getId(), useCustomerCouponIn));
        assertEquals("기간이 만료된 쿠폰입니다", thrownException.getMessage());
    }
}
