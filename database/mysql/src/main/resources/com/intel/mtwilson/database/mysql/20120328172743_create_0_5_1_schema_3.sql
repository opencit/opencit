
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_location_pcr` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `location` varchar(200) NOT NULL,
  `pcr_value` varchar(100) NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1 COMMENT='Mapping between the pcr values and location';

INSERT INTO `changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20120328172743,NOW(),'premium - create 0.5.1 schema location pcr');

