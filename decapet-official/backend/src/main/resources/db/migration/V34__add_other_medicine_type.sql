-- 기존 CHECK 제약조건 삭제 (Hibernate가 PARASITE, SKIN, COGNITIVE만 허용하도록 생성)
ALTER TABLE medicines DROP CONSTRAINT IF EXISTS medicines_type_check;

-- OTHER를 포함한 새 CHECK 제약조건 추가
ALTER TABLE medicines ADD CONSTRAINT medicines_type_check
    CHECK (type IN ('PARASITE', 'SKIN', 'COGNITIVE', 'OTHER'));
