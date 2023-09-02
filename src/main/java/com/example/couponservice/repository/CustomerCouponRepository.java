package com.example.couponservice.repository;

import com.example.couponservice.domain.CustomerCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CustomerCouponRepository extends JpaRepository<CustomerCoupon, UUID> {
}
