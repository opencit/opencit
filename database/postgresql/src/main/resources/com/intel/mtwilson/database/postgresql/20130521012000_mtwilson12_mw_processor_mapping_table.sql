-- created 2013-05-21

-- This script creates the processor mapping table, which can be used to retrieve the platform generation provided either the processor name or CPUID

CREATE SEQUENCE mw_processor_mapping_serial;
CREATE  TABLE mw_processor_mapping (
  ID integer NOT NULL DEFAULT nextval('mw_processor_mapping_serial') ,
  platform_name VARCHAR(45) NOT NULL ,
  processor_type VARCHAR(45) NOT NULL ,
  processor_cpuid VARCHAR(45) NULL ,
  PRIMARY KEY (ID) );

INSERT INTO mw_processor_mapping (platform_name, processor_type, processor_cpuid) VALUES ('Romley', 'Sandybridge', 'A7 06 02');
INSERT INTO mw_processor_mapping (platform_name, processor_type, processor_cpuid) VALUES ('Romley', 'Sandybridge', 'D6 06 02');
INSERT INTO mw_processor_mapping (platform_name, processor_type, processor_cpuid) VALUES ('Romley', 'Sandybridge', 'D7 06 02');
INSERT INTO mw_processor_mapping (platform_name, processor_type, processor_cpuid) VALUES ('Romley', 'Ivybridge', 'A9 06 03');
INSERT INTO mw_processor_mapping (platform_name, processor_type, processor_cpuid) VALUES ('Thurley', 'Westmere', 'C2 06 02');
INSERT INTO mw_processor_mapping (platform_name, processor_type, processor_cpuid) VALUES ('Thurley', 'Westmere', 'F2 06 02');
INSERT INTO mw_processor_mapping (platform_name, processor_type, processor_cpuid) VALUES ('Thurley', 'Westmere', '52 06 02');
INSERT INTO mw_processor_mapping (platform_name, processor_type, processor_cpuid) VALUES ('Thurley', 'Westmere', '55 06 02');

INSERT INTO mw_changelog (ID, APPLIED_AT, DESCRIPTION) VALUES (20130521012000,NOW(),'Patch for creating the processor mapping table.');