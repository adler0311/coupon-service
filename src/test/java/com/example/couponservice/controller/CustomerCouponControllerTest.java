package com.example.couponservice.controller;

import com.example.couponservice.service.CustomerCouponService;
import com.example.couponservice.service.dto.IssueCustomerCouponIn;
import com.example.couponservice.service.dto.IssueCustomerCouponOut;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@WebMvcTest(CustomerCouponController.class)
public class CustomerCouponControllerTest {
    @MockBean
    private CustomerCouponService customerCouponService;

    @Autowired
    private MockMvc mvc;

    private JacksonTester<IssueCustomerCouponIn> jsonRequest;
    private JacksonTester<IssueCustomerCouponOut> jsonResponse;

    @BeforeEach
    public void setUp() {
        JacksonTester.initFields(this, new ObjectMapper());
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

}
