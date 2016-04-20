

-- Convert the portal user table to contain password-protected keystore.
-- DROP TABLE `tbl_db_portal_user`;

-- This will support OpenStack integration, because OpenStack only allows
-- a generalized static login header most likely implemented as HTTP BASIC


-- This table supports single-signon across all the UI portals
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

INSERT INTO `mw_changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20120920085204,NOW(),'premium - patch for 1.1 add portal user table');

