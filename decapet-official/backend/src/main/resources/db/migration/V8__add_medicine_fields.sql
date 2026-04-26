-- medicines 테이블에 sale_price, sales_status, additional_info 필드 추가
ALTER TABLE medicines ADD COLUMN sale_price numeric(12,2);
ALTER TABLE medicines ADD COLUMN sales_status varchar(20) NOT NULL DEFAULT 'ON_SALE';
ALTER TABLE medicines ADD COLUMN additional_info jsonb;
