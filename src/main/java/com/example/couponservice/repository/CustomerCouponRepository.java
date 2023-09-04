package com.example.couponservice.repository;

import com.example.couponservice.domain.Coupon;
import com.example.couponservice.domain.CustomerCoupon;
import jakarta.persistence.LockModeType;
import org.hibernate.LockMode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerCouponRepository extends JpaRepository<CustomerCoupon, UUID> {

    @Query("select cc from CustomerCoupon cc join fetch cc.coupon where cc.customerId = :customerId and cc.coupon.usageExpAt > now() order by cc.issuedAt desc")
    Page<CustomerCoupon> findAllWithCouponByCustomerId(Long customerId, Pageable pageable);

    boolean existsCustomerCouponByCouponAndCustomerId(Coupon coupon, Long customerId);

    @Query(value = "SELECT * FROM customer_coupon WHERE id = :id FOR UPDATE", nativeQuery = true)
    Optional<CustomerCoupon> findByIdForUpdate(UUID id);

}
