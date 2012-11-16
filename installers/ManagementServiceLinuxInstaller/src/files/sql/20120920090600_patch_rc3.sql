-- created 2012-09-20

delimiter ;

-- Names of all database tables belonging to Mt Wilson are now prefixed with
-- "mw_" for consistency and to facilitate integration with other systems
-- ALTER TABLE `changelog` RENAME TO `mw_changelog`; -- already renamed in AS installer
-- ALTER TABLE `audit_log_entry` RENAME TO `mw_audit_log_entry`; -- already renamed in AS installer
ALTER TABLE `tbl_api_client` RENAME TO `mw_api_client_hmac`;
ALTER TABLE `api_client_x509` RENAME TO `mw_api_client_x509`;
ALTER TABLE `api_role_x509` RENAME TO `mw_api_role_x509`;

-- Convert the portal user table to contain password-protected keystore.
-- DROP TABLE `tbl_db_portal_user`;

CREATE TABLE `mw_portal_user` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(255) NOT NULL,
  `keystore` blob NOT NULL,
  `enabled` bit(1) NOT NULL DEFAULT b'0',
  `status` varchar(128) NOT NULL DEFAULT 'Pending',
  `comment` text NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `username_index` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

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

-- Extended configurationt table could have  labels (so a UI does not need to hard-code human-readable definitions), type specifiers (to indicate the serialized text format in which the setting is stored), and weight/order-preference (to indicate visual display order for the preferences)
--  `label` varchar(255) DEFAULT NULL,
--  `type` enum('text','integer','float','textarea','select','radio','checkbox') NOT NULL DEFAULT 'text',
--  `options` varchar(255) DEFAULT NULL,
--  `weight` int(11) DEFAULT '0',
-- Also if all values are encoded with some type then we won't have ambiguity. FOr example, encoding a string:  s:6:hello!    or use a specific JSON hash or array structure for encoding

INSERT INTO `mw_changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20120920090600,NOW(),'patch for rc3 by management service installer');

