-- created 2014-02-05

-- ???

CREATE TABLE `mw_rpc` (
  `ID` varchar(36) DEFAULT NULL,
  `Name` varchar(200) DEFAULT NULL,
  `Input` blob DEFAULT NULL,
  `Output` blob DEFAULT NULL,
  `Status` varchar(200) DEFAULT NULL,
  `ProgressCurrent` int DEFAULT NULL,
  `ProgressMax` int DEFAULT NULL,
  PRIMARY KEY (`ID`)
);

INSERT INTO `mw_changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20140201124500,NOW(),'Mt Wilson 2.0 - added RPC table for mysql');
