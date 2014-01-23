-- created 2014-01-17
-- ssbangal
-- Adds the uuid column to all the tables and updates the same with a unique uuid value

ALTER TABLE `mw_as`.`mw_portal_user` ADD COLUMN `uuid_hex` CHAR(36) NOT NULL  AFTER `ID` ;
UPDATE mw_portal_user SET uuid_hex = (SELECT uuid());

ALTER TABLE `mw_as`.`mw_api_client_x509` ADD COLUMN `uuid_hex` CHAR(36) NOT NULL  AFTER `ID` ;
UPDATE mw_api_client_x509 SET uuid_hex = (SELECT uuid());

ALTER TABLE `mw_as`.`mw_oem` ADD COLUMN `uuid_hex` CHAR(36) NOT NULL  AFTER `ID` ;
UPDATE mw_oem SET uuid_hex = (SELECT uuid());

ALTER TABLE `mw_as`.`mw_os` ADD COLUMN `uuid_hex` CHAR(36) NOT NULL  AFTER `ID` ;
UPDATE mw_os SET uuid_hex = (SELECT uuid());

ALTER TABLE `mw_as`.`mw_mle` ADD COLUMN `uuid_hex` CHAR(36) NOT NULL  AFTER `ID` ;
UPDATE mw_mle SET uuid_hex = (SELECT uuid());

-- We need to use the UUID from the mw_mle table to update the mw_mle_source table
ALTER TABLE `mw_as`.`mw_mle_source` ADD COLUMN `uuid_hex` CHAR(36) NOT NULL  AFTER `ID` ;
UPDATE mw_mle_source ms SET uuid_hex = (SELECT m.uuid_hex FROM mw_mle m WHERE m.ID = ms.MLE_ID);

INSERT INTO `mw_changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20140117160000,NOW(),'Added UUID fields for all the tables');
