
-- fix sql problem from 20130427142800 that created only a sequence and no table
DROP SEQUENCE mw_request_log;
CREATE TABLE mw_request_log (
  ID integer NOT NULL DEFAULT nextval('mw_request_log_serial'),
  instance varchar(255) NOT NULL,
  received timestamp NOT NULL,
  source varchar(255) NOT NULL,
  content TEXT NOT NULL,
  md5_hash bytea NOT NULL, 
  PRIMARY KEY (ID)
);

ALTER TABLE mw_request_log DROP COLUMN ID;
ALTER TABLE mw_request_log DROP COLUMN md5_hash;
ALTER TABLE mw_request_log ADD COLUMN digest varchar(128) not null;
CREATE UNIQUE index mw_request_log_unique_constraint on mw_request_log(lower(digest));


INSERT INTO mw_changelog (ID, APPLIED_AT, DESCRIPTION) VALUES (20140601021500,NOW(),'Mt Wilson 2.0 - X509 replay protection');
