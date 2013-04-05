
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


INSERT INTO `changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20120831000001,NOW(),'premium - patch rc2 module manifest and audit log entry');
