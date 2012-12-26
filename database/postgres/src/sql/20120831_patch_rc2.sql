-- created 2012-08-31

-- fix bug #266
ALTER TABLE `tbl_mle` ADD CONSTRAINT `MLE_OEM_FK` FOREIGN KEY (`OEM_ID`) REFERENCES `tbl_oem` (`ID`);
ALTER TABLE `tbl_mle` ADD CONSTRAINT `MLE_OS_FK` FOREIGN KEY (`OS_ID`) REFERENCES `tbl_os` (`ID`);

-- plaintext values of AddOn_Connection_Info were 80 bytes in previous release. Now they are encrypted so we have to make it larger to allow for padding, IV, and base-64 encoding.
ALTER TABLE `tbl_hosts` MODIFY COLUMN `AddOn_Connection_Info` TEXT DEFAULT NULL;

-- Module manifest log supports the failure report feature
CREATE TABLE `tbl_module_manifest_log` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `ta_log_id` int(11) NOT NULL,
  `name` varchar(100) DEFAULT NULL,
  `value` varchar(100) DEFAULT NULL,
  `whitelist_value` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `TA_LOG_FK` (`ta_log_id`),
  CONSTRAINT `TA_LOG_FK` FOREIGN KEY (`ta_log_id`) REFERENCES `tbl_ta_log` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

ALTER TABLE audit_log_entry MODIFY COLUMN `data` MEDIUMTEXT ;
