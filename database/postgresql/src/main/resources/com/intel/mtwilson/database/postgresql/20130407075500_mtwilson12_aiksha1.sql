-- created 2013-01-08


ALTER TABLE mw_hosts ADD COLUMN AIK_SHA1 varchar(40) DEFAULT NULL;


INSERT INTO mw_changelog (ID, APPLIED_AT, DESCRIPTION) VALUES (20130407075500,NOW(),'core - Mt Wilson 1.2 adds AIK_SHA1 field to mw_hosts');
