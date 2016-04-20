-- created 2013-08-14

-- This script creates the table to store the asset tag certificates. This would be initially populated
-- by the Tag Provisioning service. During host registration if an entry exists for the host (based on UUID),
-- then the mapping would be added. 

CREATE SEQUENCE mw_asset_tag_certificate_serial;
CREATE TABLE mw_asset_tag_certificate (
  ID integer NOT NULL DEFAULT nextval('mw_processor_mapping_serial') ,
  Host_ID integer DEFAULT NULL,
  UUID character varying(100) DEFAULT NULL,
  Certificate bytea NOT NULL,
  SHA1_Hash bytea DEFAULT NULL,
  PCREvent bytea DEFAULT NULL,
  Revoked boolean DEFAULT NULL,
  NotBefore timestamp without time zone DEFAULT NULL,
  NotAfter timestamp without time zone DEFAULT NULL,
  PRIMARY KEY (ID),
  CONSTRAINT Host_ID FOREIGN KEY (Host_ID) REFERENCES mw_hosts (ID) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION); 

INSERT INTO mw_changelog (ID, APPLIED_AT, DESCRIPTION) VALUES (20130814154300,NOW(),'Patch for creating the Asset Tag certificate table.');