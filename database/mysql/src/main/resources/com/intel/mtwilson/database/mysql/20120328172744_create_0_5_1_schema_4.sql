
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
  `Error_Code` int(11) DEFAULT NULL,
  `Error_Description` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `BIOS_MLE_ID` (`BIOS_MLE_ID`),
  KEY `VMM_MLE_ID` (`VMM_MLE_ID`),
  CONSTRAINT `BIOS_MLE_ID` FOREIGN KEY (`BIOS_MLE_ID`) REFERENCES `tbl_mle` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `VMM_MLE_ID` FOREIGN KEY (`VMM_MLE_ID`) REFERENCES `tbl_mle` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=64 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

ALTER TABLE `tbl_hosts` ADD COLUMN `Location` varchar(200) DEFAULT NULL;


INSERT INTO `changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20120328172744,NOW(),'premium - create 0.5.1 schema add host location');
