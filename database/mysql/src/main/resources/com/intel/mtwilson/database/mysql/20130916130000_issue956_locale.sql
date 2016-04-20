-- created 2013-09-16
-- jbuhacoff
-- Adds a locale field for per-user localizable messages

ALTER TABLE `mw_portal_user` ADD COLUMN `locale` varchar(36) DEFAULT NULL; 
ALTER TABLE `mw_api_client_x509` ADD COLUMN `locale` varchar(36) DEFAULT NULL; 

INSERT INTO `mw_changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20130916130000,NOW(),'Added locale field for users');
