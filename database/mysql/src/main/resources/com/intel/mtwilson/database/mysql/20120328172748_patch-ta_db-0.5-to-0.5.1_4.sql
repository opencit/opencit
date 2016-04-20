-- created 2012-03-28
-- OPTIONAL: if other scripts run before and after this one, this one is not strictly needed


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
