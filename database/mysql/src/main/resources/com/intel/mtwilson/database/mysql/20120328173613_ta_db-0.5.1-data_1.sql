
INSERT INTO `tbl_db_portal_user` (`ID`, `Login`, `Password`, `First_Name`, `Last_Name`) VALUES (1, 'root','root','root','root');

INSERT INTO `tbl_event_type` (`ID`, `Name`, `FieldName`) VALUES (1,'Vim25Api.HostTpmSoftwareComponentEventDetails','componentName');
INSERT INTO `tbl_event_type` (`ID`, `Name`, `FieldName`) VALUES (2,'Vim25Api.HostTpmOptionEventDetails','bootOptions');
INSERT INTO `tbl_event_type` (`ID`, `Name`, `FieldName`) VALUES (3,'Vim25Api.HostTpmBootSecurityOptionEventDetails','bootSecurityOption');
INSERT INTO `tbl_event_type` (`ID`, `Name`, `FieldName`) VALUES (4,'Vim25Api.HostTpmCommandEventDetails','commandLine');
INSERT INTO `tbl_os` (`ID`, `NAME`, `VERSION`, `DESCRIPTION`) VALUES (11,'VMWare','5.0','');
INSERT INTO `tbl_package_namespace` (`ID`, `Name`, `VendorName`) VALUES (1,'Standard_Global_NS','VMware');

INSERT INTO `changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20120328173613,NOW(),'premium - create 0.5.1 data');
