-- Adding the index on the componentname field since we do searches on it
CREATE INDEX idx_component_name on mw_module_manifest (componentname ASC);

-- Adding the index on the component digest field since we do searches on it while generating policies
CREATE INDEX idx_component_digest on mw_module_manifest (digestvalue ASC) ;

-- Adding the index on the pcr name field since we do searches on it
CREATE INDEX idx_pcr_name on mw_pcr_manifest (name ASC);

-- Adding the index on the pcr value field since we do searches on it while generating policies
CREATE INDEX idx_pcr_value on mw_pcr_manifest (value ASC);

-- Creating a combined index on Name and Version
CREATE INDEX idx_mle_name_version ON mw_mle (name ASC, version ASC);

-- Adding indexes on package name and vendor column. 
CREATE INDEX idx_package_namespace ON mw_package_namespace (name ASC, vendorname ASC);

-- We do the searches on the processor type and the cpuid
CREATE INDEX idx_processor_mapping ON mw_processor_mapping (processor_type ASC, processor_cpuid ASC);

-- The findByHostName is being called from 24 different locations in the code. So, adding an index to the name column.
CREATE INDEX idx_host_name ON mw_hosts (name ASC);

-- Creating a combined index on name and fieldname
CREATE INDEX idx_event_type ON mw_event_type (name ASC, fieldname ASC);

INSERT INTO mw_changelog (ID, APPLIED_AT, DESCRIPTION) VALUES (20131113015000,NOW(),'Patch for creating indexes for better performance.');