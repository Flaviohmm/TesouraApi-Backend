-- Track loyalty points redeemed as a discount on a booking, so edits and
-- receipts can reconstruct how the stored total_price was reached.
ALTER TABLE appointments
    ADD COLUMN loyalty_points_redeemed INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN loyalty_discount_amount NUMERIC(10,2) NOT NULL DEFAULT 0;
