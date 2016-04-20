/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.console.cmd;

import com.intel.dcsg.cpg.console.Command;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.shiro.jdbi.LoginDAO;
import org.apache.commons.configuration.Configuration;
import com.intel.mtwilson.user.management.rest.v2.model.RolePermission; // file.model.UserPermission;
import com.intel.mtwilson.shiro.jdbi.MyJdbi;
import com.intel.mtwilson.user.management.rest.v2.model.Role;
import com.intel.mtwilson.user.management.rest.v2.model.Status;
import com.intel.mtwilson.user.management.rest.v2.model.User;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginCertificate;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginCertificateRole;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
/**
 * Usage examples:
 * mtwilson approve-user-login-certificate-request apiclient --roles administrator
 * mtwilson approve-user-login-certificate-request apiclient --roles administrator --permissions domain:action
 * mtwilson approve-user-login-certificate-request apiclient --roles administrator --permissions domain1:action1 domain2:action2 domain3:action3
 * mtwilson approve-user-login-certificate-request apiclient --permissions domain1:action1 domain2:action2 domain3:action3
 * @author jbuhacoff
 */
public class ApproveUserLoginCertificateRequest implements Command {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ApproveUserLoginCertificateRequest.class);
    private Configuration options;
    
    @Override
    public void setOptions(Configuration options) {
        this.options = options;
    }
            
    // get the 3rd arg if it's usrename roles permissions, or the 2nd arg if it's username permissions
    private List<RolePermission> getPermissions(String[] args) {
        ArrayList<RolePermission> list = new ArrayList<>();
        int i;
                
        if (options.containsKey("permissions")) {
            
            // System.out.println("User has specified additional permissions");

            // Possible calling options are
            // ApproveUserLoginCertificateRequest username --roles role1,role2 --permissions domain1:action1 domain2:action2
            // ApproveUserLoginCertificateRequest username --permissions domain1:action1 domain2:action2
            if (options.containsKey("roles")) {
                i = 2; // both roles and permissions are provided
            } else{
                i = 1; //only permissions are provided
            }

            // Since multiple permissions could be specified, need to process all of them.
            for( ; i<args.length; i++) {
                RolePermission rp = new RolePermission();
                String permissions = args[i];
                //System.out.println("Processing permission :" + permissions);
                String[] parts = permissions.split(":");
                if( parts.length == 3 ) {
                    rp.setPermitDomain(parts[0]);
                    rp.setPermitAction(parts[1]);
                    rp.setPermitSelection(parts[2]);
                } else if( parts.length == 2 ) {
                    rp.setPermitDomain(parts[0]);
                    rp.setPermitAction(parts[1]);
                    rp.setPermitSelection("*");
                } else if( parts.length == 1 ) {
                    rp.setPermitDomain(parts[0]);
                    rp.setPermitAction("*");
                    rp.setPermitSelection("*");
                } else {
                    throw new IllegalArgumentException("Invalid permission format"); // must be in the form  domain:action:instance or domain:action or domain
                }
                list.add(rp);
            }
        }
        return list;
    }
    
    /**
     * Returns back the list of roles that are validated to exist in the system out of the roles specified by the administrator
     * to be assigned to the user.
     * @param Sample of the argument: username --roles role1,role2 --permissions domain1:action1 domain2:action2
     * @return List of validated roles.
     * @throws Exception 
     */
    private List<Role> getRoles(String args[]) throws SQLException, IOException  {
        List<Role> requestedRoles = new ArrayList<>();
        String[] roles;
        if (options.containsKey("roles")) {
           roles = args[1].split(",");
            if (roles != null && roles.length > 0) {
                try (LoginDAO dao = MyJdbi.authz()) {
                    for (String role : roles) {
                        // Verify if the role name is valid
                        Role systemRole = dao.findRoleByName(role);
                        if (systemRole != null) {
                            requestedRoles.add(systemRole);
                            log.debug("Role {} would be added to the user", role);
                        }
                        else {
                            // System.out.println("Invalid role specified -" + role);
                            log.error("Role {} is not a valid role. It will not be assigned to the user", role);
                        }
                    }
                }
            }           
        }                
        return requestedRoles;
    }
    
    @Override
    public void execute(String[] args) throws Exception {
        // First the user specified should be verified to be in PENDING state.        
        String username = args[0];
        
        try(LoginDAO dao = MyJdbi.authz()) {
            User user = dao.findUserByName(username);
            if (user == null) {
                throw new IllegalArgumentException("Invalid user specified."); 
            }
            
            UserLoginCertificate userLoginCertificate = dao.findUserLoginCertificateByUserId(user.getId());
            if (userLoginCertificate != null) {
                if (userLoginCertificate.getStatus().equals(Status.APPROVED)) {
                    //System.out.println("User has already been approved");
                    throw new IllegalAccessException("User has already been approved");
                }
                
                // Approve the user
                userLoginCertificate.setStatus(Status.APPROVED);
                userLoginCertificate.setEnabled(true);
                dao.updateUserLoginCertificateById(userLoginCertificate.getId(), userLoginCertificate.isEnabled(), userLoginCertificate.getStatus(), userLoginCertificate.getComment());
                //System.out.println("Approved the user login certificate for user -" + username);
                log.debug("Approved the user login certificate for user {}", username);
                
                // Assign the list of role specified by the administrator
                List<Role> roles = getRoles(args);
                if (roles != null && roles.size() > 0) {
                    for (Role role : roles) {
                        UserLoginCertificateRole userCertRole = new UserLoginCertificateRole();
                        userCertRole.setLoginCertificateId(userLoginCertificate.getId());
                        userCertRole.setRoleId(role.getId());
                        dao.insertUserLoginCertificateRole(userCertRole.getLoginCertificateId(), userCertRole.getRoleId());
                        //System.out.println("Added role to user" + role.getRoleName() + "--" + username);
                        log.debug("Assigned role {} to user login certificate for user {}", role.getRoleName(), username);
                    }
                }
                
                List<RolePermission> rolePermissions = getPermissions(args);
                if (rolePermissions != null && rolePermissions.size() > 0) {
                    //System.out.println("About to process permission : " + rolePermissions.size());
                    String customRoleName = "UserRole:"+username;
                    Role customRole = dao.findRoleByName(customRoleName);
                    if (customRole == null) {
                        // For the set of permissions requested, a custom role would be created and associated with all the permissions.
                        customRole = new Role();
                        customRole.setId(new UUID());
                        customRole.setRoleName(customRoleName);
                        customRole.setDescription("Custom role created for user with admin specified permissions.");
                        dao.insertRole(customRole.getId(), customRole.getRoleName(), customRole.getDescription());
                        //System.out.println("Created custom role : " + customRoleName);
                        log.debug("Created a custom role {} for user {}.", customRole, username);
                    }
                    
                    for(RolePermission rolePermission : rolePermissions){
                        rolePermission.setId(new UUID());
                        dao.insertRolePermission(customRole.getId(), rolePermission.getPermitDomain(), rolePermission.getPermitAction(), rolePermission.getPermitSelection());
                        //System.out.println("Adding permission for custom role " + rolePermission.getPermitDomain() + ":"  + rolePermission.getPermitAction());
                        log.debug("Added permission {}.{}.{} to role {}.", rolePermission.getPermitDomain(), 
                                rolePermission.getPermitAction(), rolePermission.getPermitSelection(), customRole.getRoleName());
                    }
                    
                    // associate the custom role to the user
                    if (!isRoleAssociatedToUserLoginCertificate(dao, customRole, userLoginCertificate)) {
                        dao.insertUserLoginCertificateRole(userLoginCertificate.getId(), customRole.getId());
                    }
                    log.debug("Added the custom role {} to user login certificate for user {}", customRoleName, username);
                }
            }
        }
    }
    
    private boolean isRoleAssociatedToUserLoginCertificate(LoginDAO dao, Role role, UserLoginCertificate userLoginCertificate) {
        List<Role> roleList = dao.findRolesByUserLoginCertificateId(userLoginCertificate.getId());
        for (Role r : roleList) {
            if (r.getId().toString().equals(role.getId().toString())) {
                return true;
            }
        }
        return false;
    }
        
}
