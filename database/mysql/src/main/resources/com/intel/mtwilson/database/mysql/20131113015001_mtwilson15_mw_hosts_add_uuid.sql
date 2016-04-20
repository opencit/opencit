-- created 2013-12-18

-- This script adds the UUID field to the mw_hosts so that it can be tracked during host registration

ALTER TABLE mw_hosts ADD hardware_uuid VARCHAR(254);

INSERT INTO `mw_changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20131113015001,NOW(),'Add UUID field to mw_host for asset tag association');