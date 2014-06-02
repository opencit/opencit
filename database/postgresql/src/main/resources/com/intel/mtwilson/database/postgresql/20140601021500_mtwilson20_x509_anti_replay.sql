
ALTER TABLE mw_request_log DROP COLUMN ID;
ALTER TABLE mw_request_log DROP COLUMN md5_hash;
ALTER TABLE mw_request_log ADD COLUMN digest varchar(128) not null;
CREATE UNIQUE index mw_request_log_unique_constraint on mw_request_log(lower(digest));


INSERT INTO mw_changelog (ID, APPLIED_AT, DESCRIPTION) VALUES (20140601021500,NOW(),'Mt Wilson 2.0 - X509 replay protection');
