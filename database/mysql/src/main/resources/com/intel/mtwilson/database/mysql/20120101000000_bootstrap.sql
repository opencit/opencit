
-- first thing we create is the changelog
CREATE TABLE `changelog` (
  `ID` decimal(20,0) NOT NULL,
  `APPLIED_AT` varchar(25) NOT NULL,
  `DESCRIPTION` varchar(255) NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- backdate an entry for the bootstrap sql file since the changelog didn't exist then;   
INSERT INTO `changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20120101000000,NOW(),'core - bootstrap');

-- Spring's ResourceDatabasePopulator does not recognize the "delimiter" command
-- and requires the delimiter to be semicolon
--delimiter ;
