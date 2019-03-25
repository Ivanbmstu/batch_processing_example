create table transactions (
    id serial primary key,
    amount numeric(19,2),
    data json
);