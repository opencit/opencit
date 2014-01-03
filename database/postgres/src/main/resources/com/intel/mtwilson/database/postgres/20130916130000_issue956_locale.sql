-- created 2013-09-16
-- jbuhacoff
-- Adds a locale field for per-user localizable messages
-- the "locale" name is NOT reserved http://www.postgresql.org/docs/9.1/static/sql-keywords-appendix.html

ALTER TABLE mw_portal_user ADD COLUMN locale varchar(36) NULL; 

INSERT INTO mw_changelog (ID, APPLIED_AT, DESCRIPTION) VALUES (20130916130000,NOW(),'Added locale field for users');
