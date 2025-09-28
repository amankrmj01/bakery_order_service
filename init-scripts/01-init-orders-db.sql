-- Create extensions if needed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create order status enum type
CREATE TYPE order_status AS ENUM ('PENDING', 'CONFIRMED', 'PREPARING', 'READY', 'OUT_FOR_DELIVERY', 'DELIVERED', 'CANCELLED');

-- Create delivery type enum type
CREATE TYPE delivery_type AS ENUM ('PICKUP', 'DELIVERY');

-- Ensure proper permissions
GRANT ALL PRIVILEGES ON DATABASE bakery_orders TO order_user;

-- Create indexes for better performance
-- These will be created automatically by JPA, but good to have as reference

-- Order-specific indexes (JPA will create these automatically)
-- CREATE INDEX IF NOT EXISTS idx_order_user ON orders(user_id);
-- CREATE INDEX IF NOT EXISTS idx_order_status ON orders(status);
-- CREATE INDEX IF NOT EXISTS idx_order_date ON orders(created_at);
-- CREATE INDEX IF NOT EXISTS idx_order_number ON orders(order_number);
-- CREATE INDEX IF NOT EXISTS idx_order_delivery_date ON orders(delivery_date);

-- Order item indexes
-- CREATE INDEX IF NOT EXISTS idx_order_item_order ON order_items(order_id);
-- CREATE INDEX IF NOT EXISTS idx_order_item_product ON order_items(product_id);
