create table if not exists employees
(
    employee_id  uuid      not null,
    version      bigserial not null,
    name         varchar   not null,
    seniority    varchar   not null,
    last_name    varchar   not null,
    capabilities jsonb     not null,
    primary key (employee_id)
);

create table if not exists devices
(
    device_id    uuid      not null,
    version      bigserial not null,
    model        varchar   not null,
    capabilities jsonb     not null,
    primary key (device_id)
);
