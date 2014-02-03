-- created 2012-08-29
CREATE SEQUENCE audit_log_entry_serial;
CREATE TABLE audit_log_entry (
  ID integer NOT NULL DEFAULT nextval('audit_log_entry_serial'),
  transaction_id varchar(50) NULL,
  entity_id integer NULL,
  entity_type varchar(150) NULL,
  finger_print varchar(200) NULL,
  create_dt timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  action varchar(50) NULL,
  data text NULL,
  PRIMARY KEY (ID)
);


INSERT INTO changelog (ID, APPLIED_AT, DESCRIPTION) VALUES (20120829000000,NOW(),'audit_log');
