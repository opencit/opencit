-- created 2012-09-20

-- Names of all database tables belonging to Mt Wilson are now prefixed with
-- "mw_" for consistency and to facilitate integration with other systems
ALTER TABLE `changelog` RENAME TO `mw_changelog`;
ALTER TABLE `audit_log_entry` RENAME TO `mw_audit_log_entry`;


INSERT INTO `mw_changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20120920091400,NOW(),'rename tables with mw prefix (whitelist service installer)');

