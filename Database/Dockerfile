FROM postgres

ENV POSTGRES_USER example
ENV POSTGRES_PASSWORD example
ENV POSTGRES_DB item_db

COPY init.sql /docker-entrypoint-initdb.d/
