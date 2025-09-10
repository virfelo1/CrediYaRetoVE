--
-- PostgreSQL database dump
--

-- Dumped from database version 17.6
-- Dumped by pg_dump version 17.0

-- Started on 2025-09-08 20:47:30

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
--SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 223 (class 1259 OID 16474)
-- Name: loan_type; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.loan_type (
    id_loan_type integer NOT NULL,
    name_loan character varying(100) NOT NULL,
    min_amount numeric(12,2) NOT NULL,
    max_amount numeric(12,2) NOT NULL,
    interest_rate numeric(5,2) NOT NULL,
    auto_validation boolean DEFAULT false NOT NULL
);


ALTER TABLE public.loan_type OWNER TO postgres;

--
-- TOC entry 222 (class 1259 OID 16473)
-- Name: loan_type_id_loan_type_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.loan_type_id_loan_type_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.loan_type_id_loan_type_seq OWNER TO postgres;

--
-- TOC entry 4950 (class 0 OID 0)
-- Dependencies: 222
-- Name: loan_type_id_loan_type_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.loan_type_id_loan_type_seq OWNED BY public.loan_type.id_loan_type;


--
-- TOC entry 227 (class 1259 OID 16500)
-- Name: rol; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.rol (
    id_rol integer NOT NULL,
    name_rol character varying(100) NOT NULL,
    description text
);


ALTER TABLE public.rol OWNER TO postgres;

--
-- TOC entry 226 (class 1259 OID 16499)
-- Name: rol_id_rol_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.rol_id_rol_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.rol_id_rol_seq OWNER TO postgres;

--
-- TOC entry 4951 (class 0 OID 0)
-- Dependencies: 226
-- Name: rol_id_rol_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.rol_id_rol_seq OWNED BY public.rol.id_rol;


--
-- TOC entry 221 (class 1259 OID 16465)
-- Name: states; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.states (
    id_state integer NOT NULL,
    name_state character varying(100) NOT NULL,
    description text
);


ALTER TABLE public.states OWNER TO postgres;

--
-- TOC entry 220 (class 1259 OID 16464)
-- Name: states_id_state_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.states_id_state_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.states_id_state_seq OWNER TO postgres;

--
-- TOC entry 4952 (class 0 OID 0)
-- Dependencies: 220
-- Name: states_id_state_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.states_id_state_seq OWNED BY public.states.id_state;


--
-- TOC entry 219 (class 1259 OID 16412)
-- Name: user; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public."user" (
    id integer NOT NULL,
    first_name character varying(255),
    last_name character varying(255),
    date_of_birth date,
    address character varying(255),
    phone_number character varying(255),
    email character varying(255),
    base_salary numeric(10,2),
    password character varying(255) DEFAULT 'temp_password123'::character varying NOT NULL,
    id_rol integer DEFAULT 3 NOT NULL
);


ALTER TABLE public."user" OWNER TO postgres;

--
-- TOC entry 217 (class 1259 OID 16395)
-- Name: user_entity; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_entity (
    id character varying(255) NOT NULL,
    first_name character varying(255),
    last_name character varying(255),
    date_of_birth date,
    address character varying(255),
    phone_number character varying(255),
    email character varying(255),
    base_salary numeric(10,2)
);


ALTER TABLE public.user_entity OWNER TO postgres;

--
-- TOC entry 218 (class 1259 OID 16411)
-- Name: user_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.user_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.user_id_seq OWNER TO postgres;

--
-- TOC entry 4953 (class 0 OID 0)
-- Dependencies: 218
-- Name: user_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.user_id_seq OWNED BY public."user".id;


--
-- TOC entry 225 (class 1259 OID 16482)
-- Name: users_info; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users_info (
    id_request integer NOT NULL,
    document_type character varying(50) NOT NULL,
    document_number character varying(50) NOT NULL,
    credit_amount numeric(12,2) NOT NULL,
    credit_time integer NOT NULL,
    email character varying(150) NOT NULL,
    id_state integer NOT NULL,
    id_loan_type integer NOT NULL
);


