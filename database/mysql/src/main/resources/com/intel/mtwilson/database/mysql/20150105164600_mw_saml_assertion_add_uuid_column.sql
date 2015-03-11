-- rksavino
-- Updates for the mw_saml_assertion table
ALTER TABLE `mw_saml_assertion` ADD COLUMN `uuid_hex` CHAR(36) NULL;
ALTER TABLE `mw_saml_assertion` ADD COLUMN `trust_report` TEXT DEFAULT NULL;
UPDATE mw_saml_assertion SET uuid_hex = (SELECT uuid());

INSERT INTO `mw_changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20150105164600,NOW(),'Added UUID column for mw_saml_assertion');
