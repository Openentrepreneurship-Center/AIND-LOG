-- 기존 CHECK 제약조건 삭제 (Hibernate가 DOG, CAT만 허용하도록 생성)
ALTER TABLE breeds DROP CONSTRAINT IF EXISTS breeds_species_check;

-- OTHER를 포함한 새 CHECK 제약조건 추가
ALTER TABLE breeds ADD CONSTRAINT breeds_species_check
    CHECK (species IN ('DOG', 'CAT', 'OTHER'));

-- 기타 품종 추가
INSERT INTO breeds (id, species, name) VALUES ('ETC', 'OTHER', '기타');
