-- created 2012-08-31

-- fix bug #266
ALTER TABLE `tbl_mle` ADD CONSTRAINT `MLE_OEM_FK` FOREIGN KEY (`OEM_ID`) REFERENCES `tbl_oem` (`ID`);
ALTER TABLE `tbl_mle` ADD CONSTRAINT `MLE_OS_FK` FOREIGN KEY (`OS_ID`) REFERENCES `tbl_os` (`ID`);

-- plaintext values of AddOn_Connection_Info were 80 bytes in previous release. Now they are encrypted so we have to make it larger to allow for padding, IV, and base-64 encoding.
ALTER TABLE `tbl_hosts` MODIFY COLUMN `AddOn_Connection_Info` TEXT DEFAULT NULL;

INSERT INTO `changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20120831000000,NOW(),'core - patch rc2');
