-- created 2014-03-04
-- ssbangal
-- Adds the create_time column to the asset tag certificate table

ALTER TABLE `mw_asset_tag_certificate` ADD COLUMN `create_time` BIGINT  DEFAULT NULL AFTER `uuid_hex` ;

INSERT INTO `mw_changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20140304120000,NOW(),'Added create_time field for the asset tag certificate table');
