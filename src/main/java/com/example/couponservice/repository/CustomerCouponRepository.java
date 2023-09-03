package com.example.couponservice.repository;

import com.example.couponservice.domain.CustomerCoupon;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface CustomerCouponRepository extends JpaRepository<CustomerCoupon, UUID> {

    @Query("select cc from CustomerCoupon cc join fetch cc.coupon where cc.customerId = :customerId ")
    List<CustomerCoupon> findAllWithCouponByCustomerId(Long customerId, Pageable pageable);
}
