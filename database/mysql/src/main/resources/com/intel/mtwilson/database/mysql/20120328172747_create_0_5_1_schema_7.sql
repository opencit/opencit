
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

INSERT INTO `changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20120328172747,NOW(),'premium - create 0.5.1 schema host specific manifest');
