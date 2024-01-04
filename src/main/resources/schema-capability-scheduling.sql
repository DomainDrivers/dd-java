create table if not exists allocatable_capabilities (
    id uuid not null,
    resource_id uuid not null,
    possible_capabilities jsonb not null,
    from_date timestamp not null,
    to_date timestamp not null,
    primary key (id));
