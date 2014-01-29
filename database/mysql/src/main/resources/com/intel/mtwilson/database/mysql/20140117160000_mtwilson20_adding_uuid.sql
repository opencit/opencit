-- created 2014-01-17
-- ssbangal
-- Adds the uuid column to all the tables and updates the same with a unique uuid value

ALTER TABLE `mw_as`.`mw_portal_user` ADD COLUMN `uuid_hex` CHAR(36) NULL;
UPDATE mw_portal_user SET uuid_hex = (SELECT uuid());

ALTER TABLE `mw_as`.`mw_api_client_x509` ADD COLUMN `uuid_hex` CHAR(36) NULL;
UPDATE mw_api_client_x509 SET uuid_hex = (SELECT uuid());

ALTER TABLE `mw_as`.`mw_oem` ADD COLUMN `uuid_hex` CHAR(36) NULL;
UPDATE mw_oem SET uuid_hex = (SELECT uuid());

ALTER TABLE `mw_as`.`mw_os` ADD COLUMN `uuid_hex` CHAR(36) NULL;
UPDATE mw_os SET uuid_hex = (SELECT uuid());

ALTER TABLE `mw_as`.`mw_mle` ADD COLUMN `uuid_hex` CHAR(36) NULL;
UPDATE mw_mle SET uuid_hex = (SELECT uuid());
-- Adds the reference to the OEM UUID column in the MLE table
ALTER TABLE `mw_as`.`mw_mle` ADD COLUMN `oem_uuid_hex` CHAR(36) NULL;
UPDATE mw_mle mm SET oem_uuid_hex = (SELECT moem.uuid_hex FROM mw_oem moem WHERE moem.ID = mm.OEM_ID);
-- Adds the reference to the OS UUID column in the MLE table
ALTER TABLE `mw_as`.`mw_mle` ADD COLUMN `os_uuid_hex` CHAR(36) NULL;
UPDATE mw_mle mm SET os_uuid_hex = (SELECT mos.uuid_hex FROM mw_os mos WHERE mos.ID = mm.OS_ID);


ALTER TABLE `mw_as`.`mw_mle_source` ADD COLUMN `uuid_hex` CHAR(36) NULL;
UPDATE mw_mle_source SET uuid_hex = (SELECT uuid());
-- Adds the reference to the MLE UUID column in the MW_MLE_Source table
ALTER TABLE `mw_as`.`mw_mle_source` ADD COLUMN `mle_uuid_hex` CHAR(36) NULL;
UPDATE mw_mle_source ms SET mle_uuid_hex = (SELECT m.uuid_hex FROM mw_mle m WHERE m.ID = ms.MLE_ID);

ALTER TABLE `mw_as`.`mw_pcr_manifest` ADD COLUMN `uuid_hex` CHAR(36) NULL;
UPDATE mw_pcr_manifest SET uuid_hex = (SELECT uuid());
-- Adds the reference to the MLE UUID column in the MW_PCR_Manifest table
ALTER TABLE `mw_as`.`mw_pcr_manifest` ADD COLUMN `mle_uuid_hex` CHAR(36) NULL;
UPDATE mw_pcr_manifest mpm SET mle_uuid_hex = (SELECT m.uuid_hex FROM mw_mle m WHERE m.ID = mpm.MLE_ID);

ALTER TABLE `mw_as`.`mw_module_manifest` ADD COLUMN `uuid_hex` CHAR(36) NULL;
UPDATE mw_module_manifest SET uuid_hex = (SELECT uuid());
-- Adds the reference to the MLE UUID column in the MW_PCR_Manifest table
ALTER TABLE `mw_as`.`mw_module_manifest` ADD COLUMN `mle_uuid_hex` CHAR(36) NULL;
UPDATE mw_module_manifest mpm SET mle_uuid_hex = (SELECT m.uuid_hex FROM mw_mle m WHERE m.ID = mpm.MLE_ID);

ALTER TABLE `mw_as`.`mw_asset_tag_certificate` ADD COLUMN `uuid_hex` CHAR(36) NULL;
UPDATE mw_asset_tag_certificate SET uuid_hex = (SELECT uuid());

INSERT INTO `mw_changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20140117160000,NOW(),'Added UUID fields for all the tables');
