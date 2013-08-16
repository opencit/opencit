

/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_pcr_manifest` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `MLE_ID` int(11) NOT NULL,
  `Name` varchar(20) NOT NULL,
  `Value` varchar(100) NOT NULL,
 
  `PCR_Description` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `PCR_MLE_ID` (`MLE_ID`),
 
 
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

  PRIMARY KEY (`ID`),
  KEY `Module_MLE_ID` (`MLE_ID`),

  KEY `Module_NameSpace_ID` (`NameSpace_ID`),
  KEY `Module_Event_ID` (`Event_ID`),
  CONSTRAINT `Module_MLE_ID` FOREIGN KEY (`MLE_ID`) REFERENCES `tbl_mle` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `Module_NameSpace_ID` FOREIGN KEY (`NameSpace_ID`) REFERENCES `tbl_package_namespace` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `Module_Event_ID` FOREIGN KEY (`Event_ID`) REFERENCES `tbl_event_type` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=71 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

INSERT INTO `changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20120328172746,NOW(),'premium - create 0.5.1 schema module manifest');
