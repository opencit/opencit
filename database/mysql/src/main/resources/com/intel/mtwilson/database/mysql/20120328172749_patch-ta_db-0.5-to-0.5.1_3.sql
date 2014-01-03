-- created 2012-03-28
-- OPTIONAL: if other scripts run before and after this one, this one is not strictly needed



CREATE TABLE `tbl_host_specific_manifest` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `Module_Manifest_ID` int(11) NOT NULL,
  `Host_ID` int(11) NOT NULL,
  `DigestValue` varchar(100) NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `Module_Manifest_ID` (`Module_Manifest_ID`),
  CONSTRAINT `Module_Manifest_ID` FOREIGN KEY (`Module_Manifest_ID`) REFERENCES `tbl_module_manifest` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;

ALTER TABLE `tbl_hosts` MODIFY COLUMN `IPAddress` varchar(20) DEFAULT NULL;
ALTER TABLE `tbl_hosts` ADD COLUMN `IPAddress` varchar(20) DEFAULT NULL;
ALTER TABLE `tbl_hosts` ADD COLUMN `Location` varchar(200) DEFAULT NULL;

ALTER TABLE `tbl_mle` DROP KEY `MLE_ID`;
ALTER TABLE `tbl_mle` ADD UNIQUE KEY `MLE_ID` (`ID`);
ALTER TABLE `tbl_mle` MODIFY COLUMN `Name` VARCHAR(100) NOT NULL;
ALTER TABLE `tbl_mle` MODIFY COLUMN `Version` VARCHAR(100) NOT NULL;

/*CREATE TABLE `tbl_package_namespace` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `Name` varchar(45) NOT NULL,
  `VendorName` varchar(45) NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;*/

INSERT INTO `changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20120328172749,NOW(),'premium - create 0.5.1 schema module package namespace');
