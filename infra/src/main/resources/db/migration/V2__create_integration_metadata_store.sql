create table int_metadata_store (
    metadata_key varchar(255) not null,
    metadata_value varchar(4000),
    region varchar(100) not null,
    constraint int_metadata_store_pk primary key (metadata_key, region)
);
