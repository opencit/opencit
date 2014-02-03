-- created 2013-05-21
-- stdalex
-- This script creates the processor mapping table, which can be used to retrieve the platform generation provided either the processor name or CPUID

ALTER TABLE mw_hosts ALTER COLUMN ipaddress TYPE varchar(255);
ALTER TABLE mw_hosts ALTER COLUMN name TYPE varchar(255); 

INSERT INTO mw_changelog (ID, APPLIED_AT, DESCRIPTION) VALUES (20130523095100,NOW(),'Patch to expand IpAddress and host column length in mw_hosts');