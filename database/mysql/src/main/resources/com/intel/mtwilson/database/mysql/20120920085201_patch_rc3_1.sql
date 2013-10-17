-- created 2012-09-20

-- the portal user table is now owned by management service: ALTER TABLE `tbl_db_portal_user` RENAME TO `mw_portal_user`;
-- the symmetric key api client table is now owned by maangement service: ALTER TABLE `tbl_api_client` RENAME TO `mw_api_client_hmac`;
ALTER TABLE `tbl_mle` RENAME TO `mw_mle`;
ALTER TABLE `tbl_hosts` RENAME TO `mw_hosts`;
ALTER TABLE `tbl_oem` RENAME TO `mw_oem`;
ALTER TABLE `tbl_os` RENAME TO `mw_os`;
ALTER TABLE `tbl_pcr_manifest` RENAME TO `mw_pcr_manifest`;
ALTER TABLE `tbl_request_queue` RENAME TO `mw_request_queue`;
ALTER TABLE `tbl_ta_log` RENAME TO `mw_ta_log`;
ALTER TABLE `tbl_host_specific_manifest` RENAME TO `mw_host_specific_manifest`;


INSERT INTO `mw_changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20120920085201,NOW(),'core - patch for 1.1 rename tables');

