-- created 2013-01-08

-- This file contains the mw_location_pcr table, which was originally created as tbl_location_pcr and then renamed to mw_location_pcr
-- but due to a bug it may not be created by previous SQL scripts correctly

CREATE TABLE `mw_location_pcr` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `location` varchar(200) NOT NULL,
  `pcr_value` varchar(100) NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Mapping between the pcr values and location';


INSERT INTO `mw_changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20130108232900,NOW(),'premium - patch for 1.1 adding mw_location_pcr table');
