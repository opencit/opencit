-- created 2014-01-17
-- ssbangal
-- Adds the uuid column to all the tables and updates the same with a unique uuid value

ALTER TABLE `mw_as`.`mw_portal_user` ADD COLUMN `uuid_hex` CHAR(36) NOT NULL  AFTER `ID` ;
UPDATE mw_portal_user SET uuid_hex = (SELECT uuid());

INSERT INTO `mw_changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20140117160000,NOW(),'Added UUID fields for all the tables');
