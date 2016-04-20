

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

INSERT INTO `changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20120328172741,NOW(),'premium - create 0.5.1 schema api client and portal user');

