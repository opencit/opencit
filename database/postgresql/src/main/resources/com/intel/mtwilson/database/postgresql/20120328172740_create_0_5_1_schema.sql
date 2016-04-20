
INSERT INTO changelog (ID, APPLIED_AT, DESCRIPTION) VALUES (20120328172740,NOW(),'create 0.5.1 schema');


/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE tbl_api_client (
  Client_ID varchar(128) NOT NULL,
  Secret_Key varchar(248) NOT NULL,
  PRIMARY KEY (Client_ID)
);

/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE SEQUENCE tbl_db_portal_user_serial;
CREATE TABLE tbl_db_portal_user (
  ID integer NOT NULL DEFAULT nextval('tbl_db_portal_user_serial'),
  Login varchar(15) NOT NULL,
  Password varchar(15) NOT NULL,
  First_Name varchar(15) NOT NULL,
  Last_Name varchar(25) NOT NULL,
  PRIMARY KEY (ID)
);

/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE SEQUENCE tbl_event_type_serial;
CREATE TABLE tbl_event_type (
  ID integer NOT NULL DEFAULT nextval('tbl_event_type_serial'),
  Name varchar(75) NOT NULL,
  FieldName varchar(45) NOT NULL,
  PRIMARY KEY (ID)
);

/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE SEQUENCE tbl_location_pcr_serial;
CREATE TABLE tbl_location_pcr (
  ID integer NOT NULL DEFAULT nextval('tbl_location_pcr_serial'),
  location varchar(200) NOT NULL,
  pcr_value varchar(100) NOT NULL,
  PRIMARY KEY (ID)
); COMMENT ON TABLE tbl_location_pcr IS 'This table contains the mapping between the pcr values and location';


/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE SEQUENCE tbl_mle_serial;
CREATE TABLE tbl_mle (
  ID integer NOT NULL DEFAULT nextval('tbl_mle_serial'),
  Name varchar(100) NOT NULL,
  Version varchar(100) NOT NULL,
  Attestation_Type varchar(20) NOT NULL DEFAULT 'PCR',
  MLE_Type varchar(20) NOT NULL DEFAULT 'VMM',
  Required_Manifest_List varchar(100) NOT NULL,
  Description varchar(100) DEFAULT NULL,
  OS_ID integer DEFAULT NULL,
  OEM_ID integer DEFAULT NULL,
  PRIMARY KEY (ID)
);

