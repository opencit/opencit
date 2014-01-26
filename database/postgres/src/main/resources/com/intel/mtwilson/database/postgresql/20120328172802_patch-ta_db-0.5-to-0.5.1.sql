-- created 2012-03-28

CREATE TABLE changelog (
  ID decimal(20,0) NOT NULL,
  APPLIED_AT varchar(25) NOT NULL,
  DESCRIPTION varchar(255) NOT NULL,
  PRIMARY KEY (ID)
);
INSERT INTO changelog (ID, APPLIED_AT, DESCRIPTION) VALUES (20120328172802,NOW(),'update schema from 0.5 to 0.5.1');

CREATE TABLE tbl_api_client (
  Client_ID varchar(128) NOT NULL,
  Secret_Key varchar(248) NOT NULL,
  PRIMARY KEY (Client_ID)
);

CREATE SEQUENCE tbl_event_type_serial;
CREATE TABLE tbl_event_type (
  ID integer NOT NULL DEFAULT nextval('tbl_event_type_serial'),
  Name varchar(75) NOT NULL,
  FieldName varchar(45) NOT NULL,
  PRIMARY KEY (ID)
);

CREATE SEQUENCE tbl_host_specific_manifest_serial;
CREATE TABLE tbl_host_specific_manifest (
  ID integer NOT NULL DEFAULT nextval('tbl_host_specific_manifest_serial'),
  Module_Manifest_ID integer NOT NULL,
  Host_ID integer NOT NULL,
  DigestValue varchar(100) NOT NULL,
  PRIMARY KEY (ID),
  CONSTRAINT Module_Manifest_ID FOREIGN KEY (Module_Manifest_ID) REFERENCES tbl_module_manifest (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);


CREATE SEQUENCE tbl_package_namespace_serial;
CREATE TABLE tbl_package_namespace (
  ID integer NOT NULL DEFAULT nextval('tbl_package_namespace_serial'),
  Name varchar(45) NOT NULL,
  VendorName varchar(45) NOT NULL,
  PRIMARY KEY (ID)
);

