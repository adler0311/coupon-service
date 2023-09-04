package com.example.couponservice.integration;

import com.example.couponservice.domain.Coupon;
import com.example.couponservice.domain.CustomerCoupon;
import com.example.couponservice.domain.DiscountType;
import com.example.couponservice.service.CouponService;
import com.example.couponservice.service.CustomerCouponService;
import com.example.couponservice.service.dto.CreateCouponIn;
import com.example.couponservice.service.dto.CustomerCouponOut;
import com.example.couponservice.service.dto.IssueCustomerCouponIn;
import com.example.couponservice.service.dto.UseCustomerCouponIn;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@Testcontainers
public class CustomerCouponTest {
    @Container
    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:14")
            .withDatabaseName("demo")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    public static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @Autowired
    private CustomerCouponService customerCouponService;
    @Autowired
    private CouponService couponService;

    @Test
    public void issueCustomerCoupon__sameConcurrentRequests()   {
        // given
        CreateCouponIn createCouponIn = CreateCouponIn
                .builder()
                .name("coupon 1")
                .discountAmount(100)
                .discountType(DiscountType.RATE)
                .maxIssuanceCount(-1L)
                .usageExpDt(LocalDateTime.now().plus(1, ChronoUnit.DAYS))
                .usageStartDt(LocalDateTime.now())
                .build();
        Long createdCouponId = couponService.createCoupon(createCouponIn);

        IssueCustomerCouponIn issueCustomerCouponIn = IssueCustomerCouponIn
                .builder()
                .couponId(createdCouponId)
                .userId(1L)
                .build();

        Page<CustomerCouponOut> initialResult = customerCouponService.getCustomerCoupons(issueCustomerCouponIn.getUserId(), Pageable.ofSize(5));
        assertEquals(initialResult.getSize(), 0);

        int numConcurrentRequests = 10;
        CountDownLatch latch = new CountDownLatch(numConcurrentRequests);
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        List<Boolean> results = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger successCount = new AtomicInteger(0);


        for (int i = 0; i < numConcurrentRequests; i++) {
            executorService.submit(() -> {
                try {
                    customerCouponService.issueCustomerCoupon(issueCustomerCouponIn);
                    successCount.incrementAndGet();
                } finally {
                    latch.countDown();

                }
            });
        }

        try {
            // 모든 작업이 완료될 때까지 대기
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        executorService.shutdown();
        assertEquals(1, successCount.get());
        Page<CustomerCouponOut> customerCouponOuts = customerCouponService.getCustomerCoupons(issueCustomerCouponIn.getUserId(), Pageable.ofSize(5));
        assertEquals(1, customerCouponOuts.getSize());
    }

    @Test
    public void getCustomerCouponsTest() {
        long customerId = 1L;
        IntStream.range(1, 8).forEach(i -> {
            CreateCouponIn createCouponIn = CreateCouponIn.builder()
                    .name("coupon " + i)
                    .discountAmount(100)
                    .discountType(DiscountType.RATE)
                    .maxIssuanceCount(-1L)
                    .usageExpDt(LocalDateTime.now().plus(1, ChronoUnit.DAYS))
                    .usageStartDt(LocalDateTime.now())
                    .build();
            Long createdCouponId = couponService.createCoupon(createCouponIn);
            IssueCustomerCouponIn issueCustomerCouponIn = IssueCustomerCouponIn.builder()
                    .couponId(createdCouponId)
                    .userId(customerId)
                    .build();
            customerCouponService.issueCustomerCoupon(issueCustomerCouponIn);
        });

        // when
        Page<CustomerCouponOut> customerCouponOutPage = customerCouponService.getCustomerCoupons(customerId, Pageable.ofSize(3));

        // then
        // assert query result size
        assertEquals(3, customerCouponOutPage.getNumberOfElements());

        List<CustomerCouponOut> customerCouponOuts = customerCouponOutPage.stream().toList();
        // assert fetch coupon info
        CustomerCouponOut firstCustomerCouponOut = customerCouponOuts.get(0);
        assertEquals("coupon 7", firstCustomerCouponOut.getCouponName());
        assertEquals(100, firstCustomerCouponOut.getDiscountAmount());

        // assert order by issuedAt
        for (int i = 0; i < customerCouponOutPage.getSize() -1; i++) {
            assertTrue(customerCouponOuts.get(i).getIssuedAt().isAfter(customerCouponOuts.get(i+1).getIssuedAt()));
        }
    }

    @Test
    public void getCustomerCouponsTest__onlyNotExpired() {
        long customerId = 1L;
        IntStream.range(1, 3).forEach(i -> {
            CreateCouponIn createCouponIn = CreateCouponIn.builder()
                    .name("coupon " + i)
                    .discountAmount(100)
                    .discountType(DiscountType.RATE)
                    .maxIssuanceCount(-1L)
                    .usageExpDt(LocalDateTime.now().minusDays(i - 1).plusHours(1))
                    .usageStartDt(LocalDateTime.now())
                    .build();

            Long createdCouponId = couponService.createCoupon(createCouponIn);
            IssueCustomerCouponIn issueCustomerCouponIn = IssueCustomerCouponIn.builder()
                    .couponId(createdCouponId)
                    .userId(customerId)
                    .build();
            customerCouponService.issueCustomerCoupon(issueCustomerCouponIn);
        });

        // when
        Page<CustomerCouponOut> customerCouponOutPage = customerCouponService.getCustomerCoupons(customerId, Pageable.ofSize(3));

        // then: query only not expired
        assertEquals(1, customerCouponOutPage.getNumberOfElements());
        List<CustomerCouponOut> customerCouponOuts = customerCouponOutPage.stream().toList();
        for (int i = 0; i < customerCouponOutPage.getNumberOfElements() -1; i++) {
            assertTrue(customerCouponOuts.get(i).getUsageExpAt().isAfter(LocalDateTime.now()));
        }
    }

    @Test
    public void useCustomerCoupon__sameConcurrentRequests()   {
        // given
        CreateCouponIn createCouponIn = CreateCouponIn
                .builder()
                .name("coupon 1")
                .discountAmount(100)
                .discountType(DiscountType.RATE)
                .maxIssuanceCount(-1L)
                .usageExpDt(LocalDateTime.now().plus(1, ChronoUnit.DAYS))
                .usageStartDt(LocalDateTime.now())
                .build();
        Long createdCouponId = couponService.createCoupon(createCouponIn);

        Long customerId = 1L;
        IssueCustomerCouponIn issueCustomerCouponIn = IssueCustomerCouponIn
                .builder()
                .couponId(createdCouponId)
                .userId(customerId)
                .build();

        UUID customerCouponId = customerCouponService.issueCustomerCoupon(issueCustomerCouponIn);

        UseCustomerCouponIn useCustomerCouponIn = UseCustomerCouponIn
                .builder()
                .customerId(customerId)
                .build();

        int numConcurrentRequests = 3;
        CountDownLatch latch = new CountDownLatch(numConcurrentRequests);
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        List<Boolean> results = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < numConcurrentRequests; i++) {
            executorService.submit(() -> {
                try {
                    customerCouponService.useCustomerCoupon(customerCouponId, useCustomerCouponIn);
                    successCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            // 모든 작업이 완료될 때까지 대기
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        executorService.shutdown();
        assertEquals(1, successCount.get());
        Page<CustomerCouponOut>  customerCouponOutPage = customerCouponService.getCustomerCoupons(customerId, Pageable.ofSize(1));
        assertNotNull(customerCouponOutPage.getContent().get(0).getUsedAt());
    }
}
