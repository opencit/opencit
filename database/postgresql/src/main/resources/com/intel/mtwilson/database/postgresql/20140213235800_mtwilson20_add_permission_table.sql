-- created 2014-02-13

-- XXX TODO should user_id be different for x509, hmac, and basic so user can have
-- different permissions depending on how securely user was logged in?

CREATE TABLE mw_user_permission (
  user_id character varying(36) DEFAULT NULL,
  permission_text character varying(200) DEFAULT NULL,
  PRIMARY KEY (user_id)
); 

INSERT INTO mw_changelog (ID, APPLIED_AT, DESCRIPTION) VALUES (20140213235800,NOW(),'Mt Wilson 2.0 - added permission table');
