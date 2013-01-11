
-- first thing we create is the changelog
CREATE TABLE changelog (
  ID decimal(20,0) NOT NULL,
  APPLIED_AT timestamp NOT NULL,
  DESCRIPTION varchar(255) NOT NULL,
  PRIMARY KEY (ID)
);

--INSERT INTO changelog (ID, APPLIED_AT, DESCRIPTION) VALUES (to_number( to_char(current_timestamp, 'YYYYMMDDHH24MISS'), '99999999999999'),NOW(),'create changelog');

-- backdate an entry for the bootstrap sql file since the changelog didn't exist then;   TODO move the changelog into the bootstrap sql or move the content of the bootstrap sql into a create-api-client sql AFTER create-changelog and delete the bootstrap.sql
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


-- this entry corresponds to Admin.jks which is included with the Management Console installer
--INSERT INTO `api_client_x509` (`name`,`certificate`,`fingerprint`,`issuer`,`serial_number`,`expires`,`enabled`,`status`,`comment`) VALUES ('CN=Admin',UNHEX('3082019C30820105A003020102020900B3A72C6478FCC667300D06092A864886F70D01010B05003010310E300C0603550403130541646D696E301E170D3132303732323134313930365A170D3133303732323134313930365A3010310E300C0603550403130541646D696E30819F300D06092A864886F70D010101050003818D003081890281810096EB017AA68235F3C8CC934BD7CAE949C03F99DC265969AD7CA83C06520052778EF820A2EFCB929E8780C2B659657FF6B46FC6E2EB5E4B4FBA8D96E26F1CA195F67C62DED0F859752C387A4810FA1BABAF3DFEA321804B881695DC7B622432506CCD0B9F8A389A10D4C2AFBEA6690F6341706D8A7404B88284D932F6916BD2170203010001300D06092A864886F70D01010B0500038181005C6BB1BB7380E1AFF20A0D33A12FB3E8E4EA6E4708BD1DD8D2BCDCDB71811997F524750CA3537FEE215780ACBAE25D4EB903E903DF0BF19E49EFDBEBF36192B74E3FC59136789D731CA978605273E905393140C9C7D8C246B74AF63C9B3321F0AB3B9C82CBAEDED470C1EDFCB9E4C205A23CF388B77966A59D5ED1B5E5429BB1'),UNHEX('4D50C190064672B9B92425300639C262EABAABEDCBD27A9B8503247973F700BD'),'CN=Admin',2029831783,'2013-07-22 07:19:06',b'1','APPROVED','DEFAULT ADMINISTRATIVE USER');
--INSERT INTO `api_role_x509` (`api_client_x509_ID`,`role`) VALUES (LAST_INSERT_ID(),'Security');

