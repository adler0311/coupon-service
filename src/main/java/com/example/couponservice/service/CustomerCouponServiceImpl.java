package com.example.couponservice.service;

import com.example.couponservice.domain.Coupon;
import com.example.couponservice.domain.CustomerCoupon;
import com.example.couponservice.repository.CouponRepository;
import com.example.couponservice.repository.CustomerCouponRepository;
import com.example.couponservice.service.dto.CustomerCouponOut;
import com.example.couponservice.service.dto.IssueCustomerCouponIn;
import com.example.couponservice.service.dto.UseCustomerCouponIn;
import com.example.couponservice.service.exception.CouponAlreadyIssued;
import com.example.couponservice.service.exception.CouponOutOfStock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CustomerCouponServiceImpl implements CustomerCouponService {
    private final CouponRepository couponRepository;
    private final CustomerCouponRepository customerCouponRepository;

    private final RedisTemplate<String, String> redisTemplate;

    public CustomerCouponServiceImpl(CouponRepository couponRepository, CustomerCouponRepository customerCouponRepository, RedisTemplate<String, String> redisTemplate) {
        this.couponRepository = couponRepository;
        this.customerCouponRepository = customerCouponRepository;
        this.redisTemplate = redisTemplate;
    }

    @Override
    @Transactional
    public UUID issueCustomerCoupon(IssueCustomerCouponIn issueCustomerCouponIn) throws CouponOutOfStock, CouponAlreadyIssued {
        try {

            Optional<Coupon> couponOptional = couponRepository.findById(issueCustomerCouponIn.getCouponId());
            if (couponOptional.isEmpty()) {
                throw new IllegalArgumentException("쿠폰이 존재하지 않습니다: %s".formatted(issueCustomerCouponIn.getCouponId()));
            }

            Coupon coupon = couponOptional.get();
            issueCouponToUser(issueCustomerCouponIn.getUserId(), issueCustomerCouponIn.getCouponId());
            CustomerCoupon newCustomerCoupon = customerCouponRepository.save(issueCustomerCouponIn.toEntity(coupon));
            return newCustomerCoupon.getId();
        } catch (DataIntegrityViolationException e) {
            log.warn(e.getMessage());
            throw new IllegalArgumentException("해당 고객에게 이미 발급되었습니다 1");
        }
    }

    private void issueCouponToUser(Long userId, Long couponId) throws CouponOutOfStock, CouponAlreadyIssued {
        String maxIssueKey = "coupon:" + couponId + ":maxIssuanceCount";
        String issuedUsersKey = "coupon:" + couponId + ":issuedUsers";

        SessionCallback<IssueCouponResult> sessionCallback = new SessionCallback<>() {
            @Override
            public IssueCouponResult execute(RedisOperations operations) throws DataAccessException {
                // 트랜잭션 시작
                operations.multi();

                // 1. 최대 발급 수량 확인
                operations.opsForValue().get(maxIssueKey);
                operations.opsForSet().add(issuedUsersKey, userId.toString());
                operations.opsForSet().size(issuedUsersKey);


                // 트랜잭션 커밋
                List<Object> results = operations.exec();
                String maxIssuanceCount = (String) results.get(0);
                Long notIssued = (Long) results.get(1);
                Long issuedCount = (Long) results.get(2);

                if (notIssued == 0) {
                    return IssueCouponResult.ISSUED_BEFORE;
                }

                if (Long.parseLong(maxIssuanceCount) != -1 && Long.parseLong(maxIssuanceCount) < issuedCount) {
                    operations.opsForSet().remove(issuedUsersKey, userId.toString());
                    return IssueCouponResult.OUT_OF_STOCK;
                }

                return IssueCouponResult.SUCCESS;
            }
        };
        IssueCouponResult result = redisTemplate.execute(sessionCallback);
        if (result == IssueCouponResult.ISSUED_BEFORE) {
            throw new CouponAlreadyIssued("해당 고객에게 이미 발급되었습니다: %s".formatted(userId));
        } else if (result == IssueCouponResult.OUT_OF_STOCK) {
            throw new CouponOutOfStock();
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
