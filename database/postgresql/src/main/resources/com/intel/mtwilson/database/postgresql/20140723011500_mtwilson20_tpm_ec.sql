
CREATE TABLE mw_tpm_endorsement (
  id char(36) NOT NULL,
  hardware_uuid char(36) NOT NULL,
  issuer varchar(255) NOT NULL,
  revoked boolean NOT NULL DEFAULT false,
  certificate bytea NOT NULL,
  comment text NULL,
  PRIMARY KEY (id)
);

CREATE UNIQUE index mw_tpm_endorsement_hardware_uuid_unique_constraint on mw_tpm_endorsement(lower(hardware_uuid));

INSERT INTO mw_changelog (ID, APPLIED_AT, DESCRIPTION) VALUES (20140723011500,NOW(),'Mt Wilson 2.0 - TPM Endorsement');
