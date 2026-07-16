create table if not exists outbox (
    id uuid not null,
    type varchar(255) not null,
    payload text not null,
    occurred_at timestamp not null,
    sent_at timestamp,
    primary key (id));
