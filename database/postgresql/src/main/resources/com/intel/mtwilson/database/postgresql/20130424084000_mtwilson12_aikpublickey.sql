-- created 2013-01-08


ALTER TABLE mw_hosts ADD COLUMN AIK_PublicKey TEXT DEFAULT NULL;


INSERT INTO mw_changelog (ID, APPLIED_AT, DESCRIPTION) VALUES (20130424084000,NOW(),'core - Mt Wilson 1.2 adds AIK_PublicKey field to mw_hosts');
