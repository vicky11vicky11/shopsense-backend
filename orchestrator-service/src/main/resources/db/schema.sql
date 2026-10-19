CREATE SCHEMA IF NOT EXISTS orchestrator;
CREATE TABLE IF NOT EXISTS orchestrator.order_sagas (
    id UUID PRIMARY KEY, order_id UUID NOT NULL UNIQUE, correlation_id UUID NOT NULL, user_id UUID NOT NULL,
    amount NUMERIC(19,2) NOT NULL, currency VARCHAR(3) NOT NULL, status VARCHAR(60) NOT NULL,
    failure_reason VARCHAR(1000), version BIGINT, created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS orchestrator.saga_events (
    id UUID PRIMARY KEY, event_id UUID NOT NULL UNIQUE, correlation_id UUID, order_id UUID,
    event_type VARCHAR(100) NOT NULL, payload TEXT NOT NULL, received_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_saga_events_order ON orchestrator.saga_events(order_id, received_at);
CREATE TABLE IF NOT EXISTS orchestrator.saga_commands (
    id UUID PRIMARY KEY, correlation_id UUID NOT NULL, order_id UUID NOT NULL, topic VARCHAR(100) NOT NULL,
    command_type VARCHAR(100) NOT NULL, payload TEXT NOT NULL, published_at TIMESTAMP WITH TIME ZONE,
    attempts INTEGER NOT NULL DEFAULT 0, created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_saga_command_unpublished ON orchestrator.saga_commands(published_at, created_at);
CREATE TABLE IF NOT EXISTS orchestrator.shedlock (
    name VARCHAR(64) NOT NULL PRIMARY KEY,
    lock_until TIMESTAMP WITH TIME ZONE NOT NULL,
    locked_at TIMESTAMP WITH TIME ZONE NOT NULL,
    locked_by VARCHAR(255) NOT NULL
);
