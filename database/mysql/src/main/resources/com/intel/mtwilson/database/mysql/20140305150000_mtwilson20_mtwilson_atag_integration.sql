-- created 2014-03-05

-- This script creates the tables required for integrating asset tag with mt wilson

CREATE  TABLE `mw_host_tpm_password` (
  `id` CHAR(36) NOT NULL ,
  `password` TEXT NOT NULL ,
  `modifiedOn` DATETIME NOT NULL ,
  PRIMARY KEY (`id`) );
  
CREATE  TABLE `mw_tag_kvattribute` (
  `id` CHAR(36) NOT NULL ,
  `name` VARCHAR(255) NOT NULL ,
  `value` VARCHAR(255) NOT NULL ,
  PRIMARY KEY (`id`), 
  UNIQUE KEY (`name`, `value`));
 
CREATE  TABLE `mw_tag_selection` (
  `id` CHAR(36) NOT NULL ,
  `name` VARCHAR(255) NOT NULL ,
  `description` TEXT NULL,
  PRIMARY KEY (`id`) );
  
CREATE  TABLE `mw_tag_selection_kvattribute` (
  `id` CHAR(36) NOT NULL ,
  `selectionId` CHAR(36) NOT NULL ,
  `kvAttributeId` CHAR(36) NOT NULL ,
  PRIMARY KEY (`id`) );
  
CREATE  TABLE `mw_tag_certificate` (
  `id` CHAR(36) NOT NULL ,
  `certificate` BLOB NOT NULL ,
  `sha1` CHAR(40) NOT NULL ,
  `sha256` CHAR(64) NOT NULL ,
  `subject` VARCHAR(255) NOT NULL ,
  `issuer` VARCHAR(255) NOT NULL ,
  `notBefore` DATETIME NOT NULL ,
  `notAfter` DATETIME NOT NULL ,
  `revoked` BOOLEAN NOT NULL DEFAULT FALSE ,
  PRIMARY KEY (`id`) );
  
  CREATE  TABLE `mw_tag_certificate_request` (
  `id` CHAR(36) NOT NULL ,
  `subject` VARCHAR(255) NOT NULL ,
  `status` VARCHAR(255) NULL , 
  `content` BLOB NOT NULL,
  `contentType` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`id`) );
  
  -- need to drop earlier version of table mw_configuration from 20120920085200
  DROP TABLE mw_configuration;
  CREATE  TABLE `mw_configuration` (
  `id` CHAR(36) NOT NULL ,
  `name` VARCHAR(255) NOT NULL ,
  `content` TEXT NULL ,
  PRIMARY KEY (`id`) );
  
INSERT INTO `mw_changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20140305150000,NOW(),'Patch for creating the tables for migrating asset tag to mtwilson database.');