/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE SEQUENCE tbl_hosts_serial;
CREATE TABLE tbl_hosts (
  ID integer NOT NULL DEFAULT nextval('tbl_hosts_serial'),
  BIOS_MLE_ID integer NOT NULL,
  VMM_MLE_ID integer NOT NULL,
  Name varchar(40) NOT NULL,
  IPAddress varchar(20) DEFAULT NULL,
  Port integer NOT NULL,
  Description varchar(100) DEFAULT NULL,
  AddOn_Connection_Info varchar(80) DEFAULT NULL,
  AIK_Certificate text DEFAULT NULL,
  Email varchar(45) DEFAULT NULL,
  Error_Code integer DEFAULT NULL,
  Error_Description varchar(100) DEFAULT NULL,
  Location varchar(200) DEFAULT NULL,
  PRIMARY KEY (ID),
  CONSTRAINT BIOS_MLE_ID FOREIGN KEY (BIOS_MLE_ID) REFERENCES tbl_mle (ID) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT VMM_MLE_ID FOREIGN KEY (VMM_MLE_ID) REFERENCES tbl_mle (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);

/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE SEQUENCE tbl_oem_serial;
CREATE TABLE tbl_oem (
  ID integer NOT NULL DEFAULT nextval('tbl_oem_serial'),
  NAME varchar(100) UNIQUE DEFAULT NULL,
  DESCRIPTION varchar(200) DEFAULT NULL,
  PRIMARY KEY (ID)
);

/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE SEQUENCE tbl_os_serial;
CREATE TABLE tbl_os (
  ID integer NOT NULL DEFAULT nextval('tbl_os_serial'),
  NAME varchar(100) NOT NULL,
  VERSION varchar(50) NOT NULL,
  DESCRIPTION varchar(200) DEFAULT NULL,
  PRIMARY KEY (ID),
  CONSTRAINT tbl_os_name_version UNIQUE(NAME,VERSION)
);

/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE SEQUENCE tbl_package_namespace_serial;
CREATE TABLE tbl_package_namespace (
  ID integer NOT NULL DEFAULT nextval('tbl_package_namespace_serial'),
  Name varchar(45) NOT NULL,
  VendorName varchar(45) NOT NULL,
  PRIMARY KEY (ID)
);

/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE SEQUENCE tbl_pcr_manifest_serial;
CREATE TABLE tbl_pcr_manifest (
  ID integer NOT NULL DEFAULT nextval('tbl_pcr_manifest_serial'),
  MLE_ID integer NOT NULL,
  Name varchar(20) NOT NULL,
  Value varchar(100) NOT NULL,

  PCR_Description varchar(100) DEFAULT NULL,
  PRIMARY KEY (ID),
  
  CONSTRAINT PCR_MLE_ID FOREIGN KEY (MLE_ID) REFERENCES tbl_mle (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);

/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE SEQUENCE tbl_request_queue_serial;
CREATE TABLE tbl_request_queue (
  ID integer NOT NULL DEFAULT nextval('tbl_request_queue_serial'),
  Host_ID integer NOT NULL,
  Is_Processed boolean NOT NULL DEFAULT '0',
  Trust_Status varchar(15) DEFAULT NULL,
  RQ_Error_Code integer DEFAULT NULL,
  RQ_Error_Description varchar(100) DEFAULT NULL,
  PRIMARY KEY (ID)
);

/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE SEQUENCE tbl_ta_log_serial;
CREATE TABLE tbl_ta_log (
  ID integer NOT NULL DEFAULT nextval('tbl_ta_log_serial'),
  Host_ID integer NOT NULL,
  MLE_ID integer NOT NULL,
  Manifest_Name varchar(25) NOT NULL,
  Manifest_Value varchar(100) NOT NULL,
  Trust_Status boolean NOT NULL,
  Error varchar(100) DEFAULT NULL,
  Updated_On timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (ID)
);

/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE SEQUENCE tbl_module_manifest_serial;
CREATE TABLE tbl_module_manifest (
  ID integer NOT NULL DEFAULT nextval('tbl_module_manifest_serial'),
  MLE_ID integer NOT NULL,
  Event_ID integer NOT NULL,
  NameSpace_ID integer NOT NULL,
  ComponentName varchar(150) NOT NULL,
  DigestValue varchar(100) DEFAULT NULL,
  ExtendedToPCR varchar(5) DEFAULT NULL,
  PackageName varchar(45) DEFAULT NULL,
  PackageVendor varchar(45) DEFAULT NULL,
  PackageVersion varchar(45) DEFAULT NULL,
  UseHostSpecificDigestValue boolean DEFAULT NULL,
  Description varchar(100) DEFAULT NULL,
 
  PRIMARY KEY (ID),
  CONSTRAINT Module_MLE_ID FOREIGN KEY (MLE_ID) REFERENCES tbl_mle (ID) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT Module_NameSpace_ID FOREIGN KEY (NameSpace_ID) REFERENCES tbl_package_namespace (ID) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT Module_Event_ID FOREIGN KEY (Event_ID) REFERENCES tbl_event_type (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);

/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE SEQUENCE tbl_host_specific_manifest_serial;
CREATE TABLE tbl_host_specific_manifest (
  ID integer NOT NULL DEFAULT nextval('tbl_host_specific_manifest_serial'),
  Module_Manifest_ID integer NOT NULL,
  Host_ID integer NOT NULL,
  DigestValue varchar(100) NOT NULL,
  PRIMARY KEY (ID),
  CONSTRAINT Module_Manifest_ID FOREIGN KEY (Module_Manifest_ID) REFERENCES tbl_module_manifest (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);

/*!40101 SET character_set_client = @saved_cs_client */;
