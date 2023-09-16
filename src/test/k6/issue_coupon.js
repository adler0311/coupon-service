import http from 'k6/http';
import { sleep, check } from 'k6';

export let options = {
    // stages: [
    //     { duration: '10s', target: 100 },
    //     { duration: '30s', target: 100 },
    //     { duration: '10s', target: 0 },
    // ],
    duration: '20s',
    rps: 1500,
    vus: 10

};

export default function () {
    // VU 별로 userId 시작점을 할당합니다.
    // VU 1 ~
    // __ITER 0 ~
    // VU 1: 0 ~ 1499
    // VU 2: 1500 ~ 1999
    // VU 3: 2000 ~ 2499
    // ...
    let baseUserId = 1500*(__VU - 1) + __ITER + 1
    // console.log(__VU, __ITER, baseUserId)


    let payload = JSON.stringify({
        couponId: 302,
        userId: baseUserId
    });
    let headers = {
        'Content-Type': 'application/json',
    };

    const res = http.post('http://localhost:8080/customer-coupons', payload, { headers: headers });
    check(res, {
        'is status 200': (r) => r.status === 200,
    });
    check(res, {
        'is status 400': (r) => r.status === 400
    })
    sleep(1);
}