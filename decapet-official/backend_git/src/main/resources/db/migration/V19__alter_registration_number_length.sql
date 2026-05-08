-- 동물등록번호 12자리 → 15자리로 변경
ALTER TABLE pets ALTER COLUMN registration_number TYPE VARCHAR(15);
