
-- first thing we create is the changelog
CREATE TABLE changelog (
  ID decimal(20,0) NOT NULL,
  APPLIED_AT timestamp NOT NULL,
  DESCRIPTION varchar(255) NOT NULL,
  PRIMARY KEY (ID)
);

--INSERT INTO changelog (ID, APPLIED_AT, DESCRIPTION) VALUES (to_number( to_char(current_timestamp, 'YYYYMMDDHH24MISS'), '99999999999999'),NOW(),'create changelog');

-- backdate an entry for the bootstrap sql file since the changelog didn't exist then;  
INSERT INTO changelog (ID, APPLIED_AT, DESCRIPTION) VALUES (20120101000000,NOW(),'bootstrap');

-- Spring's ResourceDatabasePopulator does not recognize the "delimiter" command
-- and requires the delimiter to be semicolon
--delimiter ;

-- should move to its own file, it's not really a bootstrap item
CREATE SEQUENCE api_client_x509_serial;

CREATE TABLE api_client_x509 (
  ID integer NOT NULL DEFAULT nextval('api_client_x509_serial'),
  name varchar(255) NOT NULL,
  certificate bytea NOT NULL,
  fingerprint bytea NOT NULL,
  issuer varchar(255) DEFAULT NULL,
  serial_number integer DEFAULT NULL,
  expires timestamp DEFAULT NULL,
  enabled boolean NOT NULL DEFAULT '0',
  status varchar(128) NOT NULL DEFAULT 'Pending',
  comment text,
  PRIMARY KEY (ID),
  CONSTRAINT fingerprint_index UNIQUE(fingerprint)
);

CREATE TABLE api_role_x509 (
  api_client_x509_ID integer NOT NULL,
  role varchar(255) NOT NULL,
  PRIMARY KEY (api_client_x509_ID,role),
  CONSTRAINT api_role_x509_ibfk_1 FOREIGN KEY (api_client_x509_ID) REFERENCES api_client_x509 (ID)
);

