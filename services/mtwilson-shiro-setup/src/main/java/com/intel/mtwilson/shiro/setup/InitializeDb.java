/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.setup;

import com.intel.mtwilson.user.management.rest.v2.model.RolePermission;
import com.intel.mtwilson.user.management.rest.v2.model.Role;
import com.intel.mtwilson.setup.DatabaseSetupTask;
import com.intel.mtwilson.shiro.jdbi.LoginDAO;
import com.intel.mtwilson.shiro.jdbi.MyJdbi;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * we do not store the admin username or password in configuration - the application
 * must display them to the administrator
 * 
 * @author jbuhacoff
 */
public class InitializeDb extends DatabaseSetupTask {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(InitializeDb.class);
    public static final String ADMINISTRATOR_ROLE = "administrator";
    public static final String AUDITOR_ROLE = "auditor";
    public static final String ASSET_TAG_MANAGER_ROLE = "asset_tag_manager";
    public static final String WHITELIST_MANAGER_ROLE = "whitelist_manager";
    public static final String HOST_MANAGER_ROLE = "host_manager";
    public static final String CHALLENGER_ROLE = "challenger";
    public static final String USER_MANAGER_ROLE = "user_manager";
    public static final String REPORT_MANAGER_ROLE = "report_manager";
    public static final String SERVER_MANAGER_ROLE = "server_manager";
    public static final String TLS_POLICY_MANAGER_ROLE = "tls_policy_manager";
    // Below roles for backward compatibility
    public static final String SECURITY_ROLE = "Security";
    public static final String WHITE_LIST_ROLE = "Whitelist";
    public static final String ATTESTATION_ROLE = "Attestation";
    public static final String REPORT_ROLE = "Report";
    public static final String AUDIT_ROLE = "Audit";
    public static final String ASSET_TAG_MANAGEMENT_ROLE = "AssetTagManagement";
    

    
    @Override
    protected void configure() throws Exception {
        try (Connection c = My.jdbc().connection()) {
            // data migrated from these mtwilson 1.2 tables:
            requireTable(c, "mw_role");
            requireTable(c, "mw_role_permission");
        }
    }

    /**
     * Provided the role name and its associated permissions, this function will initialize the database with those records.
     * @param loginDAO
     * @param roleName
     * @param roleDesc
     * @param domainActions 
     */
    private void createRoleAndAssociatedPermissions(LoginDAO loginDAO, String roleName, String roleDesc, HashMap<String, String> domainActions) {
        Role role = loginDAO.findRoleByName(roleName);
        if (role == null) {
            role = new Role();
            role.setId(new UUID());
            role.setRoleName(roleName);
            role.setDescription(roleDesc);
            loginDAO.insertRole(role.getId(), role.getRoleName(), role.getDescription());
        }
        
        // Create the associated permissions in the mw_role_permission table
        for(Map.Entry<String, String> entry : domainActions.entrySet()) {
            String domain = entry.getKey();
            String actions = entry.getValue();
            
            RolePermission rolePerm = loginDAO.findAllRolePermissionsForRoleIdDomainActionAndSelection(role.getId(), domain, actions, "*");
            if (rolePerm == null) {
                rolePerm = new RolePermission();
                rolePerm.setRoleId(role.getId());
                rolePerm.setPermitDomain(domain);
                rolePerm.setPermitAction(actions);
                rolePerm.setPermitSelection("*"); // Since we are currently not using this, we will set it to *
                loginDAO.insertRolePermission(rolePerm.getRoleId(), rolePerm.getPermitDomain(), rolePerm.getPermitAction(), rolePerm.getPermitSelection());
            }            
        }
    }
    
