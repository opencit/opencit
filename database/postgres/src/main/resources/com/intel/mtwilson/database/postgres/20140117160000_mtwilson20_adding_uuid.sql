-- created 2014-01-17
-- ssbangal
-- Adds the uuid column to all the tables and updates the same with a unique uuid value

-- Since uuid is a contrib module, it is not loaded by default. So, we need to load it first.
CREATE EXTENSION "uuid-ossp";

ALTER TABLE mw_portal_user ADD COLUMN uuid_hex CHAR(36) NULL;
DO
$$
DECLARE
    rec   record;
BEGIN
   FOR rec IN
      SELECT *
      FROM   mw_portal_user
   LOOP
      UPDATE mw_portal_user mw SET uuid_hex = (SELECT uuid_generate_v4()) where mw.id = rec.id;
   END LOOP;
END$$;


ALTER TABLE mw_api_client_x509 ADD COLUMN uuid_hex CHAR(36) NULL;
DO
$$
DECLARE
    rec   record;
BEGIN
   FOR rec IN
      SELECT *
      FROM   mw_api_client_x509
   LOOP
      UPDATE mw_api_client_x509 mw SET uuid_hex = (SELECT uuid_generate_v4()) where mw.id = rec.id;
   END LOOP;
END$$;

ALTER TABLE mw_oem ADD COLUMN uuid_hex CHAR(36) NULL;
DO
$$
DECLARE
    rec   record;
BEGIN
   FOR rec IN
      SELECT *
      FROM   mw_oem
   LOOP
      UPDATE mw_oem mw SET uuid_hex = (SELECT uuid_generate_v4()) where mw.id = rec.id;
   END LOOP;
END$$;

ALTER TABLE mw_os ADD COLUMN uuid_hex CHAR(36) NULL;
DO
$$
DECLARE
    rec   record;
BEGIN
   FOR rec IN
      SELECT *
      FROM   mw_os
   LOOP
      UPDATE mw_os mw SET uuid_hex = (SELECT uuid_generate_v4()) where mw.id = rec.id;
   END LOOP;
END$$;

ALTER TABLE mw_mle ADD COLUMN uuid_hex CHAR(36) NULL;
DO
$$
DECLARE
    rec   record;
BEGIN
   FOR rec IN
      SELECT *
      FROM   mw_mle
   LOOP
      UPDATE mw_mle mw SET uuid_hex = (SELECT uuid_generate_v4()) where mw.id = rec.id;
   END LOOP;
END$$;

ALTER TABLE mw_mle_source ADD COLUMN uuid_hex CHAR(36) NULL;
DO
$$
DECLARE
    rec   record;
BEGIN
   FOR rec IN
      SELECT *
      FROM   mw_mle_source
   LOOP
      UPDATE mw_mle_source mw SET uuid_hex = (SELECT uuid_generate_v4()) where mw.id = rec.id;
   END LOOP;
END$$;

-- We need to use the UUID from the mw_mle table to update the mw_mle_source table 
ALTER TABLE mw_mle_source ADD COLUMN mle_uuid_hex CHAR(36) NULL;
DO
$$
DECLARE
    rec   record;
BEGIN
   FOR rec IN
      SELECT *
      FROM   mw_mle_source
   LOOP
      UPDATE mw_mle_source mw SET mle_uuid_hex = (SELECT m.uuid_hex FROM mw_mle m WHERE m.id = mw.mle_id);
   END LOOP;
END$$;


ALTER TABLE mw_pcr_manifest ADD COLUMN uuid_hex CHAR(36) NULL;
DO
$$
DECLARE
    rec   record;
BEGIN
   FOR rec IN
      SELECT *
      FROM   mw_pcr_manifest
   LOOP
      UPDATE mw_pcr_manifest mw SET uuid_hex = (SELECT uuid_generate_v4()) where mw.id = rec.id;
   END LOOP;
END$$;

ALTER TABLE mw_pcr_manifest ADD COLUMN mle_uuid_hex CHAR(36) NULL;
DO
$$
DECLARE
    rec   record;
BEGIN
   FOR rec IN
      SELECT *
      FROM   mw_pcr_manifest
   LOOP
      UPDATE mw_pcr_manifest mw SET mle_uuid_hex = (SELECT m.uuid_hex FROM mw_mle m WHERE m.id = mw.mle_id);
   END LOOP;
END$$;

ALTER TABLE mw_module_manifest ADD COLUMN uuid_hex CHAR(36) NULL;
DO
$$
DECLARE
    rec   record;
BEGIN
   FOR rec IN
      SELECT *
      FROM   mw_module_manifest
   LOOP
      UPDATE mw_module_manifest mw SET uuid_hex = (SELECT uuid_generate_v4()) where mw.id = rec.id;
   END LOOP;
END$$;

ALTER TABLE mw_module_manifest ADD COLUMN mle_uuid_hex CHAR(36) NULL;
DO
$$
DECLARE
    rec   record;
BEGIN
   FOR rec IN
      SELECT *
      FROM   mw_module_manifest
   LOOP
      UPDATE mw_module_manifest mw SET mle_uuid_hex = (SELECT m.uuid_hex FROM mw_mle m WHERE m.id = mw.mle_id);
   END LOOP;
END$$;

INSERT INTO `mw_changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20140117160000,NOW(),'Added UUID fields for all the tables');
