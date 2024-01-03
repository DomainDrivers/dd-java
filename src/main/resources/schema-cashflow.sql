create table if not exists cashflows (
    project_allocations_id uuid not null,
    version bigserial not null,
    cost bigint,
    income bigint,
    primary key (project_allocations_id));
