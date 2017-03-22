/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * Author:  hmgowda
 * Created: Mar 15, 2017
 */


ALTER TABLE mw_hosts DROP COLUMN AIK_SHA1;
ALTER TABLE mw_hosts DROP COLUMN AIK_PUBLICKEY_SHA1;
ALTER TABLE mw_hosts ADD COLUMN AIK_SHA256 varchar(100) DEFAULT NULL;
ALTER TABLE mw_hosts ADD COLUMN AIK_PUBLICKEY_SHA256 varchar(100) DEFAULT NULL;

INSERT INTO mw_changelog (ID, APPLIED_AT, DESCRIPTION) VALUES (20170315154400, NOW(), 'Delete AIK_SHA1 and AIK_PUBLICKEY_SHA1 attributes and insert AIK_SHA256 and AIK_PUBLICKEY_SHA256');
