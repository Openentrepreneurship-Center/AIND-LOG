-- site_settings 테이블에서 개별 회사정보 컬럼 제거 (companyInfoHtml로 대체)
ALTER TABLE site_settings DROP COLUMN company_name;
ALTER TABLE site_settings DROP COLUMN ceo_name;
ALTER TABLE site_settings DROP COLUMN business_number;
ALTER TABLE site_settings DROP COLUMN partner_hospital_name;
ALTER TABLE site_settings DROP COLUMN partner_hospital_description;
ALTER TABLE site_settings DROP COLUMN instagram_url;
ALTER TABLE site_settings DROP COLUMN kakao_url;
