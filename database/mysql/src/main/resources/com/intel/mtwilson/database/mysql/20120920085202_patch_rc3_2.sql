-- created 2012-09-20


ALTER TABLE `tbl_api_client` RENAME TO `mw_api_client_hmac`;
ALTER TABLE `api_client_x509` RENAME TO `mw_api_client_x509`;
ALTER TABLE `api_role_x509` RENAME TO `mw_api_role_x509`;
ALTER TABLE `tbl_event_type` RENAME TO `mw_event_type`;
ALTER TABLE `tbl_location_pcr` RENAME TO `mw_location_pcr`;
ALTER TABLE `tbl_package_namespace` RENAME TO `mw_package_namespace`;
ALTER TABLE `tbl_module_manifest` RENAME TO `mw_module_manifest`;
ALTER TABLE `tbl_module_manifest_log` RENAME TO `mw_module_manifest_log`;
ALTER TABLE `tbl_saml_assertion` RENAME TO `mw_saml_assertion`;
ALTER TABLE `audit_log_entry` RENAME TO `mw_audit_log_entry`;

INSERT INTO `mw_changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20120920085202,NOW(),'premium - patch for 1.1 rename more tables');
