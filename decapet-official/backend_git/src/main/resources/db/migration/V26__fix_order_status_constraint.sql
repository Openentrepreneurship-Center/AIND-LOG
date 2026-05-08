-- orders_status_check 제약에 SHIPPED 누락 수정 + DELIVERED 상태 통합
-- SHIPPED/DELIVERED를 "발송완료"(SHIPPED) 하나로 통합

-- 기존 DELIVERED 데이터를 SHIPPED로 변환
UPDATE orders SET status = 'SHIPPED' WHERE status = 'DELIVERED';
UPDATE deliveries SET status = 'SHIPPING' WHERE status = 'DELIVERED';

-- 기존 CHECK 제약 삭제 및 재생성
ALTER TABLE orders DROP CONSTRAINT IF EXISTS orders_status_check;
ALTER TABLE orders ADD CONSTRAINT orders_status_check
    CHECK (status IN ('PENDING', 'CONFIRMED', 'SHIPPED', 'CANCELLED'));

ALTER TABLE deliveries DROP CONSTRAINT IF EXISTS deliveries_status_check;
ALTER TABLE deliveries ADD CONSTRAINT deliveries_status_check
    CHECK (status IN ('SHIPPING', 'CANCELLED'));
