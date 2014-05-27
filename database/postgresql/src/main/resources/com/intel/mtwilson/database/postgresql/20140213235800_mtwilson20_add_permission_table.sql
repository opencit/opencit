-- created 2014-02-13


-- example insert: insert into mw_role  (id,role_name,description) values ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11','test_role','just for testing');
CREATE TABLE mw_role (
  id CHAR(36) NOT NULL,
  role_name character varying(200) NOT NULL,
  description text DEFAULT NULL,
  PRIMARY KEY (id)
); 

-- describes which permissions are granted to users having a role
-- this is a many-to-many table; there is no primary key but each record must be unique (see constraint)
CREATE TABLE mw_role_permission (
  role_id CHAR(36) NOT NULL,
  permit_domain character varying(200) DEFAULT NULL,
  permit_action character varying(200) DEFAULT NULL,
  permit_selection character varying(200) DEFAULT NULL
); 
ALTER TABLE mw_role_permission ADD CONSTRAINT mw_role_permission_ukey UNIQUE (role_id,permit_domain,permit_action,permit_selection);

-- describes available permissions and features they came from
-- this is a many-to-many table; there is no primary key but each record must be unique (see constraint)
CREATE TABLE mw_feature_permission (
  feature_id CHAR(36) NOT NULL,
  feature_name character varying(200) NOT NULL,
  permit_domain character varying(200) DEFAULT NULL,
  permit_action character varying(200) DEFAULT NULL,
  permit_selection character varying(200) DEFAULT NULL,
  comment text DEFAULT NULL
); 
ALTER TABLE mw_feature_permission ADD CONSTRAINT mw_feature_permission_ukey UNIQUE (feature_id,feature_name,permit_domain,permit_action,permit_selection);

-- replaces mw_portal_user 
CREATE TABLE mw_user (
  id CHAR(36) NOT NULL,
  username character varying(255) NOT NULL,
  locale character varying(8) NULL,
  comment text DEFAULT NULL,
  PRIMARY KEY (id)
); 

CREATE TABLE mw_user_keystore (
  id CHAR(36) NOT NULL,
  user_id CHAR(36) NOT NULL,
  keystore bytea NOT NULL,
  keystore_format character varying(128) NOT NULL DEFAULT 'jks',
  comment text DEFAULT NULL,
  PRIMARY KEY (id)
); 

-- expires may be replaced with notAfter and notBefore
CREATE TABLE mw_user_login_password (
  id CHAR(36) DEFAULT NULL,
  user_id CHAR(36) DEFAULT NULL,
  password_hash bytea NOT NULL,
  salt bytea NOT NULL,
  iterations integer DEFAULT 1,
  algorithm character varying(128) NOT NULL,
  expires timestamp DEFAULT NULL,
  enabled boolean NOT NULL DEFAULT '0',
  status varchar(128) NOT NULL DEFAULT 'PENDING',
  comment text,
  PRIMARY KEY (id)
); 

CREATE TABLE mw_user_login_password_role (
  login_password_id CHAR(36) NOT NULL,
  role_id CHAR(36) NOT NULL
); 


-- expires may be replaced with notAfter and notBefore
-- protection is the algorithm name, mode, and padding used to encrypt the hmac_key
-- for storage in the hmac_key field ;  the protection key id itself might be an
-- implied global key, or we may need to add a field protection_key_id to indicate
-- which wrapping key was used to protect the hmac_key
CREATE TABLE mw_user_login_hmac (
  id CHAR(36) DEFAULT NULL,
  user_id CHAR(36) DEFAULT NULL,
  hmac_key bytea NOT NULL,
  protection character varying(128) NOT NULL,
  expires timestamp DEFAULT NULL,
  enabled boolean NOT NULL DEFAULT '0',
  status varchar(128) NOT NULL DEFAULT 'PENDING',
  comment text,
  PRIMARY KEY (id)
); 

CREATE TABLE mw_user_login_hmac_role (
  login_hmac_id CHAR(36) NOT NULL,
  role_id CHAR(36) NOT NULL
); 

-- expires may be replaced with notAfter and notBefore
CREATE TABLE mw_user_login_certificate (
  id CHAR(36) DEFAULT NULL,
  user_id CHAR(36) DEFAULT NULL,
  certificate bytea NOT NULL,
  sha1_hash bytea NOT NULL,
  sha256_hash bytea NOT NULL,
  expires timestamp DEFAULT NULL,
  enabled boolean NOT NULL DEFAULT '0',
  status varchar(128) NOT NULL DEFAULT 'PENDING',
  comment text,
  PRIMARY KEY (id)
); 

CREATE TABLE mw_user_login_certificate_role (
  login_certificate_id CHAR(36) NOT NULL,
  role_id CHAR(36) NOT NULL
); 

INSERT INTO mw_changelog (ID, APPLIED_AT, DESCRIPTION) VALUES (20140213235800,NOW(),'Mt Wilson 2.0 - added permission table');
