-- created 2012-08-29

CREATE TABLE `audit_log_entry` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `transaction_id` varchar(50) NULL,
  `entity_id` int(11) NULL,
  `entity_type` varchar(150) NULL,
  `finger_print` varchar(200) NULL,
  `create_dt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `action` varchar(50) NULL,
  `data` text NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


INSERT INTO `changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20120829000000,NOW(),'premium - audit_log');
