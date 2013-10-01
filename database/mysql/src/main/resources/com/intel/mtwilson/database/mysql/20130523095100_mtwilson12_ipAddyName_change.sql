-- created 2013-05-21
-- stdalex
-- This script creates the processor mapping table, which can be used to retrieve the platform generation provided either the processor name or CPUID

ALTER TABLE `mw_hosts` MODIFY COLUMN `IPAddress` varchar(255) DEFAULT NULL; 
ALTER TABLE `mw_hosts` MODIFY COLUMN `Name` varchar(255) DEFAULT NULL; 

INSERT INTO `mw_changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20130523095100,NOW(),'Patch to expand IpAddress and host column length in mw_hosts');