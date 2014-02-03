CREATE TABLE changelog (
  ID numeric(20,0) NOT NULL,
  APPLIED_AT varchar(25) NOT NULL,
  DESCRIPTION varchar(255) NOT NULL,
  PRIMARY KEY (ID)
);

INSERT INTO changelog (ID, APPLIED_AT, DESCRIPTION) VALUES (20120327214603,NOW(),'create changelog');