ALTER TABLE public.users_info OWNER TO postgres;

--
-- TOC entry 224 (class 1259 OID 16481)
-- Name: users_info_id_request_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.users_info_id_request_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.users_info_id_request_seq OWNER TO postgres;

--
-- TOC entry 4954 (class 0 OID 0)
-- Dependencies: 224
-- Name: users_info_id_request_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.users_info_id_request_seq OWNED BY public.users_info.id_request;


--
-- TOC entry 4770 (class 2604 OID 16477)
-- Name: loan_type id_loan_type; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.loan_type ALTER COLUMN id_loan_type SET DEFAULT nextval('public.loan_type_id_loan_type_seq'::regclass);


--
-- TOC entry 4773 (class 2604 OID 16503)
-- Name: rol id_rol; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.rol ALTER COLUMN id_rol SET DEFAULT nextval('public.rol_id_rol_seq'::regclass);


--
-- TOC entry 4769 (class 2604 OID 16468)
-- Name: states id_state; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.states ALTER COLUMN id_state SET DEFAULT nextval('public.states_id_state_seq'::regclass);


--
-- TOC entry 4766 (class 2604 OID 16415)
-- Name: user id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."user" ALTER COLUMN id SET DEFAULT nextval('public.user_id_seq'::regclass);


--
-- TOC entry 4772 (class 2604 OID 16485)
-- Name: users_info id_request; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users_info ALTER COLUMN id_request SET DEFAULT nextval('public.users_info_id_request_seq'::regclass);


--
-- TOC entry 4940 (class 0 OID 16474)
-- Dependencies: 223
-- Data for Name: loan_type; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.loan_type (id_loan_type, name_loan, min_amount, max_amount, interest_rate, auto_validation) FROM stdin;
1	Hipotecario	50000000.00	200000000.00	12.00	t
2	Vehiculo	5000000.00	100000000.00	15.00	t
3	Libre inversion	1000000.00	20000000.00	18.00	t
4	Educacion	500000.00	100000000.00	14.00	t
\.


--
-- TOC entry 4944 (class 0 OID 16500)
-- Dependencies: 227
-- Data for Name: rol; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.rol (id_rol, name_rol, description) FROM stdin;
1	ADMIN	administrador: tiene permisos en todas las funcionalidades
2	ASESOR	Asesor: tiene permisos obtener el listado de solicitudes y de enviar solicitudes de credito
3	CLIENTE	Cliente, tiene permisos para ver sus creditos
\.


--
-- TOC entry 4938 (class 0 OID 16465)
-- Dependencies: 221
-- Data for Name: states; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.states (id_state, name_state, description) FROM stdin;
1	Pendiente de revision	Se requiere la validacion por parte de un asesor
2	Aprobado	El credito ha sido aprobado
3	Rechazado	El credito ha sido rechazado
\.


