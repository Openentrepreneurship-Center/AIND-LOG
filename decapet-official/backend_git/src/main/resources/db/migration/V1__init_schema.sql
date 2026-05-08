-- Flyway Migration V1: Initial Schema
-- This file represents the initial database schema for Decapet
-- For existing databases, Flyway will baseline at V1 and skip this migration

-- =============================================
-- TABLES
-- =============================================

-- Users table
CREATE TABLE users (
    id varchar(26) NOT NULL,
    created_at timestamp(6) NOT NULL,
    updated_at timestamp(6) NOT NULL,
    deleted_at timestamp(6),
    email varchar(100) NOT NULL,
    password varchar(255) NOT NULL,
    name varchar(10) NOT NULL,
    phone varchar(11) NOT NULL,
    address varchar(100) NOT NULL,
    zip_code varchar(5) NOT NULL,
    recipient_name varchar(10) NOT NULL,
    recipient_phone varchar(11) NOT NULL,
    buyer_grade varchar(1),
    admin_memo text,
    admin_memo2 text,
    withdrawn boolean NOT NULL DEFAULT false,
    unique_number varchar(50),
    PRIMARY KEY (id)
);

-- Admins table
CREATE TABLE admins (
    id varchar(26) NOT NULL,
    created_at timestamp(6) NOT NULL,
    updated_at timestamp(6) NOT NULL,
    deleted_at timestamp(6),
    login_id varchar(50) NOT NULL,
    password varchar(255) NOT NULL,
    otp_enabled boolean NOT NULL DEFAULT false,
    otp_secret varchar(100),
    PRIMARY KEY (id)
);

-- Breeds table
CREATE TABLE breeds (
    id varchar(26) NOT NULL,
    name varchar(100) NOT NULL,
    species varchar(20) NOT NULL,
    PRIMARY KEY (id)
);

-- Pets table
CREATE TABLE pets (
    id varchar(26) NOT NULL,
    created_at timestamp(6) NOT NULL,
    updated_at timestamp(6) NOT NULL,
    deleted_at timestamp(6),
    user_id varchar(26) NOT NULL,
    breed_id varchar(26) NOT NULL,
    name varchar(100) NOT NULL,
    gender varchar(20),
    birthdate date,
    weight numeric(5,2),
    weight_updated_at timestamp(6),
    neutered boolean,
    photo varchar(500),
    custom_breed varchar(100),
    unique_number varchar(50),
    PRIMARY KEY (id),
    CONSTRAINT pets_gender_check CHECK (gender IN ('MALE', 'FEMALE'))
);

-- Pet Vets table
CREATE TABLE pet_vets (
    id varchar(26) NOT NULL,
    created_at timestamp(6) NOT NULL,
    updated_at timestamp(6) NOT NULL,
    deleted_at timestamp(6),
    pet_id varchar(26) NOT NULL,
    hospital_name varchar(255) NOT NULL,
    vet_name varchar(100) NOT NULL,
    vet_position varchar(50),
    PRIMARY KEY (id)
);

-- Products table
CREATE TABLE products (
    id varchar(26) NOT NULL,
    created_at timestamp(6) NOT NULL,
    updated_at timestamp(6) NOT NULL,
    deleted_at timestamp(6),
    name varchar(255) NOT NULL,
    description text NOT NULL,
    image_url varchar(255) NOT NULL,
    base_price numeric(12,2) NOT NULL,
    price numeric(12,2) NOT NULL,
    weight numeric(10,2) NOT NULL,
    stock_quantity integer NOT NULL,
    expiration_date date NOT NULL,
    PRIMARY KEY (id)
);

-- Medicines table
CREATE TABLE medicines (
    id varchar(26) NOT NULL,
    created_at timestamp(6) NOT NULL,
    updated_at timestamp(6) NOT NULL,
    deleted_at timestamp(6),
    name varchar(255) NOT NULL,
    type varchar(20) NOT NULL,
    description text,
    image_url varchar(255) NOT NULL,
    price numeric(12,2) NOT NULL,
    min_weight numeric(5,2),
    max_weight numeric(5,2),
    months_per_unit integer,
    expiration_date date NOT NULL,
    questionnaire jsonb,
    PRIMARY KEY (id)
);

