/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.setup;

import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.dcsg.cpg.io.Platform;
import com.intel.mtwilson.MyFilesystem;
import com.intel.mtwilson.setup.LocalSetupTask;
import com.intel.mtwilson.shiro.authc.password.PasswordCredentialsMatcher;
import com.intel.mtwilson.shiro.jdbi.LoginDAO;
import com.intel.mtwilson.shiro.jdbi.MyJdbi;
import com.intel.mtwilson.shiro.jdbi.model.*;
import java.io.File;
import java.util.List;
import org.apache.commons.io.FileUtils;
import com.intel.dcsg.cpg.io.UUID;
import java.util.ArrayList;
import java.util.Locale;
import org.apache.commons.codec.binary.Hex;

/**
 *
 * @author jbuhacoff
 */
public class CreateAdminUser extends LocalSetupTask {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CreateAdminUser.class);

    public static final String ADMINISTRATOR_ROLE = "administrator"; // TODO: move this to mtwilson-shiro-util ?
    private String username;
    private String password;
    private boolean isNewPassword = false;

    private String getUsername() {
        if (System.getenv("MC_FIRST_USERNAME") != null) {
            return System.getenv("MC_FIRST_USERNAME");
        }
        return "admin";
    }

    private String getPassword() {
        if (System.getenv("MC_FIRST_PASSWORD") != null) {
            return System.getenv("MC_FIRST_PASSWORD");
        }
        isNewPassword = true;
        return RandomUtil.randomBase64String(8).replace("=", "_"); // TODO INSECURE: use a larger alphabet with more special characters
    }

    @Override
    protected void configure() throws Exception {
        username = getUsername();
        password = getPassword();
    }

    @Override
    protected void validate() throws Exception {
        // ensure we have an admin user created with permissions assigned
        try (LoginDAO loginDAO = MyJdbi.authz()) {
            UserLoginPassword userLoginPassword = loginDAO.findUserLoginPasswordByUsername(username);
            if (userLoginPassword == null) {
                validation("User does not exist: %s", username);
                return;
            }
            List<Role> roles = loginDAO.findRolesByUserLoginPasswordId(userLoginPassword.getId());
            ArrayList<UUID> roleIds = new ArrayList<>();
            for (Role role : roles) {
                roleIds.add(role.getId());
            }
            List<RolePermission> rolePermissions = loginDAO.findRolePermissionsByPasswordRoleIds(roleIds);
            if (rolePermissions == null || rolePermissions.isEmpty()) {
                validation("User does not have permissions assigned: %s", username);
            }
            boolean isAdminPermissionAssigned = false;
            for(RolePermission rolePermission : rolePermissions) {
                if(rolePermission.getPermitDomain().equals("*") && rolePermission.getPermitAction().equals("*") && rolePermission.getPermitSelection().equals("*")) {
                    isAdminPermissionAssigned = true;
                }
            }
            if( !isAdminPermissionAssigned ) {
                validation("User does not have admin permissions assigned: %s", username);
            }
        }
    }

    @Override
    protected void execute() throws Exception {
        try (LoginDAO loginDAO = MyJdbi.authz()) {
            User user = loginDAO.findUserByName(username);
            if (user == null) {
                user = new User();
                user.setId(new UUID());
                user.setComment("automatically created by setup");
                user.setEnabled(true);
                user.setStatus(Status.APPROVED);
                user.setUsername(username);
                loginDAO.insertUser(user);
            }
            UserLoginPassword userLoginPassword = loginDAO.findUserLoginPasswordByUsername(username);
            if (userLoginPassword == null) {
                userLoginPassword = new UserLoginPassword();
                userLoginPassword.setId(new UUID());
                userLoginPassword.setUserId(user.getId());
                userLoginPassword.setAlgorithm("SHA256");
                userLoginPassword.setIterations(1);
                userLoginPassword.setSalt(RandomUtil.randomByteArray(8));
                userLoginPassword.setPasswordHash(PasswordCredentialsMatcher.passwordHash(password.getBytes(), userLoginPassword));
                userLoginPassword.setEnabled(true);
                loginDAO.insertUserLoginPassword(userLoginPassword.getId(), userLoginPassword.getUserId(), userLoginPassword.getPasswordHash(), userLoginPassword.getSalt(), userLoginPassword.getIterations(), userLoginPassword.getAlgorithm(), userLoginPassword.getExpires(), userLoginPassword.isEnabled());
                isNewPassword = true;
            }
            Role adminRole = loginDAO.findRoleByName(ADMINISTRATOR_ROLE);
            if (adminRole == null) {
                adminRole = new Role();
                adminRole.setId(new UUID());
                adminRole.setRoleName(ADMINISTRATOR_ROLE);
                adminRole.setDescription("the only role required to exist; the administrator may create and edit all other roles and permissions");
                loginDAO.insertRole(adminRole.getId(), adminRole.getRoleName(), adminRole.getDescription());
            }
            List<Role> adminUserRoles = loginDAO.findRolesByUserLoginPasswordId(userLoginPassword.getId());
            ArrayList<UUID> adminUserRoleIdList = new ArrayList<>(); // it's a list but it will only be populated with ONE role id -- the administrator role id
            if (adminUserRoles.isEmpty()) {
                loginDAO.insertUserLoginPasswordRole(userLoginPassword.getId(), adminRole.getId());
                adminUserRoleIdList.add(adminRole.getId());
            } else {
                // admin user already has some roles, check if one of them is the admin role -- if the admin user is missing the admin role we will automatically add it
                for (Role role : adminUserRoles) {
                    if (role.getRoleName().equalsIgnoreCase(ADMINISTRATOR_ROLE)) {
                        adminUserRoleIdList.add(role.getId());
                    }
                }
                if (adminUserRoleIdList.isEmpty()) {
                    loginDAO.insertUserLoginPasswordRole(userLoginPassword.getId(), adminRole.getId());
                    adminUserRoleIdList.add(adminRole.getId());
                }
            }
            List<RolePermission> adminRolePermissions = loginDAO.findRolePermissionsByPasswordRoleIds(adminUserRoleIdList);
            if (adminRolePermissions.isEmpty()) {
                RolePermission adminPermission = new RolePermission();
                adminPermission.setRoleId(adminRole.getId());
                adminPermission.setPermitDomain("*");
                adminPermission.setPermitAction("*");
                adminPermission.setPermitSelection("*");
                loginDAO.insertRolePermission(adminPermission.getRoleId(), adminPermission.getPermitDomain(), adminPermission.getPermitAction(), adminPermission.getPermitSelection());
            }
            if (isNewPassword) {
                // save the password to a file so the admin user can read it ; because it shouldn't be stored in the permanent configuration
                File privateDir = new File(MyFilesystem.getApplicationFilesystem().getConfigurationPath() + File.separator + "private");
                if (!privateDir.exists()) {
                    privateDir.mkdirs();
                }
                if (Platform.isUnix()) {
                    Runtime.getRuntime().exec("chmod 700 " + privateDir.getAbsolutePath());
                }
                File passwordFile = privateDir.toPath().resolve("password.txt").toFile();
                FileUtils.writeStringToFile(passwordFile, ""); // first create an empty file so we can set permissions before writing the password to it
                if (Platform.isUnix()) {
                    Runtime.getRuntime().exec("chmod 600 " + passwordFile.getAbsolutePath());
                }
                FileUtils.writeStringToFile(passwordFile, String.format("%s\n", password));
            }
        }
    }
}
