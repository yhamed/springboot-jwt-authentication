DROP SCHEMA IF EXISTS test CASCADE;

CREATE SCHEMA IF NOT EXISTS test;

ALTER
DATABASE postgres OWNER TO postgres;

ALTER
SCHEMA test OWNER TO postgres;

CREATE TABLE test.roles
(
    id   integer NOT NULL,
    name character varying(20)
);

ALTER TABLE test.roles OWNER TO postgres;

CREATE SEQUENCE test.roles_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE CACHE 1;


ALTER TABLE test.roles_id_seq OWNER TO postgres;

ALTER SEQUENCE test.roles_id_seq OWNED BY test.roles.id;

CREATE TABLE test.user_roles
(
    user_id bigint  NOT NULL,
    role_id integer NOT NULL
);

CREATE TABLE test.users
(
    id       bigint NOT NULL,
    email    character varying(50),
    password character varying(120),
    username character varying(20)
);

ALTER TABLE test.users OWNER TO postgres;

CREATE SEQUENCE test.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE CACHE 1;

ALTER TABLE test.users_id_seq OWNER TO postgres;

ALTER SEQUENCE test.users_id_seq OWNED BY test.users.id;

ALTER TABLE ONLY test.roles ALTER COLUMN id SET DEFAULT nextval('test.roles_id_seq'::regclass);

ALTER TABLE ONLY test.users ALTER COLUMN id SET DEFAULT nextval('test.users_id_seq'::regclass);

ALTER TABLE ONLY test.roles
    ADD CONSTRAINT roles_pkey PRIMARY KEY (id);

ALTER TABLE ONLY test.users
    ADD CONSTRAINT uk6dotkott2kjsp8vw4d0m25fb7 UNIQUE (email) ON CONFLICT DO NOTHING;

ALTER TABLE ONLY test.users
    ADD CONSTRAINT ukr43af9ap4edm43mmtq01oddj6 UNIQUE (username) ON CONFLICT DO NOTHING;

ALTER TABLE ONLY test.user_roles
    ADD CONSTRAINT user_roles_pkey PRIMARY KEY (user_id, role_id);

ALTER TABLE ONLY test.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);

ALTER TABLE ONLY test.user_roles
    ADD CONSTRAINT fkh8ciramu9cc9q3qcqiv4ue8a6 FOREIGN KEY (role_id) REFERENCES test.roles(id) ON CONFLICT DO NOTHING;

ALTER TABLE ONLY test.user_roles
    ADD CONSTRAINT fkhfh9dx7w3ubf1co1vdev94g3f FOREIGN KEY (user_id) REFERENCES test.users(id) ON CONFLICT DO NOTHING;

INSERT INTO roles(id, name)
VALUES (1, 'ROLE_USER') ON CONFLICT DO NOTHING;
INSERT INTO roles(id, name)
VALUES (2, 'ROLE_ADMIN') ON CONFLICT DO NOTHING;
