package com.example.couponservice.service;

import com.example.couponservice.domain.Coupon;
import com.example.couponservice.repository.CouponRepository;
import com.example.couponservice.service.dto.CreateCouponIn;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class CouponServiceImpl implements CouponService {
    private final CouponRepository couponRepository;
    private final StringRedisTemplate redisTemplate;

    public CouponServiceImpl(CouponRepository couponRepository, StringRedisTemplate redisTemplate) {
        this.couponRepository = couponRepository;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Long createCoupon(CreateCouponIn createCouponIn) {
        Coupon newCoupon = couponRepository.save(createCouponIn.toEntity());
        String maxIssueKey = "coupon:" + newCoupon.getId() + ":maxIssuanceCount";
        setMaxIssuanceCountToRedis(newCoupon, maxIssueKey);
        return newCoupon.getId();
    }

    private void setMaxIssuanceCountToRedis(Coupon newCoupon, String maxIssueKey) {
        redisTemplate.opsForValue().set(maxIssueKey, String.valueOf(newCoupon.getMaxIssuanceCount()));
    }
}
