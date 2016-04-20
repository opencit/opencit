/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.console.cmd;

import com.intel.dcsg.cpg.console.Command;
import com.intel.dcsg.cpg.console.input.Input;
import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.dcsg.cpg.i18n.LocaleUtil;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.shiro.jdbi.LoginDAO;
import org.apache.commons.configuration.Configuration;
import com.intel.mtwilson.crypto.password.PasswordUtil;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginPassword; // file.model.UserPassword;
import com.intel.mtwilson.user.management.rest.v2.model.RolePermission; // file.model.UserPermission;
import com.intel.mtwilson.shiro.jdbi.MyJdbi;
import com.intel.mtwilson.user.management.rest.v2.model.Role;
import com.intel.mtwilson.user.management.rest.v2.model.Status;
import com.intel.mtwilson.user.management.rest.v2.model.User;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginPasswordRole;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Usage examples: login-password username password --permissions domain:action
 * login-password username password --permissions domain1:action1
 * domain2:action2 domain3:action3 ... login-password username --nopass
 * --permissions domain:action login-password username --nopass --permissions
 * domain1:action1 domain2:action2 domain3:action3 ...
 *
 * @author jbuhacoff
 */
public class LoginPassword implements Command {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoginPassword.class);
//    private TrustagentConfiguration configuration;
    private Configuration options;

    @Override
    public void setOptions(Configuration options) {
        this.options = options;
    }

    protected boolean isEmptyPassword() {
        return options.getBoolean("nopass", false);
    }

    // never returns null but password may be empty (and that's allowed)
    private String getPassword(String[] args) throws IOException {
        String password;
        if (isEmptyPassword()) {
            password = "";
        } else if (args.length > 1) {
            password = args[1]; // always after username if present
            if (password == null || password.isEmpty()) {
                throw new IllegalArgumentException("Password is empty");
            }
            if (password.startsWith("env:") && password.length() > 4) {
                String variableName = password.substring(4);
                password = System.getenv(variableName);
                if (password == null || password.isEmpty()) {
                    throw new IllegalArgumentException(String.format("Environment variable %s does not contain a password", variableName));
                }
            }
        } else {
            password = Input.getConfirmedPasswordWithPrompt(String.format("Choose a password for %s", args[0])); // throws IOException, or always returns value or expression
            if (password == null || password.isEmpty()) {
                throw new IllegalArgumentException("Input password is empty");
            }
        }
        return password;
    }
    // get the 3rd arg if it's usrename passsword permissions, or the 2nd arg if it's username --nopass permissions

    private List<RolePermission> getPermissions(String[] args) {
        int i = 2; // login-password 0<username> 1<password> 2<permissions...>
        if (isEmptyPassword()) {
            i = 1;  //  login-password 0<username> 1<permissions...>
        }

        ArrayList<RolePermission> list = new ArrayList<>();
        for (; i < args.length; i++) {
            RolePermission rp = new RolePermission();
            String permissions = args[i];

            String[] parts = permissions.split(":");
            if (parts.length == 3) {
                rp.setPermitDomain(parts[0]);
                rp.setPermitAction(parts[1]);
                rp.setPermitSelection(parts[2]);
            } else if (parts.length == 2) {
                rp.setPermitDomain(parts[0]);
                rp.setPermitAction(parts[1]);
                rp.setPermitSelection("*");
            } else if (parts.length == 1) {
                rp.setPermitDomain(parts[0]);
                rp.setPermitAction("*");
                rp.setPermitSelection("*");
            } else {
                throw new IllegalArgumentException("Invalid permission format"); // must be in the form  domain:action:instance or domain:action or domain
            }
            list.add(rp);
        }
        return list;
    }

    @Override
    public void execute(String[] args) throws Exception {
        // store or replace the user record
        log.debug("Loading users and permissions");
        // usage:   username  (prompt for password, no permissions)
        // usage:   username password  (no permissions)
        // usage:   username password permissions
        // usage:   username --nopass  (no permissions)
        // usage:   username --nopass permissions
        // usage:   username --remove
        String username = args[0];

        if (options.getBoolean("remove", false)) {
            removeUser(username);
            removePermissions(username);
            log.debug("Removed username {}", username);
            return;
        }

        String password = getPassword(args); // never returns null but password may be empty 

        try (LoginDAO dao = MyJdbi.authz()) {
            // create the new user record
//        removeUser(username);
            User user = dao.findUserByName(username);
            if (user == null) {
                user = new User();
                user.setId(new UUID());
                //user.setComment("automatically created by setup");
                user.setUsername(username);
                dao.insertUser(user.getId(), user.getUsername(), null, ""); 
                log.info("Created user {}", username);
            } else {

                String localeTag = null;
                if (user.getLocale() != null)
                    localeTag = LocaleUtil.toLanguageTag(user.getLocale());
                
                dao.updateUser(user.getId(), localeTag, user.getComment());
                log.debug("Updated User: {}", username);
            }

            UserLoginPassword userLoginPassword = dao.findUserLoginPasswordByUsername(username);
            if (userLoginPassword == null) {
                userLoginPassword = new UserLoginPassword();
                userLoginPassword.setId(new UUID());
                userLoginPassword.setUserId(user.getId());
                userLoginPassword.setAlgorithm("SHA256");
                userLoginPassword.setIterations(1);
                userLoginPassword.setSalt(RandomUtil.randomByteArray(8));
                userLoginPassword.setPasswordHash(PasswordUtil.hash(password.getBytes(), userLoginPassword));
                userLoginPassword.setEnabled(true);
                userLoginPassword.setStatus(Status.APPROVED);
                userLoginPassword.setComment("Automatically created during setup.");
                dao.insertUserLoginPassword(userLoginPassword.getId(), userLoginPassword.getUserId(), userLoginPassword.getPasswordHash(),
                        userLoginPassword.getSalt(), userLoginPassword.getIterations(), userLoginPassword.getAlgorithm(), userLoginPassword.getExpires(),
                        userLoginPassword.isEnabled(), userLoginPassword.getStatus(), userLoginPassword.getComment());
                log.debug("Stored UserLoginPassword with ID: {}", userLoginPassword.getId());
            } else {
                userLoginPassword.setUserId(user.getId());
                userLoginPassword.setAlgorithm("SHA256");
                userLoginPassword.setIterations(1);
                userLoginPassword.setSalt(RandomUtil.randomByteArray(8));
                userLoginPassword.setPasswordHash(PasswordUtil.hash(password.getBytes(), userLoginPassword));
                userLoginPassword.setEnabled(true);
                userLoginPassword.setStatus(Status.APPROVED);
                userLoginPassword.setComment("Automatically created during setup.");
                dao.updateUserLoginPasswordWithUserId(userLoginPassword.getId(), userLoginPassword.getUserId(), userLoginPassword.getPasswordHash(),
                        userLoginPassword.getSalt(), userLoginPassword.getIterations(), userLoginPassword.getAlgorithm(), userLoginPassword.getExpires(),
                        userLoginPassword.isEnabled(), userLoginPassword.getStatus(), userLoginPassword.getComment());
                log.debug("Updated UserLoginPassword with ID: {}", userLoginPassword.getId());
            }
            log.debug("finding role by username");
            Role userRole = dao.findRoleByName(username);
            if (userRole == null) {
                log.debug("roles not found for username");
                userRole = new Role();
                userRole.setId(new UUID());
                userRole.setRoleName(username);
                userRole.setDescription("user created role");
                dao.insertRole(userRole.getId(), userRole.getRoleName(), userRole.getDescription());
                log.debug("Stored user role [{}] with ID: {}", username, userRole.getId());
            }
            log.debug("finding user login password roles by user login password id");
            List<UserLoginPasswordRole> userLoginPasswordRoles = dao.findUserLoginPasswordRolesByUserLoginPasswordId(userLoginPassword.getId());
            if (userLoginPasswordRoles == null || userLoginPasswordRoles.isEmpty()) {
                dao.insertUserLoginPasswordRole(userLoginPassword.getId(), userRole.getId());
            } else {
                // try to find the user's custom role in the list of existing roles
                List<String> userLoginPasswordRoleIds = getRoleUuidHexList(userLoginPasswordRoles);
                if (userLoginPasswordRoleIds.contains(userRole.getId().toHexString())) {
                    log.debug("user login password already linked to custom role");
                } else {
                    log.debug("Inserting user login password role id {} role id {}", userLoginPassword.getId(), userRole.getId());
                    dao.insertUserLoginPasswordRole(userLoginPassword.getId(), userRole.getId());
                }
            }

            log.debug("removing permissions");
            removePermissions(username);
            log.debug("getting permissions");
            List<RolePermission> newPermissions = getPermissions(args);
            for (RolePermission newPermission : newPermissions) {
                newPermission.setRoleId(userRole.getId());
                dao.insertRolePermission(newPermission.getRoleId(), newPermission.getPermitDomain(), newPermission.getPermitAction(), newPermission.getPermitSelection());
                log.debug("Stored permissions {}", newPermissions);
            }

        }
    }

    private void removeUser(String username) throws IOException, SQLException {
        try (LoginDAO dao = MyJdbi.authz()) {
            UserLoginPassword existingUserLoginPassword = dao.findUserLoginPasswordByUsername(username);
            if (existingUserLoginPassword != null) {
                dao.deleteUser(existingUserLoginPassword.getUserId());
                log.info("Deleted user {}", username);
            }
        }
    }

    private void removePermissions(String username) throws IOException, SQLException {
        try (LoginDAO dao = MyJdbi.authz()) {
            log.debug("finding existing user login password by username");
            UserLoginPassword existingUserLoginPassword = dao.findUserLoginPasswordByUsername(username);
            if (existingUserLoginPassword == null) {
                log.debug("No user found for username: {}", username);
                return;
            }
            log.debug("finding existing roles by user login password id");
            Set<UUID> roleUuidList = getRoleUuids(dao.findRolesByUserLoginPasswordId(existingUserLoginPassword.getId()));
            if (roleUuidList.isEmpty()) {
                log.debug("No role list found for user: {}", existingUserLoginPassword.getId());
                return;
            }
            log.debug("finding role permisiosn by password role ids {}", roleUuidList);
            List<RolePermission> existingPermissions = dao.findRolePermissionsByPasswordRoleIds(toStrings(roleUuidList));
            log.debug("got permissions {}", existingPermissions);
            for (RolePermission existingPermission : existingPermissions) {
                log.debug("deleting role permission");
                dao.deleteRolePermission(existingPermission.getRoleId(), existingPermission.getPermitDomain(), existingPermission.getPermitAction(), existingPermission.getPermitSelection());
                log.debug("Deleted role permission with role ID: {}, domain: {}, action: {}, selection: {}", existingPermission.getRoleId(), existingPermission.getPermitDomain(), existingPermission.getPermitAction(), existingPermission.getPermitSelection());
            }
        }
    }

    Set<UUID> getRoleUuids(List<Role> roles) {
        HashSet<UUID> uuids = new HashSet<>();
        for (Role role : roles) {
            uuids.add(role.getId());
        }
        return uuids;
    }

    List<String> getRoleUuidHexList(List<UserLoginPasswordRole> userLoginPasswordRoles) {
        ArrayList<String> ids = new ArrayList<>();
        for (UserLoginPasswordRole userLoginPasswordRole : userLoginPasswordRoles) {
            ids.add(userLoginPasswordRole.getRoleId().toHexString());
        }
        return ids;
    }

    // see also CreateAdminUser command
    private Set<String> toStrings(Set<UUID> uuids) {
        HashSet<String> set = new HashSet<>();
        for (UUID uuid : uuids) {
            set.add(uuid.toString());
        }
        return set;
    }
}