    /**
     * This function returns the list of default permissions for the specified role.
     * @param roleName
     * @return 
     */
    private HashMap<String, String> createDomainActionListForRole(String roleName) {
        HashMap<String, String> domainActions = new HashMap<>();
        
        if (roleName.equalsIgnoreCase(ADMINISTRATOR_ROLE) || roleName.equalsIgnoreCase(SECURITY_ROLE)) {

            domainActions.put("*", "*");
            
        } else if (roleName.equalsIgnoreCase(ASSET_TAG_MANAGER_ROLE) || roleName.equalsIgnoreCase(ASSET_TAG_MANAGEMENT_ROLE)) {
            
            domainActions.put("tag_certificates", "*");
            domainActions.put("tag_certificate_requests", "*");
            domainActions.put("configurations", "search,retrieve");
            domainActions.put("tag_kv_attributes", "*");
            domainActions.put("tag_selections", "*");
            domainActions.put("tag_selection_kv_attributes", "*");
            domainActions.put("files", "search,retrieve");
            domainActions.put("tpm_passwords", "*");
            domainActions.put("host_uuids", "*");
            domainActions.put("hosts", "search");
            
        } else if (roleName.equalsIgnoreCase(HOST_MANAGER_ROLE) || roleName.equalsIgnoreCase(ATTESTATION_ROLE)) {
            
            domainActions.put("hosts", "*");
            domainActions.put("host_aik_certificates", "*");
            domainActions.put("host_aiks", "*");
            domainActions.put("host_attestations", "*");
            domainActions.put("host_tls_certificates", "*");
//            domainActions.put("poll_hosts", "*"); // use host_attestations:search,retrieve instead
            domainActions.put("oems", "search,retrieve");
            domainActions.put("oss", "search,retrieve");
            domainActions.put("mles", "search,retrieve");
            domainActions.put("mle_pcrs", "search,retrieve");
            domainActions.put("mle_modules", "search,retrieve");
            domainActions.put("mle_sources", "search,retrieve");
            domainActions.put("tag_certificates", "search,retrieve");
            domainActions.put("configurations", "search,retrieve");
            domainActions.put("files", "search,retrieve");
            domainActions.put("host_tls_policies", "search,retrieve");
            
        } else if (roleName.equalsIgnoreCase(AUDITOR_ROLE) || roleName.equalsIgnoreCase(AUDIT_ROLE)) {
            
            domainActions.put("*", "search,retrieve");
            //domainActions.put("audit_logs", "*");
            
        } else if (roleName.equalsIgnoreCase(REPORT_MANAGER_ROLE) || roleName.equalsIgnoreCase(REPORT_ROLE)) {
            
            domainActions.put("*", "search,retrieve");

        } else if (roleName.equalsIgnoreCase(WHITELIST_MANAGER_ROLE) || roleName.equalsIgnoreCase(WHITE_LIST_ROLE)) {

            domainActions.put("oems", "*");
            domainActions.put("oss", "*");
            domainActions.put("mles", "*");
            domainActions.put("mle_pcrs", "*");
            domainActions.put("mle_modules", "*");
            domainActions.put("mle_sources", "*");
            domainActions.put("host_tls_policies", "search,retrieve");
            
        } else if (roleName.equalsIgnoreCase(CHALLENGER_ROLE) || roleName.equalsIgnoreCase(ATTESTATION_ROLE)) {
            
            domainActions.put("hosts", "search,retrieve");
            domainActions.put("host_attestations", "*");
//            domainActions.put("poll_hosts", "*"); // use host_attestations:search,retrieve instead
            domainActions.put("oems", "search,retrieve");
            domainActions.put("oss", "search,retrieve");
            domainActions.put("mles", "search,retrieve");
            domainActions.put("mle_pcrs", "search,retrieve");
            domainActions.put("mle_modules", "search,retrieve");
            domainActions.put("mle_sources", "search,retrieve");
            domainActions.put("host_tls_policies", "search,retrieve");

        } else if (roleName.equalsIgnoreCase(USER_MANAGER_ROLE)) {

            domainActions.put("users", "*");
            domainActions.put("user_keystores", "*");
            domainActions.put("user_login_certificates", "*");
            domainActions.put("user_login_certificate_roles", "*");
            domainActions.put("user_login_hmacs", "*");
            domainActions.put("user_login_hmac_roles", "*");
            domainActions.put("user_login_passwords", "*");
            domainActions.put("user_login_password_roles", "*");
            domainActions.put("user_certificates", "*");
            
        } else if (roleName.equalsIgnoreCase(SERVER_MANAGER_ROLE)) {

            domainActions.put("configurations", "*");
            domainActions.put("files", "*");
            domainActions.put("roles", "*");
            domainActions.put("role_permissions", "*");
            
        } else if (roleName.equalsIgnoreCase(TLS_POLICY_MANAGER_ROLE)) {
            
            domainActions.put("host_tls_policies", "*");
        }       
        
        return domainActions;
    }



