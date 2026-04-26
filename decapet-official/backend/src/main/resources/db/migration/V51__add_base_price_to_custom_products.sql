ALTER TABLE custom_products ADD COLUMN base_price NUMERIC(12,2);
UPDATE custom_products SET base_price = COALESCE(approved_price, requested_price);
