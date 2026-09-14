-- Align the initial notifications table with the Notification JPA entity.
ALTER TABLE notifications RENAME COLUMN scheduled_for TO scheduled_at;

ALTER TABLE notifications
    ADD COLUMN recipient VARCHAR(255),
    ADD COLUMN message TEXT,
    ADD COLUMN error_message TEXT,
    ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT now();
