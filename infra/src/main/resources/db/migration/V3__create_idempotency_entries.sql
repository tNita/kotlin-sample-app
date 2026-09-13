create table idempotency_entries (
    idempotency_key varchar(255) primary key,
    completed_at timestamp with time zone
);
