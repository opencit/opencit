-- created 2014-02-14
-- ssbangal
-- Updates the keystore column in mw_portal_user so that NULL value is allowed.

-- Updates for the Portal User table
ALTER TABLE `mw_portal_user` CHANGE COLUMN `keystore` `keystore` BLOB NULL  ;

INSERT INTO `mw_changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20140214074400,NOW(),'Updated the keystore column to be null');