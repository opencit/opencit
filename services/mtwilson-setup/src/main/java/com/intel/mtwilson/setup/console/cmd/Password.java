/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.console.cmd;

import com.intel.dcsg.cpg.console.Command;
import com.intel.dcsg.cpg.console.input.Input;
import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.shiro.jdbi.LoginDAO;
//import com.intel.mtwilson.trustagent.TrustagentConfiguration;
import java.io.File;
import org.apache.commons.configuration.Configuration;
import com.intel.mtwilson.shiro.jdbi.model.UserLoginPassword; // file.model.UserPassword;
import com.intel.mtwilson.shiro.jdbi.model.RolePermission; // file.model.UserPermission;
import com.intel.mtwilson.shiro.authc.password.PasswordCredentialsMatcher;
import com.intel.mtwilson.shiro.jdbi.MyJdbi;
import com.intel.mtwilson.shiro.jdbi.model.Role;
import com.intel.mtwilson.shiro.jdbi.model.Status;
import com.intel.mtwilson.shiro.jdbi.model.User;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author jbuhacoff
 */
public class Password implements Command {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Password.class);
//    private TrustagentConfiguration configuration;
    private Configuration options;
    private LoginDAO dao;
    
    @Override
    public void setOptions(Configuration options) {
        this.options = options;
    }
    
    // never returns null but password may be empty (and that's allowed)
    private String getPassword(String[] args) throws Exception {
        String password;
        if( options.getBoolean("nopass", false) ) {
            password = "";
        }
        else if(args.length > 1) {
            password = args[1]; // always after username if present
            if( password == null || password.isEmpty() ) {
                throw new IllegalArgumentException("Password is empty");
            }
            if( password.startsWith("env:") && password.length() > 4 ) {
                String variableName = password.substring(4);
                password = System.getenv(variableName);
                if( password == null || password.isEmpty() ) {
                    throw new IllegalArgumentException(String.format("Environment variable %s does not contain a password", variableName));
                }
            }
        }
        else {
            password = Input.getConfirmedPasswordWithPrompt(String.format("Choose a password for %s",args[0])); // throws IOException, or always returns value or expression
            if( password == null || password.isEmpty() ) {
                throw new IllegalArgumentException("Input password is empty");
            }
        }
        return password;
    }
    // get the 3rd arg if it's usrename passsword permissions, or the 2nd arg if it's username --nopass permissions
    private String getPermissions(String[] args) throws Exception {
        String permissions = null;
        if( args.length == 2 && options.getBoolean("nopass", false) ) {
            permissions = args[1];
        }
        else if(args.length == 3 ) {
            permissions = args[2];
        }
        return permissions;
    }

    @Override
    public void execute(String[] args) throws Exception {
        // store or replace the user record
        log.debug("Loading users and permissions");
        dao = MyJdbi.authz();
        // usage:   username  (prompt for password, no permissions)
        // usage:   username password  (no permissions)
        // usage:   username password permissions
        // usage:   username --nopass  (no permissions)
        // usage:   username --nopass permissions
        // usage:   username --remove
        String username = args[0];
        
        if( options.getBoolean("remove",false) ) {
            removeUser(username);
            removePermissions(username);
            log.info("Removed username {}", username);
            return;
        }
        
        String password = getPassword(args); // never returns null but password may be empty 
        
        // create the new user record
        User user = dao.findUserByName(username);
        removeUser(username);
        if (user == null) {
            user = new User();
            user.setId(new UUID());
            //user.setComment("automatically created by setup");
            user.setEnabled(true);
            user.setStatus(Status.APPROVED);
            user.setUsername(username);
            dao.insertUser(user);
            log.info("Stored User: {}", username);
        }
        UserLoginPassword userLoginPassword = new UserLoginPassword();
        userLoginPassword.setId(new UUID());
        userLoginPassword.setUserId(user.getId());
        userLoginPassword.setAlgorithm("SHA256");
        userLoginPassword.setIterations(1);
        userLoginPassword.setSalt(RandomUtil.randomByteArray(8));
        userLoginPassword.setPasswordHash(PasswordCredentialsMatcher.passwordHash(password.getBytes(), userLoginPassword));
        userLoginPassword.setEnabled(true);
        dao.insertUserLoginPassword(userLoginPassword.getId(), userLoginPassword.getUserId(), userLoginPassword.getPasswordHash(), userLoginPassword.getSalt(), userLoginPassword.getIterations(), userLoginPassword.getAlgorithm(), userLoginPassword.getExpires(), userLoginPassword.isEnabled());
        log.info("Stored UserLoginPassword with ID: {}", userLoginPassword.getId());
        
        String newPermissions = getPermissions(args);
        if( newPermissions != null ) { 
            removePermissions(username);
            RolePermission userPermission = new RolePermission();
            userPermission.setRoleId(new UUID());
            userPermission.setPermitDomain("tpms");
            userPermission.setPermitAction("endorse");
            userPermission.setPermitSelection("*");
            dao.insertRolePermission(userPermission.getRoleId(), userPermission.getPermitDomain(), userPermission.getPermitAction(), userPermission.getPermitSelection());
            
            log.info("Stored permissions {}", newPermissions);
        }
        
    }
    
    private void removeUser(String username) throws IOException {
        UserLoginPassword existingUser = dao.findUserLoginPasswordByUsername(username);
        if( existingUser != null ) {
            dao.deleteUser(existingUser.getUserId());
        }
    }
    private void removePermissions(String username) throws IOException {
        UserLoginPassword existingUser = dao.findUserLoginPasswordByUsername(username);
        List<UUID> roleUuidList = getRoleUuidList(dao.findRolesByUserLoginPasswordId(existingUser.getId()));
        List<RolePermission> existingPermissions = dao.findRolePermissionsByPasswordRoleIds(roleUuidList);
        for(RolePermission existingPermission : existingPermissions) {
            dao.deleteRolePermission(existingPermission.getRoleId(), existingPermission.getPermitDomain(), existingPermission.getPermitAction(), existingPermission.getPermitSelection());
        }
    }
    
    List<UUID> getRoleUuidList(List<Role> roles) {
        ArrayList<UUID> uuids = new ArrayList<>();
        for (Role role : roles) {
            uuids.add(role.getId());
        }
        return uuids;
    }
    
}
