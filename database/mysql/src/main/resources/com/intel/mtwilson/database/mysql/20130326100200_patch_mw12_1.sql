-- created 2013-02-26

-- This file contains the mw_api_client_http_basic table, which will be used to store the user name & password used for supporting
-- HTTP Basic authentication

CREATE TABLE `mw_api_client_http_basic` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `user_name` varchar(45) NOT NULL,
  `password` varchar(45) NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `user_name_UNIQUE` (`user_name`));

INSERT INTO `mw_changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20130326100200,NOW(),'patch for 1.1 adding mw_api_client_http_basic');
