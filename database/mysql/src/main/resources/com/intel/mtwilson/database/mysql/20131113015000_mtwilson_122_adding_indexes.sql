-- Adding the index on the componentname field since we do searches on it
ALTER TABLE `mw_module_manifest` 
ADD INDEX `idx_component_name` (`ComponentName` ASC) ;

-- Adding the index on the component digest field since we do searches on it while generating policies
ALTER TABLE `mw_module_manifest` 
ADD INDEX `idx_component_digest` (`DigestValue` ASC) ;

-- Adding the index on the pcr name field since we do searches on it
ALTER TABLE `mw_pcr_manifest` 
ADD INDEX `idx_pcr_name` (`Name` ASC) ;

-- Adding the index on the pcr value field since we do searches on it while generating policies
ALTER TABLE `mw_pcr_manifest` 
ADD INDEX `idx_pcr_value` (`Value` ASC) ;

-- Dropping the unique index since there is a primary index already and
-- creating a combined index on Name and Version
ALTER TABLE `mw_mle` 
ADD INDEX `idx_mle_name_version` (`Name` ASC, `Version` ASC) 
, DROP INDEX `MLE_ID` ;

-- Adding indexes on package name and vendor column. Today we do our searches
-- on the package name only
ALTER TABLE `mw_package_namespace` 
ADD INDEX `idx_package_namespace` (`Name` ASC, `VendorName` ASC);

-- We do the searches on the processor type and the cpuid
ALTER TABLE `mw_processor_mapping` 
ADD INDEX `idx_processor_mapping` (`processor_type` ASC, `processor_cpuid` ASC);

-- The findByHostName is being called from 24 different locations in the code.
-- So, adding an index to the name column.
ALTER TABLE `mw_hosts` 
ADD INDEX `idx_host_name` (`Name` ASC) ;

-- Creating a combined index on name and fieldname
ALTER TABLE `mw_event_type` 
ADD INDEX `idx_event_type` (`Name` ASC, `FieldName` ASC) ;

INSERT INTO `mw_changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20131113015000,NOW(),'Patch for creating indexes for better performance.');



