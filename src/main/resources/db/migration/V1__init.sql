-- MicroSaaS para cabeleireiros — modelo multi-tenant (PostgreSQL)

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TYPE user_role AS ENUM ('owner', 'staff');
CREATE TYPE appointment_status AS ENUM (
    'scheduled', 'confirmed', 'completed', 'no_show', 'cancelled'
);
CREATE TYPE transaction_type AS ENUM ('income', 'expense', 'commission');
CREATE TYPE subscription_plan AS ENUM ('solo', 'salon', 'trial');
CREATE TYPE notification_channel AS ENUM ('whatsapp', 'sms', 'email');
CREATE TYPE notification_status AS ENUM ('pending', 'sent', 'failed', 'delivered');

CREATE TABLE salons (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name                VARCHAR(120) NOT NULL,
    slug                VARCHAR(80) UNIQUE NOT NULL,
    phone               VARCHAR(20),
    address             TEXT,
    timezone            VARCHAR(50) DEFAULT 'America/Sao_Paulo',
    logo_url            TEXT,
    subscription_plan   subscription_plan DEFAULT 'trial',
    subscription_status VARCHAR(20) DEFAULT 'active',
    trial_ends_at       TIMESTAMPTZ,
    created_at          TIMESTAMPTZ DEFAULT now(),
    updated_at          TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE users (
    id             UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    salon_id       UUID NOT NULL REFERENCES salons(id) ON DELETE CASCADE,
    name           VARCHAR(120) NOT NULL,
    email          VARCHAR(160) UNIQUE NOT NULL,
    phone          VARCHAR(20),
    role           user_role NOT NULL DEFAULT 'staff',
    password_hash  VARCHAR(255),
    auth_uid       UUID,
    is_active      BOOLEAN DEFAULT TRUE,
    created_at     TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_users_salon ON users(salon_id);

CREATE TABLE professionals (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    salon_id        UUID NOT NULL REFERENCES salons(id) ON DELETE CASCADE,
    user_id         UUID REFERENCES users(id) ON DELETE SET NULL,
    name            VARCHAR(120) NOT NULL,
    photo_url       TEXT,
    bio             TEXT,
    commission_rate NUMERIC(5,2) DEFAULT 0,
    is_active       BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_professionals_salon ON professionals(salon_id);

CREATE TABLE professional_schedules (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    professional_id UUID NOT NULL REFERENCES professionals(id) ON DELETE CASCADE,
    weekday         SMALLINT NOT NULL CHECK (weekday BETWEEN 0 AND 6),
    start_time      TIME NOT NULL,
    end_time        TIME NOT NULL,
    break_start     TIME,
    break_end       TIME
);

CREATE INDEX idx_schedules_professional ON professional_schedules(professional_id);

CREATE TABLE professional_time_off (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    professional_id UUID NOT NULL REFERENCES professionals(id) ON DELETE CASCADE,
    starts_at       TIMESTAMPTZ NOT NULL,
    ends_at         TIMESTAMPTZ NOT NULL,
    reason          VARCHAR(160)
);

CREATE TABLE services (
    id               UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    salon_id         UUID NOT NULL REFERENCES salons(id) ON DELETE CASCADE,
    name             VARCHAR(120) NOT NULL,
    description      TEXT,
    duration_minutes INTEGER NOT NULL,
    price            NUMERIC(10,2) NOT NULL,
    category         VARCHAR(60),
    is_active        BOOLEAN DEFAULT TRUE,
    created_at       TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_services_salon ON services(salon_id);

CREATE TABLE professional_services (
    professional_id  UUID NOT NULL REFERENCES professionals(id) ON DELETE CASCADE,
    service_id       UUID NOT NULL REFERENCES services(id) ON DELETE CASCADE,
    custom_price     NUMERIC(10,2),
    custom_duration  INTEGER,
    PRIMARY KEY (professional_id, service_id)
);

CREATE TABLE clients (
    id             UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    salon_id       UUID NOT NULL REFERENCES salons(id) ON DELETE CASCADE,
    name           VARCHAR(120) NOT NULL,
    phone          VARCHAR(20) NOT NULL,
    email          VARCHAR(160),
    birthday       DATE,
    notes          TEXT,
    loyalty_points INTEGER DEFAULT 0,
    created_at     TIMESTAMPTZ DEFAULT now(),
    UNIQUE (salon_id, phone)
);

CREATE INDEX idx_clients_salon ON clients(salon_id);
CREATE INDEX idx_clients_phone ON clients(phone);

CREATE TABLE client_technical_notes (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    client_id       UUID NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    professional_id UUID REFERENCES professionals(id) ON DELETE SET NULL,
    title           VARCHAR(120),
    details         TEXT,
    photo_url       TEXT,
    created_at      TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_technical_notes_client ON client_technical_notes(client_id);

CREATE TABLE appointments (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    salon_id        UUID NOT NULL REFERENCES salons(id) ON DELETE CASCADE,
    client_id       UUID NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    professional_id UUID NOT NULL REFERENCES professionals(id) ON DELETE CASCADE,
    starts_at       TIMESTAMPTZ NOT NULL,
    ends_at         TIMESTAMPTZ NOT NULL,
    status          appointment_status DEFAULT 'scheduled',
    deposit_paid    BOOLEAN DEFAULT FALSE,
    deposit_amount  NUMERIC(10,2),
    total_price     NUMERIC(10,2) NOT NULL DEFAULT 0,
    notes           TEXT,
    created_via     VARCHAR(20) DEFAULT 'manual',
    created_at      TIMESTAMPTZ DEFAULT now(),
    updated_at      TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_appointments_salon_date ON appointments(salon_id, starts_at);
CREATE INDEX idx_appointments_professional_date ON appointments(professional_id, starts_at);
CREATE INDEX idx_appointments_client ON appointments(client_id);

CREATE TABLE appointment_services (
    id               UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    appointment_id   UUID NOT NULL REFERENCES appointments(id) ON DELETE CASCADE,
    service_id       UUID NOT NULL REFERENCES services(id),
    price_at_booking NUMERIC(10,2) NOT NULL
);

CREATE INDEX idx_appointment_services_appointment ON appointment_services(appointment_id);

CREATE TABLE notifications (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    salon_id        UUID NOT NULL REFERENCES salons(id) ON DELETE CASCADE,
    appointment_id  UUID REFERENCES appointments(id) ON DELETE CASCADE,
    client_id       UUID REFERENCES clients(id) ON DELETE CASCADE,
    channel         notification_channel NOT NULL,
    type            VARCHAR(40) NOT NULL,
    status          notification_status DEFAULT 'pending',
    scheduled_for   TIMESTAMPTZ NOT NULL,
    sent_at         TIMESTAMPTZ,
    created_at      TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_notifications_scheduled ON notifications(scheduled_for) WHERE status = 'pending';

CREATE TABLE transactions (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    salon_id        UUID NOT NULL REFERENCES salons(id) ON DELETE CASCADE,
    appointment_id  UUID REFERENCES appointments(id) ON DELETE SET NULL,
    professional_id UUID REFERENCES professionals(id) ON DELETE SET NULL,
    type            transaction_type NOT NULL,
    category        VARCHAR(60),
    amount          NUMERIC(10,2) NOT NULL,
    payment_method  VARCHAR(30),
    description     TEXT,
    occurred_at     TIMESTAMPTZ DEFAULT now(),
    created_at      TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_transactions_salon_date ON transactions(salon_id, occurred_at);

CREATE TABLE products (
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    salon_id      UUID NOT NULL REFERENCES salons(id) ON DELETE CASCADE,
    name          VARCHAR(120) NOT NULL,
    brand         VARCHAR(80),
    unit_cost     NUMERIC(10,2),
    unit_price    NUMERIC(10,2),
    stock_qty     INTEGER DEFAULT 0,
    min_stock_qty INTEGER DEFAULT 0,
    created_at    TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE stock_movements (
    id             UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id     UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    change_qty     INTEGER NOT NULL,
    reason         VARCHAR(60),
    appointment_id UUID REFERENCES appointments(id) ON DELETE SET NULL,
    created_at     TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE loyalty_transactions (
    id         UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    client_id  UUID NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    points     INTEGER NOT NULL,
    reason     VARCHAR(120),
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE client_service_packages (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    client_id       UUID NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    service_id      UUID NOT NULL REFERENCES services(id),
    total_sessions  INTEGER NOT NULL,
    used_sessions   INTEGER DEFAULT 0,
    purchased_at    TIMESTAMPTZ DEFAULT now(),
    expires_at      TIMESTAMPTZ
);

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_salons_updated_at
    BEFORE UPDATE ON salons
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_appointments_updated_at
    BEFORE UPDATE ON appointments
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
