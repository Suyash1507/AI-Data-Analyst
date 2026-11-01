CREATE TABLE datasets (
    id UUID PRIMARY KEY,
    original_filename VARCHAR(255) NOT NULL,
    storage_key VARCHAR(100) NOT NULL UNIQUE,
    content_type VARCHAR(150),
    size_bytes BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    uploaded_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_datasets_uploaded_at
    ON datasets (uploaded_at DESC);