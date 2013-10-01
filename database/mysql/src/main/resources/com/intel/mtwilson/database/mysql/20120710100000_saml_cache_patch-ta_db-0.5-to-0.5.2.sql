CREATE TABLE `tbl_saml_assertion` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `host_id` int(11) NOT NULL,
  `saml` text,
  `expiry_ts` datetime NOT NULL,
  `bios_trust` tinyint(1) NOT NULL,
  `vmm_trust` tinyint(1) NOT NULL,
  `error_code` varchar(50) DEFAULT NULL,
  `error_message` varchar(200) DEFAULT NULL,
  `created_ts` datetime DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `tbl_hosts_fk` (`host_id`),
  CONSTRAINT `tbl_hosts_fk` FOREIGN KEY (`host_id`) REFERENCES `tbl_hosts` (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=latin1 COMMENT='SAML assertion cache';

INSERT INTO `changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20120710100000,NOW(),'premium - saml cache patch 0.5 to 0.5.2');
