-- created 2013-08-14

-- This script creates the table to store the asset tag certificates. This would be initially populated
-- by the Tag Provisioning service. During host registration if an entry exists for the host (based on UUID),
-- then the mapping would be added. 

CREATE TABLE mw_rpc (
  ID char(36) DEFAULT NULL,
  Name character varying(200) DEFAULT NULL,
  Input bytea DEFAULT NULL,
  Output bytea DEFAULT NULL,
  Status character varying(200) DEFAULT NULL,
  ProgressCurrent integer DEFAULT NULL,
  ProgressMax integer DEFAULT NULL,
  PRIMARY KEY (ID)
); 

INSERT INTO mw_changelog (ID, APPLIED_AT, DESCRIPTION) VALUES (20140201124500,NOW(),'Mt Wilson 2.0 - added RPC table');
