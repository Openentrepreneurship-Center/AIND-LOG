-- Product 테이블에 필드 추가
ALTER TABLE products ADD COLUMN allow_multiple boolean NOT NULL DEFAULT true;
ALTER TABLE products ADD COLUMN additional_info jsonb;

-- CustomProduct 테이블에 필드 추가
ALTER TABLE custom_products ADD COLUMN additional_info jsonb;