    @Override
    protected void execute() throws Exception {
        try (LoginDAO loginDAO = MyJdbi.authz()) {
            
            createRoleAndAssociatedPermissions(loginDAO, ADMINISTRATOR_ROLE, "", createDomainActionListForRole(ADMINISTRATOR_ROLE));
            createRoleAndAssociatedPermissions(loginDAO, AUDITOR_ROLE, "", createDomainActionListForRole(AUDITOR_ROLE));
            createRoleAndAssociatedPermissions(loginDAO, ASSET_TAG_MANAGER_ROLE, "", createDomainActionListForRole(ASSET_TAG_MANAGER_ROLE));
            createRoleAndAssociatedPermissions(loginDAO, WHITELIST_MANAGER_ROLE, "", createDomainActionListForRole(WHITELIST_MANAGER_ROLE));
            createRoleAndAssociatedPermissions(loginDAO, HOST_MANAGER_ROLE, "", createDomainActionListForRole(HOST_MANAGER_ROLE));
            createRoleAndAssociatedPermissions(loginDAO, CHALLENGER_ROLE, "", createDomainActionListForRole(CHALLENGER_ROLE));
            createRoleAndAssociatedPermissions(loginDAO, REPORT_MANAGER_ROLE, "", createDomainActionListForRole(REPORT_MANAGER_ROLE));
            createRoleAndAssociatedPermissions(loginDAO, USER_MANAGER_ROLE, "", createDomainActionListForRole(USER_MANAGER_ROLE));
            createRoleAndAssociatedPermissions(loginDAO, SERVER_MANAGER_ROLE, "", createDomainActionListForRole(SERVER_MANAGER_ROLE));
            createRoleAndAssociatedPermissions(loginDAO, TLS_POLICY_MANAGER_ROLE, "", createDomainActionListForRole(TLS_POLICY_MANAGER_ROLE));

            createRoleAndAssociatedPermissions(loginDAO, SECURITY_ROLE, "This role is for backward compatibility with MTW 1.x", createDomainActionListForRole(SECURITY_ROLE));
            createRoleAndAssociatedPermissions(loginDAO, WHITE_LIST_ROLE, "This role is for backward compatibility with MTW 1.x", createDomainActionListForRole(WHITE_LIST_ROLE));
            createRoleAndAssociatedPermissions(loginDAO, ATTESTATION_ROLE, "This role is for backward compatibility with MTW 1.x", createDomainActionListForRole(ATTESTATION_ROLE));
            createRoleAndAssociatedPermissions(loginDAO, REPORT_ROLE, "This role is for backward compatibility with MTW 1.x", createDomainActionListForRole(REPORT_ROLE));
            createRoleAndAssociatedPermissions(loginDAO, AUDIT_ROLE, "This role is for backward compatibility with MTW 1.x", createDomainActionListForRole(AUDIT_ROLE));
            createRoleAndAssociatedPermissions(loginDAO, ASSET_TAG_MANAGEMENT_ROLE, "This role is for backward compatibility with MTW 1.x", createDomainActionListForRole(ASSET_TAG_MANAGEMENT_ROLE));
            
        }
    }

    @Override
    protected void validate() throws Exception {
        return;
    }
}
