-- Add missing UNIQUE constraints that exist in JPA entities but not in DB

ALTER TABLE orders ADD CONSTRAINT uk_orders_order_number UNIQUE (order_number);

ALTER TABLE schedules ADD CONSTRAINT uk_schedules_schedule_date UNIQUE (schedule_date);

ALTER TABLE refresh_tokens ADD CONSTRAINT uk_refresh_tokens_token_hash UNIQUE (token_hash);

ALTER TABLE admins ADD CONSTRAINT uk_admins_login_id UNIQUE (login_id);

ALTER TABLE deliveries ADD CONSTRAINT uk_deliveries_tracking_number UNIQUE (tracking_number);
