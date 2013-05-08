-- the following are ok
INSERT INTO `tbl_oem` (`ID`, `NAME`, `DESCRIPTION`) VALUES (5,'GENERIC','Default Oem for testing');
INSERT INTO `tbl_oem` (`ID`, `NAME`, `DESCRIPTION`) VALUES (8,'EPSD','Intel white boxes');
INSERT INTO `tbl_oem` (`ID`, `NAME`, `DESCRIPTION`) VALUES (9,'HP','HP Systems');
INSERT INTO `tbl_os` (`ID`, `NAME`, `VERSION`, `DESCRIPTION`) VALUES (7,'RHEL','6.1',NULL);
INSERT INTO `tbl_os` (`ID`, `NAME`, `VERSION`, `DESCRIPTION`) VALUES (8,'RHEL','6.2',NULL);
INSERT INTO `tbl_os` (`ID`, `NAME`, `VERSION`, `DESCRIPTION`) VALUES (9,'UBUNTU','11.10',NULL);
INSERT INTO `tbl_os` (`ID`, `NAME`, `VERSION`, `DESCRIPTION`) VALUES (10,'SUSE','11 P2',NULL);

INSERT INTO `changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20120328173612,NOW(),'core - create 0.5.1 data');
