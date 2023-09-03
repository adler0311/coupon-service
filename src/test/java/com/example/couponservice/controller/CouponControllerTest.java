package com.example.couponservice.controller;

import com.example.couponservice.domain.DiscountType;
import com.example.couponservice.service.CouponService;
import com.example.couponservice.service.dto.CreateCouponIn;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@WebMvcTest(CouponController.class)
public class CouponControllerTest {
    @MockBean
    private CouponService couponService;

    @Autowired
    private MockMvc mvc;

    private JacksonTester<CreateCouponIn> jsonResult;

    @BeforeEach
    public void setUp() {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        JacksonTester.initFields(this, objectMapper);
    }

    @Test
    public void createCouponTest() throws Exception {
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
        given(couponService.createCoupon(any(CreateCouponIn.class))).willReturn(1L);


        // when
        MockHttpServletResponse response = mvc.perform(
                post("/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonResult.write(createCouponIn).getJson())
                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString())
                .isEqualTo("1");
    }
}
