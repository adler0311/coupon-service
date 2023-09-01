package com.example.couponservice.service;

import com.example.couponservice.domain.Coupon;
import com.example.couponservice.repository.CouponRepository;
import com.example.couponservice.service.dto.CreateCouponIn;
import org.springframework.stereotype.Service;

@Service
public class CouponServiceImpl implements CouponService {
    private final CouponRepository couponRepository;

    public CouponServiceImpl(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    @Override
    public Long createCoupon(CreateCouponIn createCouponIn) {
        Coupon newCoupon = couponRepository.save(createCouponIn.toEntity());
        return newCoupon.getId();
    }
}
