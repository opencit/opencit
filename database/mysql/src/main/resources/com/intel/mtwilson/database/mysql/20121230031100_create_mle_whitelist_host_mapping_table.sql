-- This is a new table that would be used to store the host name that was used to create the white list for the MLE.

CREATE  TABLE `mw_mle_source` (
  `ID` INT NOT NULL AUTO_INCREMENT ,
  `MLE_ID` INT NOT NULL ,
  `Host_Name` VARCHAR(100) NULL ,
  PRIMARY KEY (`ID`) ,
  INDEX `MLE_ID` (`MLE_ID` ASC) ,
  CONSTRAINT `MLE_ID`
    FOREIGN KEY (`MLE_ID` )
    REFERENCES `mw_mle` (`ID` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;

INSERT INTO `mw_changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20121230031100,NOW(),'premium - Patch to create mw_mle_source table');
