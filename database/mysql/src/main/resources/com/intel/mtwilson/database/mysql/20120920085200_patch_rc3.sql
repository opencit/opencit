-- created 2012-09-20

-- Names of all database tables belonging to Mt Wilson are now prefixed with
-- "mw_" for consistency and to facilitate integration with other systems
ALTER TABLE `changelog` RENAME TO `mw_changelog`;

-- This file contains schema changes from Mt Wilson 1.0-RC2 to Mt Wilson 1.1
INSERT INTO `mw_changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20120920085200,NOW(),'core - patch for 1.1');

