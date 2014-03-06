-- created 2014-03-04
-- ssbangal
-- Adds the insert_date column to the asset tag certificate table

ALTER TABLE `mw_as`.`mw_asset_tag_certificate` ADD COLUMN `insert_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP  AFTER `uuid_hex` ;

INSERT INTO `mw_changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20140304120000,NOW(),'Added insert_date field for the asset tag certificate table');