-- Custom Products table
CREATE TABLE custom_products (
    id varchar(26) NOT NULL,
    created_at timestamp(6) NOT NULL,
    updated_at timestamp(6) NOT NULL,
    deleted_at timestamp(6),
    user_id varchar(26) NOT NULL,
    name varchar(255) NOT NULL,
    description text,
    image_url varchar(255),
    quantity integer NOT NULL,
    weight numeric(10,3),
    requested_price numeric(12,2) NOT NULL,
    approved_price numeric(12,2),
    approved_at timestamp(6),
    status varchar(20) NOT NULL,
    allow_multiple boolean,
    PRIMARY KEY (id)
);

-- Carts table
CREATE TABLE carts (
    id varchar(26) NOT NULL,
    created_at timestamp(6) NOT NULL,
    updated_at timestamp(6) NOT NULL,
    user_id varchar(26) NOT NULL,
    PRIMARY KEY (id)
);

-- Cart Items table
CREATE TABLE cart_items (
    id varchar(26) NOT NULL,
    cart_id varchar(26) NOT NULL,
    item_type varchar(20) NOT NULL,
    product_id varchar(26),
    custom_product_id varchar(26),
    quantity integer NOT NULL,
    PRIMARY KEY (id)
);

-- Medicine Carts table
CREATE TABLE medicine_carts (
    id varchar(26) NOT NULL,
    created_at timestamp(6) NOT NULL,
    updated_at timestamp(6) NOT NULL,
    user_id varchar(26) NOT NULL,
    pet_id varchar(26) NOT NULL,
    PRIMARY KEY (id)
);

-- Medicine Cart Items table
CREATE TABLE medicine_cart_items (
    id varchar(26) NOT NULL,
    cart_id varchar(26) NOT NULL,
    medicine_id varchar(26) NOT NULL,
    medicine_name varchar(255) NOT NULL,
    quantity integer NOT NULL,
    unit_price numeric(12,2) NOT NULL,
    subtotal numeric(12,2) NOT NULL,
    questionnaire_answers jsonb,
    PRIMARY KEY (id)
);

-- Orders table
CREATE TABLE orders (
    id varchar(26) NOT NULL,
    created_at timestamp(6) NOT NULL,
    updated_at timestamp(6) NOT NULL,
    deleted_at timestamp(6),
    user_id varchar(26) NOT NULL,
    order_number varchar(50) NOT NULL,
    status varchar(30) NOT NULL,
    total_amount numeric(12,2) NOT NULL,
    shipping_fee numeric(12,2),
    grand_total numeric(12,2) NOT NULL,
    recipient_name varchar(100),
    phone_number varchar(20),
    address varchar(255),
    zip_code varchar(10),
    delivery_note text,
    cancel_reason text,
    PRIMARY KEY (id)
);

-- Order Items table
CREATE TABLE order_items (
    id varchar(26) NOT NULL,
    order_id varchar(26) NOT NULL,
    item_type varchar(20) NOT NULL,
    item_id varchar(26) NOT NULL,
    item_name varchar(255) NOT NULL,
    image_url varchar(500),
    quantity integer NOT NULL,
    item_quantity integer,
    weight numeric(10,3),
    unit_price numeric(12,2) NOT NULL,
    subtotal numeric(12,2) NOT NULL,
    PRIMARY KEY (id)
);

-- Deliveries table
CREATE TABLE deliveries (
    id varchar(26) NOT NULL,
    created_at timestamp(6) NOT NULL,
    updated_at timestamp(6) NOT NULL,
    deleted_at timestamp(6),
    order_id varchar(26) NOT NULL,
    user_id varchar(26) NOT NULL,
    status varchar(30) NOT NULL,
    recipient_name varchar(100),
    phone_number varchar(20),
    address varchar(255),
    zip_code varchar(10),
    delivery_note text,
    carrier varchar(50),
    tracking_number varchar(100),
    delivered_at timestamp(6),
    cancel_reason text,
    PRIMARY KEY (id)
);

-- Payments table
CREATE TABLE payments (
    id varchar(26) NOT NULL,
    created_at timestamp(6) NOT NULL,
    updated_at timestamp(6) NOT NULL,
    deleted_at timestamp(6),
    user_id varchar(26) NOT NULL,
    order_id varchar(26),
    appointment_id varchar(26),
    prescription_id varchar(26),
    payment_type varchar(20) NOT NULL,
    payment_method varchar(20),
    amount numeric(12,2) NOT NULL,
    status varchar(20) NOT NULL,
    pg_transaction_id varchar(100),
    failure_reason varchar(255),
    PRIMARY KEY (id)
);

