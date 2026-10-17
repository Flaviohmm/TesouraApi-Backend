CREATE TYPE payment_status AS ENUM ('pending', 'approved', 'rejected', 'refunded', 'cancelled');

CREATE TABLE stripe_accounts (
    id                UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    salon_id          UUID NOT NULL UNIQUE REFERENCES salons(id) ON DELETE CASCADE,
    account_id        VARCHAR(40) NOT NULL UNIQUE,
    charges_enabled   BOOLEAN NOT NULL DEFAULT false,
    details_submitted BOOLEAN NOT NULL DEFAULT false,
    status            VARCHAR(20) NOT NULL DEFAULT 'pending',
    connected_at      TIMESTAMPTZ DEFAULT now(),
    updated_at        TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE payments (
    id                   UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    salon_id             UUID NOT NULL REFERENCES salons(id) ON DELETE CASCADE,
    appointment_id       UUID NOT NULL REFERENCES appointments(id) ON DELETE CASCADE,
    provider             VARCHAR(20) NOT NULL DEFAULT 'stripe',
    external_reference   VARCHAR(64) NOT NULL UNIQUE,
    checkout_session_id  VARCHAR(80),
    payment_intent_id    VARCHAR(80),
    amount               NUMERIC(10,2) NOT NULL,
    platform_fee_amount  NUMERIC(10,2) NOT NULL,
    status               payment_status NOT NULL DEFAULT 'pending',
    checkout_url         TEXT,
    created_at           TIMESTAMPTZ DEFAULT now(),
    updated_at           TIMESTAMPTZ DEFAULT now()
);
CREATE INDEX idx_payments_appointment ON payments(appointment_id);
CREATE INDEX idx_payments_salon ON payments(salon_id);

CREATE TABLE processed_webhook_events (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    provider    VARCHAR(20) NOT NULL,
    event_key   VARCHAR(160) NOT NULL,
    received_at TIMESTAMPTZ DEFAULT now(),
    UNIQUE (provider, event_key)
);

CREATE TRIGGER trg_stripe_accounts_updated_at
    BEFORE UPDATE ON stripe_accounts
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_payments_updated_at
    BEFORE UPDATE ON payments
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
