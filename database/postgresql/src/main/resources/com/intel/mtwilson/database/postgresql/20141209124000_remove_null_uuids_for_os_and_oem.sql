DELETE FROM mw_oem WHERE uuid_hex IS NULL;
DELETE FROM mw_os WHERE uuid_hex IS NULL;

INSERT INTO mw_changelog (ID, APPLIED_AT, DESCRIPTION) VALUES (20141209124000,NOW(),'Removed entries in mw_oem and mw_os that have null uuids');
