package com.example.couponservice.service;

import com.example.couponservice.domain.Coupon;
import com.example.couponservice.domain.CustomerCoupon;
import com.example.couponservice.repository.CouponRepository;
import com.example.couponservice.repository.CustomerCouponRepository;
import com.example.couponservice.service.dto.CustomerCouponOut;
import com.example.couponservice.service.dto.IssueCustomerCouponIn;
import com.example.couponservice.service.dto.UseCustomerCouponIn;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.LockMode;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
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
            Optional<Coupon> couponOptional = couponRepository.findById(issueCustomerCouponIn.getCouponId());
            if (couponOptional.isEmpty()) {
                throw new IllegalArgumentException("쿠폰이 존재하지 않습니다: %s".formatted(issueCustomerCouponIn.getCouponId()));
            }

            Coupon coupon = couponOptional.get();
            if (coupon.isOutOfStock()) {
                throw new IllegalArgumentException("재고가 부족합니다");
            }

            boolean customerCouponExists = customerCouponRepository.existsCustomerCouponByCouponAndCustomerId(coupon, issueCustomerCouponIn.getUserId());
            if (customerCouponExists) {
                throw new IllegalArgumentException("해당 고객에게 이미 발급되었습니다");
            }

            CustomerCoupon newCustomerCoupon = customerCouponRepository.save(issueCustomerCouponIn.toEntity(coupon));
            return newCustomerCoupon.getId();
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("해당 고객에게 이미 발급되었습니다 1");
        }
    }

    @Override
    @Transactional
    public void useCustomerCoupon(UUID customerCouponId, UseCustomerCouponIn useCustomerCouponIn) {
        Optional<CustomerCoupon> optionalCustomerCoupon = customerCouponRepository.findByIdForUpdate(customerCouponId);

        if (optionalCustomerCoupon.isEmpty()) {
            throw new IllegalArgumentException("사용자 쿠폰이 존재하지 않습니다: %s".formatted(customerCouponId));
        }

        CustomerCoupon customerCoupon = optionalCustomerCoupon.get();

        if (!customerCoupon.getCustomerId().equals(useCustomerCouponIn.getCustomerId())) {
            throw new IllegalArgumentException("사용 권한이 없습니다");
        }

        if (customerCoupon.getUsedAt() != null) {
            throw new IllegalArgumentException("이미 사용한 쿠폰입니다");
        }

        customerCoupon.use();
        customerCouponRepository.save(customerCoupon);
    }

    @Override
    public Page<CustomerCouponOut> getCustomerCoupons(Long customerId, Pageable pageable) {
        Page<CustomerCoupon> customerCoupons = customerCouponRepository.findAllWithCouponByCustomerId(customerId, pageable);
        List<CustomerCouponOut> customerCouponOuts = customerCoupons.stream().map(CustomerCouponOut::fromEntity).collect(Collectors.toList());
        return new PageImpl<>(customerCouponOuts, pageable, customerCoupons.getTotalElements());
    }
}
