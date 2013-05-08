

-- should move to its own file, it's not really a bootstrap item
CREATE TABLE `api_client_x509` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `certificate` blob NOT NULL,
  `fingerprint` binary(32) NOT NULL,
  `issuer` varchar(255) DEFAULT NULL,
  `serial_number` int(11) DEFAULT NULL,
  `expires` datetime DEFAULT NULL,
  `enabled` bit(1) NOT NULL DEFAULT b'0',
  `status` varchar(128) NOT NULL DEFAULT 'Pending',
  `comment` text,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `fingerprint_index` (`fingerprint`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `api_role_x509` (
  `api_client_x509_ID` int(11) NOT NULL,
  `role` varchar(255) NOT NULL,
  PRIMARY KEY (`api_client_x509_ID`,`role`),
  CONSTRAINT `api_role_x509_ibfk_1` FOREIGN KEY (`api_client_x509_ID`) REFERENCES `api_client_x509` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20120101000001,NOW(),'premium - bootstrap api client');