--
-- TOC entry 4936 (class 0 OID 16412)
-- Dependencies: 219
-- Data for Name: user; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public."user" (id, first_name, last_name, date_of_birth, address, phone_number, email, base_salary, password, id_rol) FROM stdin;
1	Laura	Herrera	1997-08-30	Calle 1234	3001234567	lauherrera@gmail.com	10000000.00	temp_password123	3
2	Laura	Rodriguez	1997-08-30	Calle 1234	3001234567	LauRodrigue@gmail.com	10000000.00	temp_password123	3
3	Juan Carlos	García López	1990-05-15	Calle 123 #45-67, Bogotá	+57 300 123 4567	juan.garcia@email.com	2500000.00	temp_password123	3
4	Juan Carlos	García López	1990-05-15	Calle 123 #45-67, Medellin	+57 300 123 4567	juan.garcia@gmail.com	2500000.00	temp_password123	3
5	Laura	Rodriguez	1997-08-30	Calle 1234	3001234567	LauRodrdsdgue@gmail.com	10000000.00	temp_password123	3
6	Juan Carlos	García López	1990-05-15	Calle 123 #45-67, Medellin	+57 300 123 4567	juan.dsd@email.com	2500000.00	temp_password123	3
7	victor	García López	1990-05-15	Calle 123 #45-67, Medellin	+57 300 123 4567	victor.dsd@email.com	2500000.00	temp_password123	3
8	Juan Carlos	García López	1990-05-15	Calle 123 #45-67, Bogotá	+57 300 123 4567	juan.garciasfssd@email.com	2500000.00	Ej3mpl0@	3
9	Juan Carlos	García López	1990-05-15	Calle 123 #45-67, Bogotá	+57 300 123 4567	juan.fdfrrfr@email.com	2500000.00	Ej3mpl0@123	3
10	Laura	Rodriguez	1990-05-15	Calle 123 #45-67, Copacabana	+57 300 123 4567	lauRodriguez@email.com	2500000.00	Ej3mpl0@	3
11	admin	solicitudes	1997-08-30	Calle 1234	3001234567	admin@gmail.com	10000000.00	$2a$12$BNdyyEwOSsrxySu6VNfpt.JUxjtASO4YUFnZqwz2/OO1TfXmeAOBK	1
12	sds	Rodriguez	1997-08-30	Calle 1234	3001234567	dsd@dscscd.com	1000000.00	$2a$10$URkiPHff6VhL7pDQ09c5a.AY2RL8VF.CdDqoSY2.PkjKLSowikQie	3
13	Victor	Escobar	1997-08-30	Calle 1234	3001234567	victor@email.com	1000000.00	$2a$10$4ONEEvJKXqFkfoMxAWebfewNlRMNPJJqKHXYm0H0Ox9dIf78bTLda	3
14	asesor	usuarios	1997-08-30	Calle 1234	3001234567	asesor@gmail.com	10000000.00	$2a$12$Gp/sQwxv0SSL.E5xkelYKeHaLpp0vnusQgpn/NOna/XMQEyhF5u8O	2
15	Victor	Escobar	1997-08-30	Calle 1234 Medellin	3001234567	victor@adminemail.com	1000000.00	$2a$10$3dSabtRLpp5QdTH/fOIPJ.mkxrcz7YSEfp32Iw6f5dfqzmAAeR.EG	1
16	Fernando	Escobar	1997-08-30	Calle 1234 Medellin	3001234567	victorsdwde@adminemail.com	1000000.00	$2a$10$HH9sE62Tybuh6PjmSBizvu7ms0yUfRyKBAm7gMmPaOksivIsK3gK6	3
17	Fernando	Escobar	1997-08-30	Calle 1234 Medellin	3001234567	vi@adminemail.com	1000000.00	$2a$10$lH35OP.xAGDHw5dlIgauHeW8IksWBXnwfVqBXMAhQjMSgi7MyV0Xm	3
18	Juan Carlos	García López	1990-05-15	Calle 123 #45-67, Bogotá	+57 300 123 4567	juan.garcsfdfdia@email.com	2500000.00	$2a$10$4d7QUPscYyEGrJF1IymsyOrU.n.qOswsPvI4hAJounau/RqhqKaHu	3
19	Camilo	García López	1990-05-15	Calle 123 #45-67, Medellin	+57 300 123 4567	prueba2@email.com	3000000.00	$2a$10$7rH/2M1n4OoHBSqE6TlKIu9wy8lCJA4t24M6t4mzWnmITUaXy907C	3
20	Estefania	Ramirez	1990-05-15	Calle 123 #45-67, Medellin	+57 300 123 4567	prueba@email.com	4000000.00	$2a$10$Ozy8NTZuxWSb6BP1Hii.luaaYocKyQAnZJOHTBLgVRd7OYEsOgnt2	3
21	Luis	Montoya	1990-05-15	Calle 123 #45-67, Medellin	+57 300 123 4567	prueba3@email.com	5000000.00	$2a$10$Xcegw//sfN4MtYZhVB/83ONqfidBKSbQ9ldpKpiubsxWt9QFAXAQG	3
22	Victor	Escobar	1990-05-15	Calle 123 #45-67, Medellin	+57 300 123 4567	prueba4@email.com	8000000.00	$2a$10$oJPD9HCcOPhQOt1iQ5ODq.rVuVtISr99pEvVdhqdEv.9z7CxZUpLu	3
24	leidy	Rodriguez	1990-05-15	Calle 123 #45-67, copacabana	+57 300 123 4567	prueba12@email.com	12000000.00	$2a$10$AjrW/8XmWo92ftVovp.KvOrBbVlSO3F3z/nqLLfkTNRwIMZF.hNUi	3
23	laura	Rodriguez	1990-05-15	Calle 123 #45-67, copacabana	+57 300 123 4567	prueba11@gmail.com	10000000.00	$2a$10$drRmf80eeEYs3lQ8zHbtbOxfGxw0.OvjJ/6blN62wRn9fQEYwUB4i	3
25	Mirian	Lopez	1990-05-15	Calle 123 #45-67, Rionegro	+57 300 123 4567	prueba12@gmail.com	12000000.00	$2a$10$IBROT.iSdeT/2RNyo3BtK.SHooDMbgrTuO0xoBP1uiditDF8KM976	3
\.


