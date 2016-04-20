
-- Now that all the referenced columns are dropped, we can finally drop the tbl_db_portal_user table as well.
DROP TABLE tbl_db_portal_user;

INSERT INTO mw_changelog (ID, APPLIED_AT, DESCRIPTION) VALUES (20121226120202,NOW(),'premium - Patch for RC3 to drop old portal user table');

