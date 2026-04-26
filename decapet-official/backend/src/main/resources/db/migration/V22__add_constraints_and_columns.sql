-- 1. 중복 예약 방지 unique index
CREATE UNIQUE INDEX idx_appointment_user_timeslot
ON appointments (user_id, time_slot_id)
WHERE deleted_at IS NULL AND status IN ('PENDING', 'APPROVED', 'COMPLETED');

-- 2. 의약품 결제 필드 추가
ALTER TABLE payments ADD COLUMN account_holder VARCHAR(20);
ALTER TABLE payments ADD COLUMN cash_receipt_type VARCHAR(10);
ALTER TABLE payments ADD COLUMN cash_receipt_number VARCHAR(20);

-- 3. 낙관적 잠금 (관리자 승인/거절 race condition 방지)
ALTER TABLE appointments ADD COLUMN version BIGINT DEFAULT 0;
ALTER TABLE prescriptions ADD COLUMN version BIGINT DEFAULT 0;

-- 4. 관리자 RBAC: role 필드
ALTER TABLE admins ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'ADMIN';

-- 5. 관리자 계정 잠금 필드
ALTER TABLE admins ADD COLUMN failed_login_attempts INT NOT NULL DEFAULT 0;
ALTER TABLE admins ADD COLUMN locked_until TIMESTAMP(6);

-- 6. 감사 로그 테이블
CREATE TABLE audit_logs (
    id VARCHAR(26) PRIMARY KEY,
    admin_id VARCHAR(26) NOT NULL,
    action VARCHAR(50) NOT NULL,
    target_type VARCHAR(50),
    target_id VARCHAR(26),
    details JSONB,
    ip_address VARCHAR(45),
    created_at TIMESTAMP(6) NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_audit_logs_admin_id ON audit_logs(admin_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);