--
-- TOC entry 4934 (class 0 OID 16395)
-- Dependencies: 217
-- Data for Name: user_entity; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.user_entity (id, first_name, last_name, date_of_birth, address, phone_number, email, base_salary) FROM stdin;
123	Victor	Escobar	1990-01-01	Calle 123	3001234567	victor@example.com	5000000.00
124	Victor	Escobar	1990-01-01	Calle 123	3001234567	victorescobar@example.com	5000000.00
125	Victor	Escobar	1990-01-01	Calle 123	3001234567	victorescobar@cose.com	5000000.00
126	Victor	Escobar	1990-01-01	Calle 123	3001234567	victorescobar@xxxx.com	5000000.00
128	Victor	Escobar	1990-01-01	Calle 123	3001234567	victorescobar@clse.com	5000000.00
130	Victor	Escobar	1990-01-01	Calle 123	3001234567	victorescobar@eeeeclse.com	5000000.00
131	Victor	Escobar	1990-01-01	Calle 123	3001234567	victorescobar@gggclse.com	6000000.00
132	Victor	Escobar	1990-01-01	Calle 123	3001234567	victorescobar@hhhhclse.com	6000000.00
133	Victor	Escobar	1990-01-01	Calle 123	3001234567	victorescobar@iiiiiclse.com	6000000.00
134	Victor	Escobar	1990-01-01	Calle 123	3001234567	victorescobar@jjjjjclse.com	6000000.00
135	Victor	Escobar	1990-01-01	Calle 123	3001234567	victorescobar@kkkclse.com	6000000.00
136	Victor	Escobar	1990-01-01	Calle 123	3001234567	victorescobar@kkkkkclse.com	6000000.00
137	Victor	Escobar	1990-01-01	Calle 123	3001234567	victorescobar@llllclse.com	6000000.00
139	carlos	Ruiz	1990-01-01	Calle 123	3001234567	carlos@llllclse.com	6000000.00
140	carlos	Ruiz	1990-01-01	Calle 123	3001234567	carlos@llllcfddlse.com	6000000.00
141	Laura	Herrera	1997-08-30	Calle 1234	3001234567	lauherrera@gmail.com	10000000.00
\.


