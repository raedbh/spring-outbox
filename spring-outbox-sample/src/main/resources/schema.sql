create database if not exists s2p;

create table proposals
(
    id                binary(16)     not null primary key,
    rfp               binary(16)     null,
    vendor            binary(16)     null,
    proposal_amount   decimal(38, 2) null,
    status            tinyint        null,
    review_started_at datetime(6)    null,
    submitted_at      datetime(6)    null,
    details           varchar(255)   null,
    constraint chk_status_range check (`status` between 0 and 4)
);

create table requirement_labels
(
    id    binary(16)   not null primary key,
    label varchar(255) null
);

create table rfps
(
    id                  binary(16)   not null primary key,
    title               varchar(255) null,
    description         varchar(255) null,
    proposal_awarded    bit          not null,
    status              tinyint      null,
    created_at          datetime(6)  null,
    last_updated_at     datetime(6)  null,
    published_at        datetime(6)  null,
    submission_deadline datetime(6)  null,
    constraint chk_rfp_status_range check (`status` between 0 and 2)
);

create table requirements
(
    id                 binary(16)   not null primary key,
    rfp_id             binary(16)   null,
    label_id           binary(16)   null,
    requirements_order int          null,
    description        varchar(255) null,
    constraint fk_requirements_rfp foreign key (rfp_id) references rfps (id)
);

create table vendors
(
    id   binary(16)   not null primary key,
    name varchar(255) null,
    constraint uq_vendor_name unique (name)
);
