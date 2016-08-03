/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * Author:  dczech
 * Created: Jul 28, 2016
 */

ALTER TABLE `mw_pcr_manifest` ADD COLUMN `pcr_bank` VARCHAR(20);

ALTER TABLE `mw_hosts` ADD COLUMN `pcr_bank` VARCHAR(20), ADD COLUMN `tpm_version` VARCHAR(20);

INSERT INTO `mw_changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20160728140000, NOW(), 'Add TPM 2.0 SHA256 support by adding tpm_version and pcr_bank fields');