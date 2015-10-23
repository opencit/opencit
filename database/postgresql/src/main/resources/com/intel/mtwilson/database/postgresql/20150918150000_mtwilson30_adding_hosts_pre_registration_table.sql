-- created 2015-09-18

-- This script creates the table to store the pre-registration information for the host (username & password for hosts running Trust Agent)

CREATE  TABLE mw_host_pre_registration_details (
  id CHAR(36) NOT NULL,
  name VARCHAR(255) NOT NULL ,
  login VARCHAR(255) NOT NULL ,
  password TEXT DEFAULT NULL ,
  created_ts timestamp DEFAULT now(),  
  PRIMARY KEY (id)
  );
    
INSERT INTO mw_changelog (ID, APPLIED_AT, DESCRIPTION) VALUES (20150918150000, NOW(), 'Patch for creating the table to store host pre-registration details.');