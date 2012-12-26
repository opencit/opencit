
INSERT INTO `changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20120328172740,NOW(),'create 0.5.1 schema');


/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_api_client` (
  `Client_ID` varchar(128) NOT NULL,
  `Secret_Key` varchar(248) NOT NULL,
  PRIMARY KEY (`Client_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_db_portal_user` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `Login` varchar(15) NOT NULL,
  `Password` varchar(15) NOT NULL,
  `First_Name` varchar(15) NOT NULL,
  `Last_Name` varchar(25) NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `User_ID` (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_event_type` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `Name` varchar(75) NOT NULL,
  `FieldName` varchar(45) NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_location_pcr` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `location` varchar(200) NOT NULL,
  `pcr_value` varchar(100) NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1 COMMENT='This table contains the mapping between the pcr values and location';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_mle` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `Name` varchar(20) NOT NULL COMMENT '	',
  `Version` varchar(20) NOT NULL,
  `Attestation_Type` varchar(20) NOT NULL DEFAULT 'PCR',
  `MLE_Type` varchar(20) NOT NULL DEFAULT 'VMM',
  `Required_Manifest_List` varchar(100) NOT NULL,
  `Description` varchar(100) DEFAULT NULL,
  `OS_ID` int(11) DEFAULT NULL,
  `OEM_ID` int(11) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `MLE_ID` (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=latin1;
ALTER TABLE `tbl_mle` MODIFY COLUMN `Name` VARCHAR(100) NOT NULL;
ALTER TABLE `tbl_mle` MODIFY COLUMN `Version` VARCHAR(100) NOT NULL;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_hosts` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `BIOS_MLE_ID` int(11) NOT NULL,
  `VMM_MLE_ID` int(11) NOT NULL,
  `Name` varchar(40) NOT NULL,
  `IPAddress` varchar(20) DEFAULT NULL,
  `Port` int(11) NOT NULL,
  `Description` varchar(100) DEFAULT NULL,
  `AddOn_Connection_Info` varchar(80) DEFAULT NULL,
  `AIK_Certificate` text,
  `Email` varchar(45) DEFAULT NULL,
  `Created_On` datetime NOT NULL,
  `Updated_On` datetime NOT NULL,
  `Error_Code` int(11) DEFAULT NULL,
  `Error_Description` varchar(100) DEFAULT NULL,
  `Location` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `BIOS_MLE_ID` (`BIOS_MLE_ID`),
  KEY `VMM_MLE_ID` (`VMM_MLE_ID`),
  CONSTRAINT `BIOS_MLE_ID` FOREIGN KEY (`BIOS_MLE_ID`) REFERENCES `tbl_mle` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `VMM_MLE_ID` FOREIGN KEY (`VMM_MLE_ID`) REFERENCES `tbl_mle` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=64 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_oem` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `NAME` varchar(100) DEFAULT NULL,
  `DESCRIPTION` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `tbl_oem.UNIQUE` (`NAME`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_os` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `NAME` varchar(100) NOT NULL,
  `VERSION` varchar(50) NOT NULL,
  `DESCRIPTION` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `tbl_os_name_version.UNIQUE` (`NAME`,`VERSION`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_package_namespace` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `Name` varchar(45) NOT NULL,
  `VendorName` varchar(45) NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_pcr_manifest` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `MLE_ID` int(11) NOT NULL,
  `Name` varchar(20) NOT NULL,
  `Value` varchar(100) NOT NULL,
  `Created_By` int(11) NOT NULL,
  `Created_On` datetime NOT NULL,
  `Updated_By` int(11) NOT NULL,
  `Updated_On` datetime NOT NULL,
  `PCR_Description` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `PCR_MLE_ID` (`MLE_ID`),
  KEY `PCR_Created_By` (`Created_By`),
  KEY `PCR_Last_Updated_By` (`Updated_By`),
  CONSTRAINT `PCR_Created_By` FOREIGN KEY (`Created_By`) REFERENCES `tbl_db_portal_user` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `PCR_Last_Updated_By` FOREIGN KEY (`Updated_By`) REFERENCES `tbl_db_portal_user` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `PCR_MLE_ID` FOREIGN KEY (`MLE_ID`) REFERENCES `tbl_mle` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_request_queue` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `Host_ID` int(11) NOT NULL,
  `Is_Processed` tinyint(1) NOT NULL,
  `Trust_Status` varchar(15) DEFAULT NULL,
  `RQ_Error_Code` int(11) DEFAULT NULL,
  `RQ_Error_Description` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_ta_log` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `Host_ID` int(11) NOT NULL,
  `MLE_ID` int(11) NOT NULL,
  `Manifest_Name` varchar(25) NOT NULL,
  `Manifest_Value` varchar(100) NOT NULL,
  `Trust_Status` tinyint(1) NOT NULL,
  `Error` varchar(100) DEFAULT NULL,
  `Updated_On` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=10329 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_module_manifest` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `MLE_ID` int(11) NOT NULL,
  `Event_ID` int(11) NOT NULL,
  `NameSpace_ID` int(11) NOT NULL,
  `ComponentName` varchar(150) NOT NULL,
  `DigestValue` varchar(100) DEFAULT NULL,
  `ExtendedToPCR` varchar(5) DEFAULT NULL,
  `PackageName` varchar(45) DEFAULT NULL,
  `PackageVendor` varchar(45) DEFAULT NULL,
  `PackageVersion` varchar(45) DEFAULT NULL,
  `UseHostSpecificDigestValue` tinyint(1) DEFAULT NULL,
  `Description` varchar(100) DEFAULT NULL,
  `Created_By` int(11) NOT NULL,
  `Created_On` datetime NOT NULL,
  `Updated_By` int(11) DEFAULT NULL,
  `Updated_On` datetime DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `Module_MLE_ID` (`MLE_ID`),
  KEY `Module_Created_By` (`Created_By`),
  KEY `Module_Last_Updated_By` (`Updated_By`),
  KEY `Module_NameSpace_ID` (`NameSpace_ID`),
  KEY `Module_Event_ID` (`Event_ID`),
  CONSTRAINT `Module_MLE_ID` FOREIGN KEY (`MLE_ID`) REFERENCES `tbl_mle` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `Module_Created_By` FOREIGN KEY (`Created_By`) REFERENCES `tbl_db_portal_user` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `Module_Last_Updated_By` FOREIGN KEY (`Updated_By`) REFERENCES `tbl_db_portal_user` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `Module_NameSpace_ID` FOREIGN KEY (`NameSpace_ID`) REFERENCES `tbl_package_namespace` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `Module_Event_ID` FOREIGN KEY (`Event_ID`) REFERENCES `tbl_event_type` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=71 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_host_specific_manifest` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `Module_Manifest_ID` int(11) NOT NULL,
  `Host_ID` int(11) NOT NULL,
  `DigestValue` varchar(100) NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `Module_Manifest_ID` (`Module_Manifest_ID`),
  CONSTRAINT `Module_Manifest_ID` FOREIGN KEY (`Module_Manifest_ID`) REFERENCES `tbl_module_manifest` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
