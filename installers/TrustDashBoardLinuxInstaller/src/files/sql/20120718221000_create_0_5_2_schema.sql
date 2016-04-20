delimiter ;

CREATE TABLE `host_detail` (
  `Host_Detail_ID` mediumint(9) NOT NULL AUTO_INCREMENT,
  `Host_Name` varchar(100) DEFAULT NULL,
  `Host_IP_Address` varchar(20) DEFAULT NULL,
  `Host_Port` varchar(10) DEFAULT NULL,
  `Host_Description` varchar(200) DEFAULT NULL,
  `BIOS_Name` varchar(100) DEFAULT NULL,
  `BIOS_Build_No` varchar(100) DEFAULT NULL,
  `VMM_Name` varchar(100) DEFAULT NULL,
  `VMM_Build_No` varchar(100) DEFAULT NULL,
  `Created_On` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `Created_By` varchar(100) DEFAULT NULL,
  `Updated_On` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `Updated_By` varchar(100) DEFAULT NULL,
  `Is_Processed` char(1) DEFAULT NULL,
  `Email_Address` varchar(100) DEFAULT NULL,
  `VCenter_Details` varchar(200) DEFAULT NULL,
  `Location` varchar(200) DEFAULT NULL,
  `OEM` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`Host_Detail_ID`)
) ENGINE=InnoDB AUTO_INCREMENT=134 DEFAULT CHARSET=latin1;

delimiter $$

CREATE
TRIGGER `cloudportal`.`tbl_hostvmmapping_INSERT`
BEFORE INSERT ON `cloudportal`.`tbl_hostvmmapping`
FOR EACH ROW
-- Set the creation date

SET new.Created_Date = now(),
-- Set the udpate date     
new.Modified_Date = now()
$$

CREATE
TRIGGER `cloudportal`.`tbl_hostvmmapping_UPDATE`
BEFORE UPDATE ON `cloudportal`.`tbl_hostvmmapping`
FOR EACH ROW
Set new.Modified_Date = now()
$$

delimiter ;

CREATE TABLE `tbl_hostvmmapping` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `Host_Id` int(11) DEFAULT NULL,
  `VM_Name` varchar(100) DEFAULT NULL,
  `VM_Status` tinyint(4) NOT NULL DEFAULT '0',
  `Trusted_Host_Policy` tinyint(4) NOT NULL DEFAULT '0',
  `Location_Policy` tinyint(4) NOT NULL DEFAULT '0',
  `Created_By` varchar(45) DEFAULT 'admin',
  `Created_Date` datetime DEFAULT NULL,
  `Modified_By` varchar(45) DEFAULT 'admin',
  `Modified_Date` datetime DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=latin1;


delimiter ;

CREATE TABLE `tbl_mle` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `Name` varchar(50) NOT NULL COMMENT '           ',
  `Version` varchar(25) NOT NULL,
  `Attestation_Type` varchar(10) NOT NULL DEFAULT 'PCR',
  `MLE_Type` varchar(5) NOT NULL DEFAULT 'VMM',
  `Required_Manifest_List` varchar(100) DEFAULT NULL,
  `Description` varchar(45) DEFAULT NULL,
  `OsName` varchar(40) DEFAULT NULL,
  `OsVersion` varchar(40) DEFAULT NULL,
  `OemName` varchar(40) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `MLE_ID` (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=118 DEFAULT CHARSET=latin1;

