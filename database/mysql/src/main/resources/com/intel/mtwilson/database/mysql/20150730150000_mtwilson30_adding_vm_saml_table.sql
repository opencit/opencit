-- created 2015-07-30

-- This script creates the table to store the VM attestation report

CREATE  TABLE `mw_vm_attestation_report` (
  `id` CHAR(36) NOT NULL,
  `vm_instance_id` CHAR(36) NOT NULL,
  `vm_trust_status` BOOLEAN NOT NULL,
  `host_name` VARCHAR(255) NOT NULL,
  `vm_saml` TEXT DEFAULT NULL ,
  `vm_trust_report` TEXT DEFAULT NULL,
  `host_attestation_report` TEXT DEFAULT NULL,
  `error_code` varchar(50) DEFAULT NULL,
  `error_message` varchar(200) DEFAULT NULL,
  `created_ts` timestamp DEFAULT NULL,
  `expiry_ts` timestamp DEFAULT NULL,
  PRIMARY KEY (`id`)
  );
    
INSERT INTO `mw_changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20150730150000, NOW(), 'Patch for creating the table for storing vm saml data.');