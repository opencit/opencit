-- created 2013-01-06

-- This file contains schema changes from Mt Wilson 1.0-RC2 to Mt Wilson 1.1

-- bug #497 allow administrator to save trusted vcenter ssl certificate and verify it on each connection
ALTER TABLE `mw_hosts` ADD COLUMN `TlsPolicy` varchar(255) NOT NULL DEFAULT 'TRUST_FIRST_CERTIFICATE';
ALTER TABLE `mw_hosts` ADD COLUMN `TlsKeystore` blob NULL;

INSERT INTO `mw_changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20130106235900,NOW(),'core - patch for 1.1 adding tls policy enforcement');
