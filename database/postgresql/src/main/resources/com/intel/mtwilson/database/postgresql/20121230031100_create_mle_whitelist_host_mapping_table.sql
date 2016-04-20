-- This is a new table that would be used to store the host name that was used to create the white list for the MLE.
CREATE SEQUENCE mw_mle_source_serial;
CREATE  TABLE mw_mle_source (
  ID integer NOT NULL DEFAULT nextval('mw_mle_source_serial'),
  MLE_ID integer NOT NULL ,
  Host_Name VARCHAR(100) NULL ,
  PRIMARY KEY (ID) ,
  --INDEX MLE_ID (MLE_ID ASC) ,
  CONSTRAINT MLE_ID
    FOREIGN KEY (MLE_ID )
    REFERENCES mw_mle (ID )
    ON DELETE CASCADE
    ON UPDATE CASCADE
);

INSERT INTO mw_changelog (ID, APPLIED_AT, DESCRIPTION) VALUES (20121230031100,NOW(),'Patch to create mw_mle_source table');
