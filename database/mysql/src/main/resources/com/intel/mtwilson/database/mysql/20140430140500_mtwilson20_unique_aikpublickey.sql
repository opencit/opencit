-- created 2014-04-30  MYSQL
-- rksavino
-- Makes aik_publickey a unique key in mw_hosts, also adds a column for aik_publickey_sha1

-- ALTER TABLE `mw_as`.`mw_hosts` ADD UNIQUE KEY `mw_host_aik_public_key_unique` (`aik_publickey`);
ALTER TABLE `mw_as`.`mw_hosts` ADD COLUMN `aik_publickey_sha1` varchar(40) DEFAULT NULL AFTER `aik_publickey`;
  
INSERT INTO `mw_changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20140430140500,NOW(),'Patch to make aik_publickey a unique key in mw_hosts, also adds a column for aik_publickey_sha1.');