package com.example.couponservice.service;

import com.example.couponservice.domain.Coupon;
import com.example.couponservice.domain.CustomerCoupon;
import com.example.couponservice.repository.CouponRepository;
import com.example.couponservice.repository.CustomerCouponRepository;
import com.example.couponservice.service.dto.CustomerCouponOut;
import com.example.couponservice.service.dto.IssueCustomerCouponIn;
import com.example.couponservice.service.dto.UseCustomerCouponIn;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CustomerCouponServiceImpl implements CustomerCouponService {
    private final CouponRepository couponRepository;
    private final CustomerCouponRepository customerCouponRepository;

    public CustomerCouponServiceImpl(CouponRepository couponRepository, CustomerCouponRepository customerCouponRepository) {
        this.couponRepository = couponRepository;
        this.customerCouponRepository = customerCouponRepository;
    }

    @Override
    @Transactional
    public UUID issueCustomerCoupon(IssueCustomerCouponIn issueCustomerCouponIn) {
        try {
            Optional<Coupon> coupon = couponRepository.findById(issueCustomerCouponIn.getCouponId());
            if (coupon.isEmpty()) {
                throw new IllegalArgumentException("쿠폰이 존재하지 않습니다: %s".formatted(issueCustomerCouponIn.getCouponId()));
            }

            boolean customerCouponExists = customerCouponRepository.existsCustomerCouponByCouponAndCustomerId(coupon.get(), issueCustomerCouponIn.getUserId());
            if (customerCouponExists) {
                throw new IllegalArgumentException("해당 고객에게 이미 발급되었습니다");
            }

            CustomerCoupon newCustomerCoupon = customerCouponRepository.save(issueCustomerCouponIn.toEntity(coupon.get()));
            return newCustomerCoupon.getId();
        } catch (DataIntegrityViolationException e) {
            log.warn("aa");
            throw new IllegalArgumentException("해당 고객에게 이미 발급되었습니다 1");
        }
    }

    @Override
    public void useCustomerCoupon(UUID customerCouponId, UseCustomerCouponIn useCustomerCouponIn) {
        Optional<CustomerCoupon> optionalCustomerCoupon = customerCouponRepository.findById(customerCouponId);
        if (optionalCustomerCoupon.isEmpty()) {
            throw new IllegalArgumentException("사용자 쿠폰이 존재하지 않습니다: %s".formatted(customerCouponId));
        }

        CustomerCoupon customerCoupon = optionalCustomerCoupon.get();

        if (!customerCoupon.getCustomerId().equals(useCustomerCouponIn.getCustomerId())) {
            throw new IllegalArgumentException("사용 권한이 없습니다");
        }

        customerCoupon.use();
        customerCouponRepository.save(customerCoupon);
    }

    @Override
    public List<CustomerCouponOut> getCustomerCoupons(Long customerId, Pageable pageable) {
        List<CustomerCoupon> customerCoupons = customerCouponRepository.findAllWithCouponByCustomerId(customerId, pageable);
        return customerCoupons.stream().map(CustomerCouponOut::fromEntity).collect(Collectors.toList());
    }
}
