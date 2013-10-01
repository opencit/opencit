
CREATE TABLE `mw_request_log` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `instance` varchar(255) NOT NULL,
  `received` datetime NOT NULL,
  `source` varchar(255) NOT NULL,
  `content` TEXT NOT NULL,
  `md5_hash` binary(16) NOT NULL,
  PRIMARY KEY (`ID`));

INSERT INTO `mw_changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20130427142800,NOW(),'premium - request log');
