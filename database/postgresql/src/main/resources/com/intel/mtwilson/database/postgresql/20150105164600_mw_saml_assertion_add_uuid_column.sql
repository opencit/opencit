-- rksavino
-- Updates for the mw_saml_assertion table
ALTER TABLE mw_saml_assertion ADD COLUMN uuid_hex CHAR(36) NULL;
ALTER TABLE mw_saml_assertion ADD COLUMN trust_report TEXT DEFAULT NULL;
DO
$$
DECLARE
    rec   record;
BEGIN
   FOR rec IN
      SELECT *
      FROM   mw_saml_assertion
   LOOP
      UPDATE mw_saml_assertion mw SET uuid_hex = (SELECT uuid_generate_v4()) where mw.id = rec.id;
   END LOOP;
END$$;

INSERT INTO mw_changelog (ID, APPLIED_AT, DESCRIPTION) VALUES (20150105164600,NOW(),'Added UUID column for mw_saml_assertion');
