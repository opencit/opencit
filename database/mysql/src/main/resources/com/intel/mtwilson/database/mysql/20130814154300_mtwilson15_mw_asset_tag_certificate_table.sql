-- created 2013-08-14

-- This script creates the table to store the asset tag certificates. This would be initially populated
-- by the Tag Provisioning service. During host registration if an entry exists for the host (based on UUID),
-- then the mapping would be added. 


CREATE TABLE `mw_asset_tag_certificate` (
  `ID` INT(11) NOT NULL AUTO_INCREMENT,
  `Host_ID` INT(11) DEFAULT NULL,
  `UUID` VARCHAR(255) DEFAULT NULL,
  `Certificate` BLOB NOT NULL,
  `SHA1_Hash` BINARY(20) DEFAULT NULL,
  `PCREvent` BINARY(20) DEFAULT NULL,
  `Revoked` BOOLEAN DEFAULT NULL,
  `NotBefore` DATETIME DEFAULT NULL,
  `NotAfter` DATETIME DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `Host_ID` (`Host_ID`),
  CONSTRAINT `Host_ID` FOREIGN KEY (`Host_ID`) REFERENCES `mw_hosts` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION); 

INSERT INTO `mw_changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20130814154300,NOW(),'Patch for creating the Asset Tag certificate table.');