/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * Author:  dczech
 * Created: Aug 5, 2016
 */

/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * Author:  dczech
 * Created: Aug 5, 2016
 */

ALTER TABLE `mw_module_manifest` ADD COLUMN `pcr_bank` varchar(20);

INSERT INTO `mw_changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20160805143100, NOW(), 'Add pcr_bank to mw_module_manifest');