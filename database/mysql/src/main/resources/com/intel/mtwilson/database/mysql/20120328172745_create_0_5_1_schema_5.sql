
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


INSERT INTO `changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20120328172745,NOW(),'premium - create 0.5.1 schema module package namespace');
