-- created 2015-03-05

-- This script creates the table to store the measurement xml whitelist from OpenSource hosts

CREATE  TABLE `mw_measurement_xml` (
  `id` CHAR(36) NOT NULL,
  `mleId` INT(11) NOT NULL ,
  `content` TEXT DEFAULT NULL ,
  PRIMARY KEY (`id`)
  CONSTRAINT `measurement_xml_mleId` FOREIGN KEY (`mleId`) REFERENCES `mw_mle` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
  );
    
INSERT INTO `mw_changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20150305150000, NOW(), 'Patch for creating the table for storing the measurement xml whitelist.');