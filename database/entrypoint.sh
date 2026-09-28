#!/bin/bash
# Entrypoint script to initialize the database and run migrations

# Script exits if any command fails or any variable is not set
set -eu

# Run bootstrap.sql and privileges.sql for book_social_network, if needed
export PGPASSWORD="$ROOT_DB_PASSWORD"

DB_EXISTS=$(psql -h "$HOST" -U "$ROOT_DB_USER" -d postgres \
  -tAc "SELECT 1 FROM pg_database WHERE datname='book_social_network'")

if [ "$DB_EXISTS" = "1" ]; then
  echo "Database book_social_network already exists. Skipping bootstrap..."
else
  echo "Database book_social_network doesn't exist. Running bootstrap..."
  psql -h "$HOST" -U "$ROOT_DB_USER" -d postgres \
    -v API_DDL_USER="$API_DDL_USER" \
    -v API_DDL_PASSWORD="$API_DDL_PASSWORD" \
    -v API_DML_USER="$API_DML_USER" \
    -v API_DML_PASSWORD="$API_DML_PASSWORD" \
    -f /init/bootstrap.sql
  echo "book_social_network bootstrap completed!"

  export PGPASSWORD="$API_DDL_PASSWORD"

  echo "Initializing privileges for book_social_network..."
  psql -h "$HOST" -U "$API_DDL_USER" -d book_social_network \
    -v API_DDL_USER="$API_DDL_USER" \
    -v API_DML_USER="$API_DML_USER" \
    -f /init/privileges.sql
  echo "Privileges initialized for book_social_network!"
fi

unset PGPASSWORD

# Run Flyway migrations
JDBC_URL="jdbc:postgresql://${HOST}:5432/book_social_network"

echo "Running Flyway migrations..."
flyway -url="$JDBC_URL" -user="$API_DDL_USER" -password="$API_DDL_PASSWORD" \
  -locations="filesystem:/flyway/sql" migrate

echo "Migration completed!"