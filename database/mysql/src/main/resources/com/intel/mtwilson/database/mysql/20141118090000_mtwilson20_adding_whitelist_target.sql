-- created 2014-11-18
-- ssbangal
-- Adds the WhiteList target type and target value columns to the MLE table


-- Updates for the MLE table
ALTER TABLE `mw_mle` ADD COLUMN `target_type` VARCHAR(10) NULL;
ALTER TABLE `mw_mle` ADD COLUMN `target_value` VARCHAR(255) NULL;

INSERT INTO `mw_changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20141118090000,NOW(),'Added WhiteList target fields to the MLE table');
