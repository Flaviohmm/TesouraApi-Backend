-- Customer accounts span multiple salons, unlike the salon-scoped `clients` CRM table.
CREATE TABLE customers (
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name          VARCHAR(120) NOT NULL,
    email         VARCHAR(160) NOT NULL UNIQUE,
    phone         VARCHAR(20),
    password_hash VARCHAR(255) NOT NULL,
    created_at    TIMESTAMPTZ DEFAULT now()
);

-- Links a salon's own Client record back to the cross-salon Customer account that created it.
ALTER TABLE clients ADD COLUMN customer_id UUID REFERENCES customers(id) ON DELETE SET NULL;
CREATE INDEX idx_clients_customer ON clients(customer_id);
