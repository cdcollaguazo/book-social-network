-- V1__tables.sql
-- Initial schema for book_social_network (Flyway)

-- Create tables and sequences
CREATE TABLE public._user (
                              keycloak_id character varying(255) NOT NULL,
                              created_date timestamp(6) without time zone NOT NULL,
                              date_of_birth date,
                              email character varying(255),
                              first_name character varying(255),
                              last_modified_date timestamp(6) without time zone,
                              last_name character varying(255)
);

CREATE TABLE public.book (
                             id integer NOT NULL,
                             created_by character varying(255) NOT NULL,
                             created_date timestamp(6) without time zone NOT NULL,
                             last_modified_by character varying(255),
                             last_modified_date timestamp(6) without time zone,
                             archived boolean NOT NULL,
                             author_name character varying(255),
                             book_cover character varying(255),
                             isbn character varying(255),
                             shareable boolean NOT NULL,
                             synopsis character varying(1025),
                             title character varying(255),
                             owner_id character varying(255)
);

CREATE SEQUENCE public.book_seq
    START WITH 1
    INCREMENT BY 25
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE public.book_transaction_history (
                                                 id integer NOT NULL,
                                                 created_by character varying(255) NOT NULL,
                                                 created_date timestamp(6) without time zone NOT NULL,
                                                 last_modified_by character varying(255),
                                                 last_modified_date timestamp(6) without time zone,
                                                 return_approved boolean NOT NULL,
                                                 returned boolean NOT NULL,
                                                 book_id integer,
                                                 user_id character varying(255)
);

CREATE SEQUENCE public.book_transaction_history_seq
    START WITH 1
    INCREMENT BY 25
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE public.feedback (
                                 id integer NOT NULL,
                                 created_by character varying(255) NOT NULL,
                                 created_date timestamp(6) without time zone NOT NULL,
                                 last_modified_by character varying(255),
                                 last_modified_date timestamp(6) without time zone,
                                 comment character varying(1025),
                                 note double precision,
                                 book_id integer,
                                 user_id character varying(255)
);

CREATE SEQUENCE public.feedback_seq
    START WITH 1
    INCREMENT BY 25
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE public.user_likes_book (
                                        user_keycloak_id character varying(255) NOT NULL,
                                        book_id integer NOT NULL
);

-- Create table constraints
ALTER TABLE ONLY public._user
    ADD CONSTRAINT pk_user PRIMARY KEY (keycloak_id);

ALTER TABLE ONLY public.book
    ADD CONSTRAINT pk_book PRIMARY KEY (id);

ALTER TABLE ONLY public.book_transaction_history
    ADD CONSTRAINT pk_book_transaction_history PRIMARY KEY (id);

ALTER TABLE ONLY public.feedback
    ADD CONSTRAINT pk_feedback PRIMARY KEY (id);

ALTER TABLE ONLY public._user
    ADD CONSTRAINT uk_user_email UNIQUE (email);

ALTER TABLE ONLY public.book
    ADD CONSTRAINT fk_book_owner
    FOREIGN KEY (owner_id) REFERENCES public._user(keycloak_id);

ALTER TABLE ONLY public.user_likes_book
    ADD CONSTRAINT fk_user_likes_book_book
    FOREIGN KEY (book_id) REFERENCES public.book(id);

ALTER TABLE ONLY public.book_transaction_history
    ADD CONSTRAINT fk_book_transaction_history_book
    FOREIGN KEY (book_id) REFERENCES public.book(id);

ALTER TABLE ONLY public.feedback
    ADD CONSTRAINT fk_feedback_user
    FOREIGN KEY (user_id) REFERENCES public._user(keycloak_id);

ALTER TABLE ONLY public.feedback
    ADD CONSTRAINT fk_feedback_book
    FOREIGN KEY (book_id) REFERENCES public.book(id);

ALTER TABLE ONLY public.book_transaction_history
    ADD CONSTRAINT fk_book_transaction_history_user
    FOREIGN KEY (user_id) REFERENCES public._user(keycloak_id);

ALTER TABLE ONLY public.user_likes_book
    ADD CONSTRAINT fk_user_likes_book_user
    FOREIGN KEY (user_keycloak_id) REFERENCES public._user(keycloak_id);