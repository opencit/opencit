/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * Author:  hmgowda
 * Created: Mar 27, 2017
 */

ALTER TABLE mw_asset_tag_certificate ADD COLUMN SHA256_Hash bytea DEFAULT NULL;


INSERT INTO mw_changelog (ID, APPLIED_AT, DESCRIPTION) VALUES (2017032711200000, NOW(), 'update mw_asset_tag_certificate to add SHA256_Hash attribute');
