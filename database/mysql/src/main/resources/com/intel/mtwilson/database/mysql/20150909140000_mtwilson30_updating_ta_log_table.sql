-- created 2015-09-09

-- This script updates the size of the error field in the ta log table.

ALTER TABLE `mw_ta_log` MODIFY `error` VARCHAR(500);
    
INSERT INTO `mw_changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20150909140000, NOW(), 'Patch for updating the error field of the ta log table.');