-- Schedules table
CREATE TABLE schedules (
    id varchar(26) NOT NULL,
    created_at timestamp(6) NOT NULL,
    updated_at timestamp(6) NOT NULL,
    schedule_date date NOT NULL,
    PRIMARY KEY (id)
);

-- Time Slots table
CREATE TABLE time_slots (
    id varchar(26) NOT NULL,
    schedule_id varchar(26) NOT NULL,
    time time NOT NULL,
    location varchar(255) NOT NULL,
    PRIMARY KEY (id)
);

-- Appointments table
CREATE TABLE appointments (
    id varchar(26) NOT NULL,
    created_at timestamp(6) NOT NULL,
    updated_at timestamp(6) NOT NULL,
    deleted_at timestamp(6),
    user_id varchar(26) NOT NULL,
    pet_id varchar(26) NOT NULL,
    time_slot_id varchar(26) NOT NULL,
    status varchar(30) NOT NULL,
    total_amount numeric(12,2),
    PRIMARY KEY (id)
);

-- Appointment Medicine Items table
CREATE TABLE appointment_medicine_items (
    id varchar(26) NOT NULL,
    appointment_id varchar(26),
    medicine_id varchar(26) NOT NULL,
    medicine_name varchar(255) NOT NULL,
    quantity integer NOT NULL,
    unit_price numeric(12,2) NOT NULL,
    subtotal numeric(12,2) NOT NULL,
    questionnaire_answers jsonb,
    PRIMARY KEY (id)
);

-- Prescriptions table
CREATE TABLE prescriptions (
    id varchar(26) NOT NULL,
    created_at timestamp(6) NOT NULL,
    updated_at timestamp(6) NOT NULL,
    deleted_at timestamp(6),
    pet_id varchar(26) NOT NULL,
    time_slot_id varchar(26) NOT NULL,
    type varchar(30) NOT NULL,
    status varchar(30) NOT NULL,
    description text,
    medicine_name varchar(100),
    medicine_weight varchar(50),
    dosage_frequency varchar(100),
    dosage_period varchar(50),
    price numeric(10,2),
    additional_notes text,
    attachment_urls jsonb,
    PRIMARY KEY (id)
);

-- Posts table
CREATE TABLE posts (
    id varchar(26) NOT NULL,
    created_at timestamp(6) NOT NULL,
    updated_at timestamp(6) NOT NULL,
    deleted_at timestamp(6),
    user_id varchar(26) NOT NULL,
    type varchar(30) NOT NULL,
    title varchar(255) NOT NULL,
    content text,
    status varchar(30) NOT NULL,
    is_anonymous boolean NOT NULL DEFAULT false,
    admin_response text,
    attachment_urls jsonb,
    PRIMARY KEY (id)
);

-- Banners table
CREATE TABLE banners (
    id varchar(26) NOT NULL,
    created_at timestamp(6) NOT NULL,
    updated_at timestamp(6) NOT NULL,
    deleted_at timestamp(6),
    title varchar(255) NOT NULL,
    image_url varchar(500) NOT NULL,
    display_order integer NOT NULL,
    start_at timestamp(6),
    end_at timestamp(6),
    PRIMARY KEY (id)
);

-- Terms table
CREATE TABLE terms (
    id varchar(26) NOT NULL,
    created_at timestamp(6) NOT NULL,
    type varchar(50) NOT NULL,
    version integer NOT NULL,
    content text NOT NULL,
    is_required boolean NOT NULL DEFAULT true,
    effective_date date NOT NULL,
    PRIMARY KEY (id)
);

-- User Term Consents table
CREATE TABLE user_term_consents (
    user_id varchar(255) NOT NULL,
    term_id varchar(26) NOT NULL,
    version integer NOT NULL,
    agreed_at timestamp(6) NOT NULL,
    PRIMARY KEY (user_id, term_id, version)
);

-- User Permissions table
CREATE TABLE user_permissions (
    user_id varchar(26) NOT NULL,
    permissions varchar(255)
);

