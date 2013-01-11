
CREATE SEQUENCE tbl_saml_assertion_serial;
CREATE TABLE tbl_saml_assertion (
  ID integer NOT NULL DEFAULT nextval('tbl_saml_assertion_serial'),
  host_id integer NOT NULL,
  saml text,
  expiry_ts timestamp NOT NULL,
  bios_trust boolean NOT NULL,
  vmm_trust boolean NOT NULL,
  error_code varchar(50) DEFAULT NULL,
  error_message varchar(200) DEFAULT NULL,
  created_ts timestamp DEFAULT NULL,
  PRIMARY KEY (ID),
  CONSTRAINT tbl_hosts_fk FOREIGN KEY (host_id) REFERENCES tbl_hosts (ID)
) ;

INSERT INTO changelog (ID, APPLIED_AT, DESCRIPTION) VALUES (20120710100000,NOW(),'saml cache patch 0.5 to 0.5.2');
