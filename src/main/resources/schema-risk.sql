create table if not exists project_risk_sagas (
    project_risk_saga_id uuid not null,
    project_allocations_id uuid not null,
    earnings bigint,
    missing_demands jsonb,
    deadline timestamp,
    version bigserial not null,
    primary key (project_risk_saga_id));

