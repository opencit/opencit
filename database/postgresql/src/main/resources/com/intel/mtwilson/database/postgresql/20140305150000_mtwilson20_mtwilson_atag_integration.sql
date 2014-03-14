-- created 2014-03-05

-- This script creates the tables required for integrating asset tag with mt wilson

CREATE  TABLE mw_host_tpm_password (
  id UUID NOT NULL,
  password VARCHAR(255) NOT NULL ,
  PRIMARY KEY (id) );
  
CREATE  TABLE mw_tag_kvattribute (
  id UUID NOT NULL,
  name VARCHAR(255) NOT NULL ,
  value VARCHAR(255) NOT NULL ,
  PRIMARY KEY (id) );
  
CREATE  TABLE mw_tag_selection (
  id UUID NOT NULL,
  name VARCHAR(255) NOT NULL ,
  description TEXT NULL,
  PRIMARY KEY (id) );
  
CREATE  TABLE mw_tag_selection_kvattribute (
  id UUID NOT NULL,
  selectionId UUID NOT NULL ,
  kvAttributeId UUID NOT NULL ,
  PRIMARY KEY (id) );

CREATE  TABLE mw_tag_certificate (
  id UUID NOT NULL,
  certificate BYTEA NOT NULL ,
  sha1 CHAR(40) NOT NULL ,
  sha256 CHAR(64) NOT NULL ,
  subject VARCHAR(255) NOT NULL ,
  issuer VARCHAR(255) NOT NULL ,
  notBefore timestamp without time zone NOT NULL ,
  notAfter timestamp without time zone NOT NULL ,
  revoked BOOLEAN NOT NULL DEFAULT FALSE ,
  PRIMARY KEY (id) );
  
CREATE  TABLE mw_tag_certificate_request (
  id UUID NOT NULL,
  subject VARCHAR(255) NOT NULL ,
  selectionId UUID NOT NULL ,
  certificateId UUID NULL , 
  status VARCHAR(255) NOT NULL ,
  authorityName VARCHAR(255) NULL ,
  PRIMARY KEY (id) );
  
CREATE  TABLE mw_tag_configuration (
  id UUID NOT NULL,
  name VARCHAR(255) NOT NULL ,
  content BYTEA DEFAULT NULL ,
  PRIMARY KEY (id) );
  
INSERT INTO mw_changelog (ID, APPLIED_AT, DESCRIPTION) VALUES (20140305150000,NOW(),'Patch for creating the tables for migrating asset tag to mtwilson database.');