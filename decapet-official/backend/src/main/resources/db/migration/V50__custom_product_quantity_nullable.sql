-- 한 세트당 개수(quantity)를 nullable로 변경
ALTER TABLE custom_products ALTER COLUMN quantity DROP NOT NULL;
