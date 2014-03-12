-- created 2014-02-13


-- example insert: insert into mw_role  (id,role_name,description) values ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11','test_role','just for testing');
CREATE TABLE mw_role (
  id binary(16) NOT NULL,
  role_name character varying(200) NOT NULL,
  description text DEFAULT NULL,
  PRIMARY KEY (id)
); 

CREATE TABLE mw_role_permission (
  role_id binary(16) NOT NULL,
  permit_domain varchar(200) DEFAULT NULL,
  permit_action varchar(200) DEFAULT NULL,
  permit_selection varchar(200) DEFAULT NULL,
  PRIMARY KEY (role_id)
); 

-- replaces mw_portal_user 
CREATE TABLE mw_user (
  id binary(16) NOT NULL,
  username varchar(255) NOT NULL,
  locale varchar(8) NULL,
  enabled bit(1) NOT NULL DEFAULT b'0',
  status varchar(128) NOT NULL DEFAULT 'Pending',
  comment text DEFAULT NULL,
  PRIMARY KEY (id)
); 

CREATE TABLE mw_user_keystore (
  id binary(16) NOT NULL,
  user_id binary(16) NOT NULL,
  keystore blob NOT NULL,
  keystore_format varchar(128) NOT NULL DEFAULT 'jks',
  comment text DEFAULT NULL,
  PRIMARY KEY (id)
); 

-- expires may be replaced with notAfter and notBefore
CREATE TABLE mw_user_login_password (
  id binary(16) DEFAULT NULL,
  user_id binary(16) DEFAULT NULL,
  password_hash blob NOT NULL,
  salt blob NOT NULL,
  iterations int(11) DEFAULT 1,
  algorithm varchar(128) NOT NULL,
  expires timestamp DEFAULT NULL,
  enabled bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (id)
); 

CREATE TABLE mw_user_login_password_role (
  login_password_id binary(16) NOT NULL,
  role_id binary(16) NOT NULL
); 


-- expires may be replaced with notAfter and notBefore
-- protection is the algorithm name, mode, and padding used to encrypt the hmac_key
-- for storage in the hmac_key field ;  the protection key id itself might be an
-- implied global key, or we may need to add a field protection_key_id to indicate
-- which wrapping key was used to protect the hmac_key
CREATE TABLE mw_user_login_hmac (
  id binary(16) DEFAULT NULL,
  user_id binary(16) DEFAULT NULL,
  hmac_key blob NOT NULL,
  protection varchar(128) NOT NULL,
  expires timestamp DEFAULT NULL,
  enabled bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (id)
); 

CREATE TABLE mw_user_login_hmac_role (
  login_hmac_id binary(16) NOT NULL,
  role_id binary(16) NOT NULL
); 

-- expires may be replaced with notAfter and notBefore
CREATE TABLE mw_user_login_certificate (
  id binary(16) DEFAULT NULL,
  user_id binary(16) DEFAULT NULL,
  certificate blob NOT NULL,
  sha1_hash binary(16) NOT NULL,
  sha256_hash binary(32) NOT NULL,
  expires timestamp DEFAULT NULL,
  enabled bit(1) NOT NULL DEFAULT b'0',
  status varchar(128) NOT NULL DEFAULT 'Pending',
  comment text,
  PRIMARY KEY (id)
); 

CREATE TABLE mw_user_login_certificate_role (
  login_certificate_id binary(16) NOT NULL,
  role_id binary(16) NOT NULL
); 

INSERT INTO mw_changelog (ID, APPLIED_AT, DESCRIPTION) VALUES (20140213235800,NOW(),'Mt Wilson 2.0 - added permission table');
