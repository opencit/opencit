-- created 2012-09-20

-- Names of all database tables belonging to Mt Wilson are now prefixed with
-- "mw_" for consistency and to facilitate integration with other systems
ALTER TABLE `changelog` RENAME TO `mw_changelog`;
-- the portal user table is now owned by management service: ALTER TABLE `tbl_db_portal_user` RENAME TO `mw_portal_user`;
-- the symmetric key api client table is now owned by maangement service: ALTER TABLE `tbl_api_client` RENAME TO `mw_api_client_hmac`;
ALTER TABLE `tbl_event_type` RENAME TO `mw_event_type`;
ALTER TABLE `tbl_location_pcr` RENAME TO `mw_location_pcr`;
ALTER TABLE `tbl_mle` RENAME TO `mw_mle`;
ALTER TABLE `tbl_hosts` RENAME TO `mw_hosts`;
ALTER TABLE `tbl_oem` RENAME TO `mw_oem`;
ALTER TABLE `tbl_os` RENAME TO `mw_os`;
ALTER TABLE `tbl_package_namespace` RENAME TO `mw_package_namespace`;
ALTER TABLE `tbl_pcr_manifest` RENAME TO `mw_pcr_manifest`;
ALTER TABLE `tbl_request_queue` RENAME TO `mw_request_queue`;
ALTER TABLE `tbl_ta_log` RENAME TO `mw_ta_log`;
ALTER TABLE `tbl_module_manifest` RENAME TO `mw_module_manifest`;
ALTER TABLE `tbl_module_manifest_log` RENAME TO `mw_module_manifest_log`;
ALTER TABLE `tbl_host_specific_manifest` RENAME TO `mw_host_specific_manifest`;
ALTER TABLE `tbl_saml_assertion` RENAME TO `mw_saml_assertion`;
ALTER TABLE `audit_log_entry` RENAME TO `mw_audit_log_entry`;

-- No longer using the "portal user" table
-- DROP TABLE `mw_portal_user`;
-- INSERT INTO `mw_changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20120920085201,NOW(),'dropped mw_portal_user');

-- The certificate table holds signing certificates for distribution to clients, and holds Privacy CA certificates for validating AIK's
CREATE TABLE `mw_certificate_x509` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `certificate` blob NOT NULL,
  `md5_hash` binary(16) NOT NULL,
  `sha1_hash` binary(20) NOT NULL,
  `purpose` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `issuer` varchar(255) DEFAULT NULL,
  `expires` datetime DEFAULT NULL,
  `enabled` bit(1) NOT NULL DEFAULT b'0',
  `status` varchar(128) NOT NULL DEFAULT 'Pending',
  `comment` text,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `sha1_hash_index` (`sha1_hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- The keystore table holds SAML signing key so that a cluster of Mt Wilson servers can share the key
CREATE TABLE `mw_keystore` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `keystore` blob NOT NULL,
  `provider` varchar(255) NOT NULL,
  `comment` text,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `name_index` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `mw_changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20120920085200,NOW(),'patch for rc3 by attestation service');

