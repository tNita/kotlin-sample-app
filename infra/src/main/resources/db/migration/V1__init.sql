create table authors (
    id uuid primary key,
    name varchar(255) not null,
    birth_date date not null,
    constraint authors_name_birth_unique unique (name, birth_date)
);

create table books (
    id uuid primary key,
    title varchar(255) not null,
    price numeric(10, 2) not null,
    publish_status varchar(20) not null,
    constraint books_price_non_negative check (price >= 0),
    constraint books_publish_status_valid check (publish_status in ('UNPUBLISHED', 'PUBLISHED'))
);

create table book_authors (
    book_id uuid not null references books(id) on delete cascade,
    author_id uuid not null references authors(id) on delete cascade,
    constraint book_authors_pk primary key (book_id, author_id)
);
