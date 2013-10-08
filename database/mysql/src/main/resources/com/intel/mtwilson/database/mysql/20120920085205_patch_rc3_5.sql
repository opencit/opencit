
-- CREATE TABLE `api_client_hmac` (
--  `ID` int(11) NOT NULL AUTO_INCREMENT,
--  `name` varchar(255) NOT NULL,
--  `secret_key` blob NOT NULL,
--  `issuer` varchar(255) DEFAULT NULL,
--  `enabled` bit(1) NOT NULL DEFAULT b'0',
--  `status` varchar(128) NOT NULL DEFAULT 'Pending',
--  `comment` text,
--  PRIMARY KEY (`ID`),
--  UNIQUE KEY `fingerprint_index` (`fingerprint`)
-- ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
--
-- CREATE TABLE `api_role_hmac` (
--  `api_client_hmac_ID` int(11) NOT NULL,
--  `role` varchar(255) NOT NULL,
--  PRIMARY KEY (`api_client_hmac_ID`,`role`),
--  CONSTRAINT `api_role_hmac_ibfk_1` FOREIGN KEY (`api_client_hmac_ID`) REFERENCES `api_client_hmac` (`ID`)
-- ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `mw_configuration` (
  `key` varchar(255) NOT NULL,
  `value` text NOT NULL,
  `comment` text NULL,
  PRIMARY KEY (`key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `mw_changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20120920085205,NOW(),'premium - patch for 1.1 add configuration');

-- Extended configurationt table could have  labels (so a UI does not need to hard-code human-readable definitions), type specifiers (to indicate the serialized text format in which the setting is stored), and weight/order-preference (to indicate visual display order for the preferences)
--  `label` varchar(255) DEFAULT NULL,
--  `type` enum('text','integer','float','textarea','select','radio','checkbox') NOT NULL DEFAULT 'text',
--  `options` varchar(255) DEFAULT NULL,
--  `weight` int(11) DEFAULT '0',
-- Also if all values are encoded with some type then we won't have ambiguity. FOr example, encoding a string:  s:6:hello!    or use a specific JSON hash or array structure for encoding

