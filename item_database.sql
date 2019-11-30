CREATE TABLE item (id serial PRIMARY KEY, name VARCHAR(100), price BIGINT);

CREATE TABLE item_warehouse (id serial PRIMARY KEY, amount bigint, reserved_amount bigint);

GRANT ALL PRIVILEGES ON TABLE item to example;

GRANT ALL PRIVILEGES ON TABLE item_warehouse to example;
