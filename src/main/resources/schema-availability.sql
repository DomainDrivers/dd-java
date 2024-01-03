create table if not exists availabilities (
    id uuid not null,
    resource_id uuid not null,
    resource_parent_id uuid,
    version bigserial not null,
    from_date timestamp not null,
    to_date timestamp not null,
    taken_by uuid,
    disabled boolean not null,
    primary key (id),
    unique(resource_id, from_date, to_date));

