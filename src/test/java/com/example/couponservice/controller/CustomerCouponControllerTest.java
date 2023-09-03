package com.example.couponservice.controller;

import com.example.couponservice.domain.Coupon;
import com.example.couponservice.domain.CustomerCoupon;
import com.example.couponservice.domain.DiscountType;
import com.example.couponservice.service.CustomerCouponService;
import com.example.couponservice.service.dto.CustomerCouponOut;
import com.example.couponservice.service.dto.IssueCustomerCouponIn;
import com.example.couponservice.service.dto.IssueCustomerCouponOut;
import com.example.couponservice.service.dto.UseCustomerCouponIn;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@RunWith(SpringRunner.class)
@WebMvcTest(CustomerCouponController.class)
public class CustomerCouponControllerTest {
    @MockBean
    private CustomerCouponService customerCouponService;

    @Autowired
    private MockMvc mvc;

    private JacksonTester<IssueCustomerCouponIn> jsonRequest;
    private JacksonTester<UseCustomerCouponIn> useCustomerCouponInJacksonTester;
    private JacksonTester<IssueCustomerCouponOut> jsonResponse;

    private JacksonTester<List<CustomerCouponOut>> customerCouponOutsJacksonTester;

    @BeforeEach
    public void setUp() {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        JacksonTester.initFields(this, objectMapper);

    }

    @Test
    public void issueCustomerCouponTest() throws Exception {
        // given

        IssueCustomerCouponIn issueCustomerCouponIn = IssueCustomerCouponIn.builder().userId(1L).couponId(1L).build();
        UUID customerCouponId = UUID.randomUUID();
        given(customerCouponService.issueCustomerCoupon(any(IssueCustomerCouponIn.class))).willReturn(customerCouponId);

        // when
        MockHttpServletResponse response = mvc.perform(
                post("/customer-coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest.write(issueCustomerCouponIn).getJson())
                ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString())
                .isEqualTo(jsonResponse.write(new IssueCustomerCouponOut(customerCouponId)).getJson());

    }


    @Test
    public void useCustomerCouponTest() throws Exception {
        // given
        UseCustomerCouponIn useCustomerCouponIn = UseCustomerCouponIn.builder().customerId(1L).build();
        UUID customerCouponId = UUID.randomUUID();

        // when
        MockHttpServletResponse response = mvc.perform(
                patch("/customer-coupons/{customerCouponId}", customerCouponId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(useCustomerCouponInJacksonTester.write(useCustomerCouponIn).getJson())
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    public void getCustomerCouponsTest() throws Exception {
        // given
        long customerId = 1L;
        Coupon coupon = Coupon.builder().name("1").discountAmount(30).discountType(DiscountType.RATE).maxIssuanceCount(2L).usageExpAt(LocalDateTime.now().plus(1, ChronoUnit.DAYS)).usageStartAt(LocalDateTime.now()).build();
        List<CustomerCoupon> customerCoupons = IntStream.range(0, 7)
                .mapToObj(i -> {
                    CustomerCoupon customerCoupon = CustomerCoupon.builder().build();
                    customerCoupon.setCoupon(coupon);
                    return customerCoupon;
                })
                .toList();

        List<CustomerCouponOut> customerCouponOuts = customerCoupons.stream()
                .map(CustomerCouponOut::fromEntity)
                .collect(Collectors.toList());
        given(customerCouponService.getCustomerCoupons(any(Long.class), any(Pageable.class))).willReturn(customerCouponOuts);

        // when
        MockHttpServletResponse response = mvc.perform(
                get("/customer-coupons")
                        .param("customerId", Long.toString(customerId))
                        .param("page", "1")
                        .param("size", "5")
                        .contentType(MediaType.APPLICATION_JSON)).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(customerCouponOutsJacksonTester.write(customerCouponOuts).getJson());
    }
}
