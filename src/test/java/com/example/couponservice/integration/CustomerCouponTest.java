package com.example.couponservice.integration;

import com.example.couponservice.domain.Coupon;
import com.example.couponservice.domain.DiscountType;
import com.example.couponservice.service.CouponService;
import com.example.couponservice.service.CustomerCouponService;
import com.example.couponservice.service.dto.CreateCouponIn;
import com.example.couponservice.service.dto.CustomerCouponOut;
import com.example.couponservice.service.dto.IssueCustomerCouponIn;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    public void issueCustomerCoupon__same_concurrent_request()   {
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

        List<CustomerCouponOut> aa = customerCouponService.getCustomerCoupons(issueCustomerCouponIn.getUserId(), Pageable.ofSize(5));
        assertEquals(aa.size(), 0);

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
        List<CustomerCouponOut> customerCouponOuts = customerCouponService.getCustomerCoupons(issueCustomerCouponIn.getUserId(), Pageable.ofSize(5));
        assertEquals(1, customerCouponOuts.size());
    }
}
