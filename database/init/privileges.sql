-- privileges.sql
-- Initializes the privileges

\set ON_ERROR_STOP on


-- Application role can use the schema but cannot create objects
SELECT format('GRANT USAGE ON SCHEMA public TO %I', :'API_DML_USER')\gexec
SELECT format('REVOKE CREATE ON SCHEMA public FROM %I', :'API_DML_USER')\gexec


-- Allow the application to read and write existing tables
SELECT format('GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO %I', :'API_DML_USER')\gexec

-- Allow the application to use sequences (needed for auto-increment IDs)
SELECT format('GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA public TO %I', :'API_DML_USER')\gexec


-- Ensure future tables created by the migration role are accessible by the app
SELECT format(
               'ALTER DEFAULT PRIVILEGES FOR ROLE %I IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO %I',
               :'API_DDL_USER', :'API_DML_USER'
       )\gexec

-- Ensure future sequences created by the migration role are accessible by the app
SELECT format(
               'ALTER DEFAULT PRIVILEGES FOR ROLE %I IN SCHEMA public GRANT USAGE, SELECT, UPDATE ON SEQUENCES TO %I',
               :'API_DDL_USER', :'API_DML_USER'
       )\gexec
