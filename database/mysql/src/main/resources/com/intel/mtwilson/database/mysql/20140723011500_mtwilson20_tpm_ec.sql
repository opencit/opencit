
CREATE TABLE mw_tpm_endorsement (
  id char(36) NOT NULL,
  hardware_uuid char(36) NOT NULL UNIQUE,
  issuer varchar(255) NOT NULL,
  revoked boolean NOT NULL DEFAULT FALSE,
  certificate blob NOT NULL,
  comment text NULL,
  PRIMARY KEY (id)
);

INSERT INTO mw_changelog (ID, APPLIED_AT, DESCRIPTION) VALUES (20140723011500,NOW(),'Mt Wilson 2.0 - TPM Endorsement');
