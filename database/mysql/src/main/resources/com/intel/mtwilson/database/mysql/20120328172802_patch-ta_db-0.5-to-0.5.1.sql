-- created 2012-03-28
-- OPTIONAL: if other scripts run before and after this one, this one is not strictly needed

INSERT INTO `changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20120328172802,NOW(),'update schema from 0.5 to 0.5.1');

CREATE TABLE `tbl_api_client` (
  `Client_ID` varchar(128) NOT NULL,
  `Secret_Key` varchar(248) NOT NULL,
  PRIMARY KEY (`Client_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO `changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20120328172741,NOW(),'premium - create 0.5.1 schema api client and portal user');

CREATE TABLE `tbl_event_type` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `Name` varchar(75) NOT NULL,
  `FieldName` varchar(45) NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=latin1;

INSERT INTO `changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20120328172742,NOW(),'premium - create 0.5.1 schema module event');

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

CREATE TABLE `tbl_package_namespace` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `Name` varchar(45) NOT NULL,
  `VendorName` varchar(45) NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;

INSERT INTO `changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20120328172745,NOW(),'premium - create 0.5.1 schema module package namespace');

ALTER TABLE `tbl_module_manifest` MODIFY COLUMN `Event_ID` int(11) NOT NULL;
ALTER TABLE `tbl_module_manifest` MODIFY COLUMN `NameSpace_ID` int(11) NOT NULL;
ALTER TABLE `tbl_module_manifest` ADD COLUMN `Event_ID` int(11) NOT NULL;
ALTER TABLE `tbl_module_manifest` ADD COLUMN `NameSpace_ID` int(11) NOT NULL;
ALTER TABLE `tbl_module_manifest` ADD COLUMN `ComponentName` varchar(150) NOT NULL;
ALTER TABLE `tbl_module_manifest` ADD COLUMN `DigestValue` varchar(100) DEFAULT NULL;
ALTER TABLE `tbl_module_manifest` ADD COLUMN `ExtendedToPCR` varchar(5) DEFAULT NULL;
ALTER TABLE `tbl_module_manifest` ADD COLUMN `PackageName` varchar(45) DEFAULT NULL;
ALTER TABLE `tbl_module_manifest` ADD COLUMN `PackageVendor` varchar(45) DEFAULT NULL;
ALTER TABLE `tbl_module_manifest` ADD COLUMN `PackageVersion` varchar(45) DEFAULT NULL;
ALTER TABLE `tbl_module_manifest` ADD COLUMN `UseHostSpecificDigestValue` tinyint(1) DEFAULT NULL;
ALTER TABLE `tbl_module_manifest` DROP COLUMN `Name`;
ALTER TABLE `tbl_module_manifest` DROP COLUMN `Value`;
ALTER TABLE `tbl_module_manifest` DROP COLUMN `Type`;
ALTER TABLE `tbl_module_manifest` MODIFY COLUMN `Updated_By` int(11) DEFAULT NULL;
ALTER TABLE `tbl_module_manifest` MODIFY COLUMN `Updated_On` datetime DEFAULT NULL;
ALTER TABLE `tbl_module_manifest` ADD COLUMN `Updated_By` int(11) DEFAULT NULL;
ALTER TABLE `tbl_module_manifest` ADD COLUMN `Updated_On` datetime DEFAULT NULL;
ALTER TABLE `tbl_module_manifest` ADD KEY `Module_NameSpace_ID` (`NameSpace_ID`);
ALTER TABLE `tbl_module_manifest` ADD KEY `Module_Event_ID` (`Event_ID`);
ALTER TABLE `tbl_module_manifest` ADD CONSTRAINT `Module_NameSpace_ID` FOREIGN KEY (`NameSpace_ID`) REFERENCES `tbl_package_namespace` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `tbl_module_manifest` ADD CONSTRAINT `Module_Event_ID` FOREIGN KEY (`Event_ID`) REFERENCES `tbl_event_type` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;

INSERT INTO `changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20120328172748,NOW(),'premium - create 0.5.1 schema module manifest 2');
