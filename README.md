# 기능 명세
- [x] 사용자 쿠폰 리스트는 페이지네이션을 지원한다
- [x] 쿠폰 발급 시 중복 발급인 경우에 발급에 실패한다.
- [x] 쿠폰 수량을 모두 소진한 경우 발급에 실패한다
- [x] 사용자의 쿠폰을 보여줄 때 유효기간이 지난 쿠폰은 보여주지 않는다
- [x] 사용자가 쿠폰을 사용할 때 유효 기간이 지난 경우에는 사용에 실패해야 한다


# 비기능 명세
- [x] 스케일 아웃되어 여러 서버가 배포되어 처리해도 문제가 없어야 한다

# 사용 기술
- Java, Spring Boot, PostgreSQL, Redis

# 핵심 포인트
- 레디스 트랜잭션을 활용한 선착순 쿠폰 발급
- 비관적 락을 활용한 쿠폰 사용 동시성 처리

# 회고록
- [쿠폰 서비스 사이드 프로젝트 Day 1](https://purplebook.tistory.com/5)
- [쿠폰 서비스 사이드 프로젝트 Day 2](https://purplebook.tistory.com/6)
- [쿠폰 서비스 사이드 프로젝트 Day 3](https://purplebook.tistory.com/7)
- [쿠폰 서비스 사이드 프로젝트 Day 4](https://purplebook.tistory.com/8)
- [쿠폰 서비스 사이드 프로젝트 Day 5](https://purplebook.tistory.com/9)

# API

---
## **Coupon Service API Documentation**

### **1. Create Coupon**

- **Endpoint:** `POST /`
- **Description:** Creates a new coupon.
- **Request Body:**
  ```json
  {
    "name": "string",
    "maxIssuanceCount": "long",
    "usageStartDt": "LocalDateTime",
    "usageExpDt": "LocalDateTime",
    "discountAmount": "integer",
    "discountType": "DiscountType"
  }
  ```
- **Response:**
    - **200 OK**:
      ```json
      {
        "couponId": "long"
      }
      ```

### **2. Issue Customer Coupon**

- **Endpoint:** `POST /`
- **Description:** Issues a coupon to a customer.
- **Request Body:**
  ```json
  {
    "couponId": "long",
    "userId": "long"
  }
  ```
- **Response:**
  - **200 OK**:
    ```json
    {
      "customerCouponId": "UUID"
    }
    ```

### **3. Use Customer Coupon**

- **Endpoint:** `PATCH /{customerCouponId}`
- **Description:** Marks a customer's coupon as used.
- **Path Parameters:**
    - `customerCouponId`: UUID of the customer coupon.
- **Request Body:**
  ```json
  {
    "customerId": "long"
  }
  ```
- **Response:**
    - **200 OK**: No content.

### **4. Get Customer Coupons**

- **Endpoint:** `GET /`
- **Description:** Retrieves a list of coupons issued to a customer.
- **Query Parameters:**
    - `customerId`: ID of the customer.
    - `pageable`: Pagination parameters.
- **Response:**
    - **200 OK**:
      ```json
      {
        "content": [
          {
            "usedAt": "LocalDateTime",
            "couponName": "string",
            "usageStartAt": "LocalDateTime",
            "usageExpAt": "LocalDateTime",
            "discountAmount": "integer",
            "discountType": "DiscountType",
            "issuedAt": "LocalDateTime"
          },
          ...
        ],
        "pageable": {
          ...
        },
        "totalPages": "integer",
        "totalElements": "long",
        ...
      }
      ```
---

# 설치 및 실행
### 다음 프로그램이 설치되어 있거나 해당 서버에 연결 가능해야 합니다.
- PostgreSQL
- Redis


# 테스트
통합 테스트는 testcontainer를 통해 실행됩니다. 따라서 docker가 설치되어 있어야 합니다.
