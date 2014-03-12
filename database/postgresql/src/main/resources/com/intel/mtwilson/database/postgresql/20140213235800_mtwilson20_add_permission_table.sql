-- created 2014-02-13


-- example insert: insert into mw_role  (id,role_name,description) values ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11','test_role','just for testing');
CREATE TABLE mw_role (
  id uuid NOT NULL,
  role_name character varying(200) NOT NULL,
  description text DEFAULT NULL,
  PRIMARY KEY (id)
); 

CREATE TABLE mw_role_permission (
  role_id uuid NOT NULL,
  permit_domain character varying(200) DEFAULT NULL,
  permit_action character varying(200) DEFAULT NULL,
  permit_selection character varying(200) DEFAULT NULL,
  PRIMARY KEY (role_id)
); 

-- replaces mw_portal_user 
CREATE TABLE mw_user (
  id uuid NOT NULL,
  username character varying(255) NOT NULL,
  locale character varying(8) NULL,
  enabled boolean NOT NULL DEFAULT '0',
  status varchar(128) NOT NULL DEFAULT 'Pending',
  comment text DEFAULT NULL,
  PRIMARY KEY (id)
); 

CREATE TABLE mw_user_keystore (
  id uuid NOT NULL,
  user_id uuid NOT NULL,
  keystore bytea NOT NULL,
  keystore_format character varying(128) NOT NULL DEFAULT 'jks',
  comment text DEFAULT NULL,
  PRIMARY KEY (id)
); 

-- expires may be replaced with notAfter and notBefore
CREATE TABLE mw_user_login_password (
  id uuid DEFAULT NULL,
  user_id uuid DEFAULT NULL,
  password_hash bytea NOT NULL,
  salt bytea NOT NULL,
  iterations integer DEFAULT 1,
  algorithm character varying(128) NOT NULL,
  expires timestamp DEFAULT NULL,
  enabled boolean NOT NULL DEFAULT '0',
  PRIMARY KEY (id)
); 

CREATE TABLE mw_user_login_password_role (
  login_password_id uuid NOT NULL,
  role_id uuid NOT NULL
); 


-- expires may be replaced with notAfter and notBefore
-- protection is the algorithm name, mode, and padding used to encrypt the hmac_key
-- for storage in the hmac_key field ;  the protection key id itself might be an
-- implied global key, or we may need to add a field protection_key_id to indicate
-- which wrapping key was used to protect the hmac_key
CREATE TABLE mw_user_login_hmac (
  id uuid DEFAULT NULL,
  user_id uuid DEFAULT NULL,
  hmac_key bytea NOT NULL,
  protection character varying(128) NOT NULL,
  expires timestamp DEFAULT NULL,
  enabled boolean NOT NULL DEFAULT '0',
  PRIMARY KEY (id)
); 

CREATE TABLE mw_user_login_hmac_role (
  login_hmac_id uuid NOT NULL,
  role_id uuid NOT NULL
); 

-- expires may be replaced with notAfter and notBefore
CREATE TABLE mw_user_login_certificate (
  id uuid DEFAULT NULL,
  user_id uuid DEFAULT NULL,
  certificate bytea NOT NULL,
  sha1_hash bytea NOT NULL,
  sha256_hash bytea NOT NULL,
  expires timestamp DEFAULT NULL,
  enabled boolean NOT NULL DEFAULT '0',
  status varchar(128) NOT NULL DEFAULT 'Pending',
  comment text,
  PRIMARY KEY (id)
); 

CREATE TABLE mw_user_login_certificate_role (
  login_certificate_id uuid NOT NULL,
  role_id uuid NOT NULL
); 

INSERT INTO mw_changelog (ID, APPLIED_AT, DESCRIPTION) VALUES (20140213235800,NOW(),'Mt Wilson 2.0 - added permission table');
