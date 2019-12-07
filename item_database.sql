CREATE TABLE item (
    id serial PRIMARY KEY,
    name VARCHAR(100),
    price BIGINT default 0,
    amount bigint default 0,
    reserved_amount bigint default 0,
    CONSTRAINT ak_key_2_item UNIQUE (name)
    );

GRANT ALL PRIVILEGES ON TABLE item to example;
