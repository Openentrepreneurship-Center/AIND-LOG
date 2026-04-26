-- Create site_settings table
CREATE TABLE site_settings (
    id varchar(26) NOT NULL,
    created_at timestamp(6) NOT NULL,
    updated_at timestamp(6) NOT NULL,

    -- 상단 배너
    banner_text varchar(500),
    banner_enabled boolean NOT NULL DEFAULT true,

    -- 회사 정보
    company_name varchar(100),
    ceo_name varchar(50),
    business_number varchar(20),
    partner_hospital_name varchar(200),
    partner_hospital_description text,
    instagram_url varchar(500),
    kakao_url varchar(500),

    -- 섹션 타이틀
    product_section_title varchar(100),
    medicine_section_title varchar(100),

    PRIMARY KEY (id)
);

-- 기본 설정 데이터 삽입
INSERT INTO site_settings (
    id, created_at, updated_at,
    banner_text, banner_enabled,
    company_name, ceo_name, business_number,
    partner_hospital_name, partner_hospital_description,
    instagram_url, kakao_url,
    product_section_title, medicine_section_title
) VALUES (
    '01JJSITESETTING000001', NOW(), NOW(),
    '데카펫은 회원가입 승인 후 이용 가능합니다.', true,
    '주식회사 비트진컴패니언', '김두현', '595-88-02163',
    '동편동물혈액검진센터(서울시 서초구 소재)', '전문적인 진료와 검사를 통해 반려동물의 건강을 관리합니다.',
    'https://www.instagram.com/pet_blood/', 'http://pf.kakao.com/_evBBxj',
    '사료', '진료 예약'
);
