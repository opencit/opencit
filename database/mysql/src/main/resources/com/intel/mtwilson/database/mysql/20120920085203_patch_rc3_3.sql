
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


CREATE TABLE `mw_keystore` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `keystore` blob NOT NULL,
  `provider` varchar(255),
  `comment` text,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `name_index` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `mw_changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20120920085203,NOW(),'premium - certificates and keystores');
