create table if not exists projects (
    project_id uuid not null,
    version bigserial not null,
    name varchar not null,
    parallelized_stages jsonb,
    chosen_resources jsonb,
    schedule jsonb,
    all_demands jsonb,
    demands_per_stage jsonb,
    primary key (project_id));


