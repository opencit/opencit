-- created 2014-04-30  MYSQL
-- rksavino
-- Adds a column for aik_publickey_sha1

ALTER TABLE `mw_hosts` ADD COLUMN `aik_publickey_sha1` varchar(40) DEFAULT NULL AFTER `aik_publickey`;
  
INSERT INTO `mw_changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20140430140500,NOW(),'Patch to add a column for aik_publickey_sha1.');