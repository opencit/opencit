-- created 2015-01-30
-- ssbangal
-- Adds the binding key certificate column to the Hosts table


ALTER TABLE `mw_hosts` ADD COLUMN `binding_key_certificate` text;

INSERT INTO `mw_changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20150130160000,NOW(),'Added binding key certificate column to host table');
