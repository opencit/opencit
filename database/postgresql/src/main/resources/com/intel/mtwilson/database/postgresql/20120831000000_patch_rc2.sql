-- created 2012-08-31

-- fix bug #266
-- New constraints
ALTER TABLE tbl_mle ADD CONSTRAINT MLE_OEM_FK FOREIGN KEY (OEM_ID) REFERENCES tbl_oem (ID);
ALTER TABLE tbl_mle ADD CONSTRAINT MLE_OS_FK FOREIGN KEY (OS_ID) REFERENCES tbl_os (ID);

-- plaintext values of AddOn_Connection_Info were 80 bytes in previous release. Now they are encrypted so we have to make it larger to allow for padding, IV, and base-64 encoding.
ALTER TABLE tbl_hosts 
	ALTER COLUMN AddOn_Connection_Info TYPE text,
	ALTER COLUMN AddOn_Connection_Info SET DEFAULT NULL;

-- Module manifest log supports the failure report feature
CREATE SEQUENCE tbl_module_manifest_log_serial;
CREATE TABLE tbl_module_manifest_log (
  ID integer NOT NULL DEFAULT nextval('tbl_module_manifest_log_serial'),
  ta_log_id integer NOT NULL,
  name varchar(100) DEFAULT NULL,
  value varchar(100) DEFAULT NULL,
  whitelist_value varchar(100) DEFAULT NULL,
  PRIMARY KEY (ID),
  CONSTRAINT TA_LOG_FK FOREIGN KEY (ta_log_id) REFERENCES tbl_ta_log (ID)
);

INSERT INTO changelog (ID, APPLIED_AT, DESCRIPTION) VALUES (20120831000000,NOW(),'patch rc2');
