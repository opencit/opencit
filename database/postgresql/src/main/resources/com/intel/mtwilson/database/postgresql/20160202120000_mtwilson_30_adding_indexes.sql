
CREATE INDEX idx_host_id on mw_host_specific_manifest (host_id ASC);
CREATE INDEX idx_module_manifest_id on mw_host_specific_manifest (module_manifest_id ASC);

CREATE INDEX idx_role_name on mw_role (role_name ASC);

CREATE INDEX idx_user_name on mw_user (username ASC);

CREATE INDEX idx_role_id_domain on mw_role_permission (role_id ASC, permit_domain ASC) ;
CREATE INDEX idx_role_id_domain_permit_action on mw_role_permission (role_id ASC, permit_domain ASC, permit_action ASC) ;
CREATE INDEX idx_role_id_permit_action on mw_role_permission (role_id ASC, permit_action ASC) ;
CREATE INDEX idx_role_id_domain_permit_action_selection on mw_role_permission (role_id ASC, permit_domain ASC, permit_action ASC, permit_selection ASC) ;

CREATE INDEX idx_saml_host_id on mw_saml_assertion (host_id ASC);
CREATE INDEX idx_saml_host_id_expiry on mw_saml_assertion (host_id ASC, expiry_ts ASC);
CREATE INDEX idx_saml_created_ts on mw_saml_assertion (created_ts ASC);
CREATE INDEX idx_saml_expiry_ts on mw_saml_assertion (expiry_ts ASC);

CREATE INDEX idx_json_host_id on mw_ta_log (host_id ASC);
CREATE INDEX idx_json_host_id on mw_ta_log (host_id ASC);

CREATE INDEX idx_asset_tag_host_id on mw_asset_tag_certificate (host_id ASC);
