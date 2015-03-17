/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.file.cmd;

import com.intel.dcsg.cpg.console.Command;
import com.intel.dcsg.cpg.console.input.Input;
import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.mtwilson.Folders;
import com.intel.mtwilson.shiro.file.LoginDAO;
import org.apache.commons.configuration.Configuration;
import com.intel.mtwilson.shiro.file.model.UserPassword;
import com.intel.mtwilson.shiro.file.model.UserPermission;
import com.intel.mtwilson.crypto.password.PasswordUtil;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
/**
 * NOTE: this task is deprecated by mtwilson-shiro-file including a similar
 * task but without trustagent specific permissions.
 * 
 * @author jbuhacoff
 */
public class Password implements Command {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Password.class);
    private Configuration options;
    private LoginDAO dao;
    
    @Override
    public void setOptions(Configuration options) {
        this.options = options;
    }
    
    // never returns null but password may be empty (and that's allowed)
    private String getPassword(String[] args) throws IOException {
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
    private String getPermissions(String[] args) {
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
        File userFile = new File(Folders.configuration()+File.separator+"users.txt");
        File permissionFile = new File(Folders.configuration()+File.separator+"permissions.txt");
        
        // store or replace the user record
        log.debug("Loading users and permissions");
        dao = new LoginDAO(userFile, permissionFile);
        
        if( options.getBoolean("list", false) ) {
            if( args.length > 0 ) {
                // list all permissions for specified user
                String username = args[0];
                List<UserPermission> userPermissions = dao.getPermissions(username);
                for(UserPermission userPermission : userPermissions) {
                    System.out.println(userPermission.toString());
                }
            }
            else {
                // list all users and exit, ignore other arguments
                List<String> users = dao.listUsernames();
                for(String user : users) {
                    System.out.println(user);
                }
            }
            return;
        }
        
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
        UserPassword userPassword = new UserPassword();
        userPassword.setAlgorithm("SHA256");
        userPassword.setIterations(1);
        userPassword.setSalt(RandomUtil.randomByteArray(8));
        byte[] hashedPassword = PasswordUtil.hash(password.getBytes(Charset.forName("UTF-8")), userPassword);
        userPassword.setUsername(username);
        userPassword.setPasswordHash(hashedPassword);
        removeUser(username);
        dao.createUser(userPassword);
        log.info("Created user {}", username);
        
        String newPermissions = getPermissions(args);
        if( newPermissions != null ) { 
            removePermissions(username);
            dao.addPermission(username, newPermissions);
            log.info("Added permissions {}", newPermissions);
        }
        
    }
    
    private void removeUser(String username) throws IOException {
        UserPassword existingUser = dao.findUserByName(username);
        if( existingUser != null ) {
            dao.deleteUserByName(username);
        }
    }
    private void removePermissions(String username) throws IOException {
        List<UserPermission> existingPermissions = dao.getPermissions(username);
        for(UserPermission existingPermission : existingPermissions) {
            dao.removePermission(username, existingPermission.toString());
        }
    }
    
}
