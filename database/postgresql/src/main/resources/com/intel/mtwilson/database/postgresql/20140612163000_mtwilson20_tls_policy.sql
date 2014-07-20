
ALTER TABLE mw_hosts ALTER COLUMN TlsPolicy DROP NOT NULL;
ALTER TABLE mw_hosts ALTER COLUMN TlsPolicy SET DEFAULT NULL;
ALTER TABLE mw_hosts ADD COLUMN tls_policy_id char(36);

CREATE TABLE mw_tls_policy (
  id char(36) NOT NULL,
  name varchar(255) NOT NULL,
  private boolean NOT NULL,
  content_type varchar(255) NOT NULL,
  content bytea NOT NULL,
  comment text NULL,
  PRIMARY KEY (id)
);

INSERT INTO mw_changelog (ID, APPLIED_AT, DESCRIPTION) VALUES (20140612163000,NOW(),'Mt Wilson 2.0 - TLS Policy');
