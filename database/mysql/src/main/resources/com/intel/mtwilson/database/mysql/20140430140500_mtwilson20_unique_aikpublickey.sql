-- created 2014-04-30
-- rksavino
-- Makes aik_publickey a unique key in mw_hosts

ALTER TABLE mw_hosts ADD UNIQUE KEY mw_host_aik_public_key_unique (aik_publickey);
  
INSERT INTO `mw_changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20140430140500,NOW(),'Patch to make aik_publickey a unique key in mw_hosts.');