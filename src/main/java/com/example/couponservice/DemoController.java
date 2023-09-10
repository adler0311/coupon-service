package com.example.couponservice;

import com.example.couponservice.service.exception.CouponOutOfStock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@ControllerAdvice
public class DemoController {
    @GetMapping("/")
    public String home() {
        return "hello world";
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public void handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.error(e.getMessage());
    }

    @ExceptionHandler(CouponOutOfStock.class)
    public ResponseEntity<String> handleBadRequest(CouponOutOfStock ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