-- Refresh Tokens table
CREATE TABLE refresh_tokens (
    id varchar(26) NOT NULL,
    created_at timestamp(6) NOT NULL,
    user_id varchar(26) NOT NULL,
    token_hash varchar(255) NOT NULL,
    expires_at timestamp(6) NOT NULL,
    PRIMARY KEY (id)
);

-- Verifications table
CREATE TABLE verifications (
    id varchar(26) NOT NULL,
    created_at timestamp(6) NOT NULL,
    phone varchar(11) NOT NULL,
    code varchar(10) NOT NULL,
    token varchar(255),
    token_used boolean NOT NULL DEFAULT false,
    verified_at timestamp(6),
    expires_at timestamp(6) NOT NULL,
    PRIMARY KEY (id)
);

-- =============================================
-- FOREIGN KEYS
-- =============================================

ALTER TABLE pets ADD CONSTRAINT fk_pets_user FOREIGN KEY (user_id) REFERENCES users(id);
ALTER TABLE pets ADD CONSTRAINT fk_pets_breed FOREIGN KEY (breed_id) REFERENCES breeds(id);

ALTER TABLE pet_vets ADD CONSTRAINT fk_pet_vets_pet FOREIGN KEY (pet_id) REFERENCES pets(id);

ALTER TABLE custom_products ADD CONSTRAINT fk_custom_products_user FOREIGN KEY (user_id) REFERENCES users(id);

ALTER TABLE carts ADD CONSTRAINT fk_carts_user FOREIGN KEY (user_id) REFERENCES users(id);

ALTER TABLE cart_items ADD CONSTRAINT fk_cart_items_cart FOREIGN KEY (cart_id) REFERENCES carts(id);

ALTER TABLE medicine_carts ADD CONSTRAINT fk_medicine_carts_user FOREIGN KEY (user_id) REFERENCES users(id);
ALTER TABLE medicine_carts ADD CONSTRAINT fk_medicine_carts_pet FOREIGN KEY (pet_id) REFERENCES pets(id);

ALTER TABLE medicine_cart_items ADD CONSTRAINT fk_medicine_cart_items_cart FOREIGN KEY (cart_id) REFERENCES medicine_carts(id);

ALTER TABLE orders ADD CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(id);

ALTER TABLE order_items ADD CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id);

ALTER TABLE deliveries ADD CONSTRAINT fk_deliveries_order FOREIGN KEY (order_id) REFERENCES orders(id);
ALTER TABLE deliveries ADD CONSTRAINT fk_deliveries_user FOREIGN KEY (user_id) REFERENCES users(id);

ALTER TABLE payments ADD CONSTRAINT fk_payments_user FOREIGN KEY (user_id) REFERENCES users(id);

ALTER TABLE time_slots ADD CONSTRAINT fk_time_slots_schedule FOREIGN KEY (schedule_id) REFERENCES schedules(id);

ALTER TABLE appointments ADD CONSTRAINT fk_appointments_user FOREIGN KEY (user_id) REFERENCES users(id);
ALTER TABLE appointments ADD CONSTRAINT fk_appointments_pet FOREIGN KEY (pet_id) REFERENCES pets(id);
ALTER TABLE appointments ADD CONSTRAINT fk_appointments_time_slot FOREIGN KEY (time_slot_id) REFERENCES time_slots(id);

ALTER TABLE prescriptions ADD CONSTRAINT fk_prescriptions_pet FOREIGN KEY (pet_id) REFERENCES pets(id);
ALTER TABLE prescriptions ADD CONSTRAINT fk_prescriptions_time_slot FOREIGN KEY (time_slot_id) REFERENCES time_slots(id);

ALTER TABLE posts ADD CONSTRAINT fk_posts_user FOREIGN KEY (user_id) REFERENCES users(id);

ALTER TABLE user_term_consents ADD CONSTRAINT fk_user_term_consents_user FOREIGN KEY (user_id) REFERENCES users(id);

ALTER TABLE user_permissions ADD CONSTRAINT fk_user_permissions_user FOREIGN KEY (user_id) REFERENCES users(id);

ALTER TABLE refresh_tokens ADD CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id);

-- =============================================
-- INDEXES
-- =============================================

CREATE INDEX idx_pets_user_deleted ON pets(user_id, deleted_at);
CREATE INDEX idx_pets_breed ON pets(breed_id);