--
-- TOC entry 4942 (class 0 OID 16482)
-- Dependencies: 225
-- Data for Name: users_info; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.users_info (id_request, document_type, document_number, credit_amount, credit_time, email, id_state, id_loan_type) FROM stdin;
1	CC	111222	10000000.00	12	prueba@email.com	1	1
2	CC	111333	10000000.00	12	prueba2@email.com	1	2
3	CC	111444	8000000.00	12	prueba3@email.com	2	3
4	CC	111555	8000000.00	12	prueba4@email.com	3	4
5	CC	11121314	10000000.00	60	prueba11@gmail.com	1	3
6	CC	11121314	10000000.00	60	prueba11@gmail.com	1	2
7	CC	11121314	20000000.00	60	prueba11@gmail.com	1	4
8	CC	11121315	20000000.00	60	prueba12@gmail.com	1	1
9	CC	11121315	20000000.00	60	prueba12@email.com	1	1
10	CC	11121315	30000000.00	60	prueba12@email.com	1	1
11	CC	11121316	20000000.00	24	lauherrera@gmail.com	1	3
12	CC	11121317	10000000.00	12	juan.garcia@email.com	1	4
13	CC	11121318	10000000.00	24	victorsdwde@adminemail.com	1	2
\.


--
-- TOC entry 4955 (class 0 OID 0)
-- Dependencies: 222
-- Name: loan_type_id_loan_type_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.loan_type_id_loan_type_seq', 4, true);


--
-- TOC entry 4956 (class 0 OID 0)
-- Dependencies: 226
-- Name: rol_id_rol_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.rol_id_rol_seq', 3, true);


--
-- TOC entry 4957 (class 0 OID 0)
-- Dependencies: 220
-- Name: states_id_state_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.states_id_state_seq', 3, true);


--
-- TOC entry 4958 (class 0 OID 0)
-- Dependencies: 218
-- Name: user_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.user_id_seq', 25, true);


--
-- TOC entry 4959 (class 0 OID 0)
-- Dependencies: 224
-- Name: users_info_id_request_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.users_info_id_request_seq', 13, true);


--
-- TOC entry 4781 (class 2606 OID 16480)
-- Name: loan_type loan_type_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.loan_type
    ADD CONSTRAINT loan_type_pkey PRIMARY KEY (id_loan_type);


--
-- TOC entry 4785 (class 2606 OID 16507)
-- Name: rol rol_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.rol
    ADD CONSTRAINT rol_pkey PRIMARY KEY (id_rol);


--
-- TOC entry 4779 (class 2606 OID 16472)
-- Name: states states_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.states
    ADD CONSTRAINT states_pkey PRIMARY KEY (id_state);


--
-- TOC entry 4775 (class 2606 OID 16401)
-- Name: user_entity user_entity_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_entity
    ADD CONSTRAINT user_entity_pkey PRIMARY KEY (id);


--
-- TOC entry 4777 (class 2606 OID 16419)
-- Name: user user_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."user"
    ADD CONSTRAINT user_pkey PRIMARY KEY (id);


--
-- TOC entry 4783 (class 2606 OID 16487)
-- Name: users_info users_info_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users_info
    ADD CONSTRAINT users_info_pkey PRIMARY KEY (id_request);


--
-- TOC entry 4787 (class 2606 OID 16493)
-- Name: users_info fk_loan_type; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users_info
    ADD CONSTRAINT fk_loan_type FOREIGN KEY (id_loan_type) REFERENCES public.loan_type(id_loan_type);


--
-- TOC entry 4786 (class 2606 OID 16508)
-- Name: user fk_rol; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."user"
    ADD CONSTRAINT fk_rol FOREIGN KEY (id_rol) REFERENCES public.rol(id_rol);


--
-- TOC entry 4788 (class 2606 OID 16488)
-- Name: users_info fk_state; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users_info
    ADD CONSTRAINT fk_state FOREIGN KEY (id_state) REFERENCES public.states(id_state);


-- Completed on 2025-09-08 20:47:30

--
-- PostgreSQL database dump complete
--

