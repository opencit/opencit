-- created 2014-01-17
-- ssbangal
-- Adds the uuid column to all the tables and updates the same with a unique uuid value

-- Since uuid is a contrib module, it is not loaded by default. So, we need to load it first.
CREATE EXTENSION "uuid-ossp";


-- Updates for the Portal User table
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


-- Updates for the API Client X509 table
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
-- Adds the reference to the User UUID column in the Portal User table
ALTER TABLE mw_api_client_x509 ADD COLUMN user_uuid_hex CHAR(36) NULL;
DO
$$
DECLARE
    rec   record;
BEGIN
   FOR rec IN
      SELECT *
      FROM   mw_api_client_x509
   LOOP
      UPDATE mw_api_client_x509 mac SET user_uuid_hex = (SELECT mpu.uuid_hex FROM mw_portal_user mpu WHERE ('CN='||username||',OU=Mt Wilson,O=Trusted Data Center,C=US') = mac.name);
   END LOOP;
END$$;


-- Updates for the OEM table
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


-- Updates for the OS table
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


-- Updates for the MLE table
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
-- Adds the reference to the OEM UUID column in the MLE table
ALTER TABLE mw_mle ADD COLUMN oem_uuid_hex CHAR(36) NULL;
DO
$$
DECLARE
    rec   record;
BEGIN
   FOR rec IN
      SELECT *
      FROM   mw_mle
   LOOP
      UPDATE mw_mle mw SET oem_uuid_hex = (SELECT moem.uuid_hex FROM mw_oem moem WHERE moem.ID = mm.OEM_ID);
   END LOOP;
END$$;
-- Adds the reference to the OS UUID column in the MLE table
ALTER TABLE mw_mle ADD COLUMN os_uuid_hex CHAR(36) NULL;
DO
$$
DECLARE
    rec   record;
BEGIN
   FOR rec IN
      SELECT *
      FROM   mw_mle
   LOOP
      UPDATE mw_mle mw SET os_uuid_hex = (SELECT mos.uuid_hex FROM mw_os mos WHERE mos.ID = mm.OS_ID);
   END LOOP;
END$$;


-- Updates for the MLE Source table
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
-- Adds the reference to the MLE UUID column in the MW_MLE_Source table
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


-- Updates for the PCR Manifest table
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
-- Adds the reference to the MLE UUID column in the MW_PCR_Manifest table
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


-- Updates for the Module Manifest table
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
-- Adds the reference to the MLE UUID column in the MW_Module_Manifest table
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


-- Updates for the Asset Tag Certificate table
ALTER TABLE mw_asset_tag_certificate ADD COLUMN uuid_hex CHAR(36) NULL;
DO
$$
DECLARE
    rec   record;
BEGIN
   FOR rec IN
      SELECT *
      FROM   mw_asset_tag_certificate
   LOOP
      UPDATE mw_asset_tag_certificate mw SET uuid_hex = (SELECT uuid_generate_v4()) where mw.id = rec.id;
   END LOOP;
END$$;


-- Updates for the Host table
ALTER TABLE mw_hosts ADD COLUMN uuid_hex CHAR(36) NULL;
DO
$$
DECLARE
    rec   record;
BEGIN
   FOR rec IN
      SELECT *
      FROM   mw_hosts
   LOOP
      UPDATE mw_hosts mh SET uuid_hex = (SELECT uuid_generate_v4()) where mh.id = rec.id;
   END LOOP;
END$$;
-- Adds the reference to the BIOS MLE UUID column in the Hosts table
ALTER TABLE mw_hosts ADD COLUMN bios_mle_uuid_hex CHAR(36) NULL;
DO
$$
DECLARE
    rec   record;
BEGIN
   FOR rec IN
      SELECT *
      FROM   mw_hosts
   LOOP
      UPDATE mw_hosts mh SET bios_mle_uuid_hex = (SELECT mm.uuid_hex FROM mw_mle mm WHERE mm.ID = mh.BIOS_MLE_ID);
   END LOOP;
END$$;
-- Adds the reference to the VMM MLE UUID column in the Hosts table
ALTER TABLE mw_hosts ADD COLUMN vmm_mle_uuid_hex CHAR(36) NULL;
DO
$$
DECLARE
    rec   record;
BEGIN
   FOR rec IN
      SELECT *
      FROM   mw_hosts
   LOOP
      UPDATE mw_hosts mh SET vmm_mle_uuid_hex = (SELECT mm.uuid_hex FROM mw_mle mm WHERE mm.ID = mh.VMM_MLE_ID);
   END LOOP;
END$$;


-- Updates for the MW_TA_Log table
ALTER TABLE mw_ta_log ADD COLUMN uuid_hex CHAR(36) NULL;
DO
$$
DECLARE
    rec   record;
BEGIN
   FOR rec IN
      SELECT *
      FROM   mw_ta_log
   LOOP
      UPDATE mw_ta_log mh SET uuid_hex = (SELECT uuid_generate_v4()) where mh.id = rec.id;
   END LOOP;
END$$;
-- Adds the reference to the HOST UUID column in the Hosts table
ALTER TABLE mw_ta_log ADD COLUMN host_uuid_hex CHAR(36) NULL;
DO
$$
DECLARE
    rec   record;
BEGIN
   FOR rec IN
      SELECT *
      FROM   mw_ta_log
   LOOP
      UPDATE mw_ta_log mtl SET host_uuid_hex = (SELECT mh.uuid_hex FROM mw_hosts mh WHERE mm.ID = mh.Host_ID);
   END LOOP;
END$$;


INSERT INTO mw_changelog (ID, APPLIED_AT, DESCRIPTION) VALUES (20140117160000,NOW(),'Added UUID fields for all the tables');
