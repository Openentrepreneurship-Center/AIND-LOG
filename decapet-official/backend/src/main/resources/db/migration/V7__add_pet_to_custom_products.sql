-- CustomProduct 테이블에 pet_id 컬럼 추가
ALTER TABLE custom_products ADD COLUMN pet_id varchar(26) NOT NULL;

-- 외래키 제약조건 추가
ALTER TABLE custom_products
    ADD CONSTRAINT fk_custom_products_pet
    FOREIGN KEY (pet_id) REFERENCES pets(id);

-- 인덱스 추가 (조회 성능 향상)
CREATE INDEX idx_custom_products_pet ON custom_products(pet_id);
