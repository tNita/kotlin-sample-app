alter table idempotency_entries add column expires_at timestamp with time zone;
update idempotency_entries set expires_at = current_timestamp where expires_at is null;
alter table idempotency_entries alter column expires_at set not null;
