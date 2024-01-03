create table if not exists project_allocations (
    project_allocations_id uuid not null,
    allocations jsonb not null,
    demands jsonb not null,
    from_date timestamp,
    to_date timestamp,
    primary key (project_allocations_id));

create table if not exists allocatable_capabilities (
    id uuid not null,
    resource_id uuid not null,
    possible_capabilities jsonb not null,
    from_date timestamp not null,
    to_date timestamp not null,
    primary key (id));

