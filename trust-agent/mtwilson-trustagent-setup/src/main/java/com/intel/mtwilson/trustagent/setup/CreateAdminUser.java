/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.setup;

import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.dcsg.cpg.io.Platform;
import com.intel.dcsg.cpg.validation.ValidationUtil;
import com.intel.mtwilson.Folders;
import com.intel.mtwilson.setup.LocalSetupTask;
import com.intel.mtwilson.crypto.password.PasswordUtil;
import com.intel.mtwilson.setup.AbstractSetupTask;
import com.intel.mtwilson.shiro.file.LoginDAO;
import com.intel.mtwilson.shiro.file.cmd.Password;
import com.intel.mtwilson.shiro.file.model.UserPassword;
import com.intel.mtwilson.shiro.file.model.UserPermission;
import com.intel.mtwilson.trustagent.TrustagentConfiguration;
import java.io.File;
import java.util.List;
import org.apache.commons.io.FileUtils;

/**
 * NOTE: this task is deprecated by mtwilson-shiro-setup/mtwilson-shiro-file including a similar
 * task but without specific permissions, so an admin can either set the
 * adminUsername and passowrd in environment variables or just create the adminUsername
 * automatically with this task and add application-specific permissions using
 * a command immediately after.
 * 
 * Trust agent admin user permissions:  tpm:provision, tpm:quote, host:info, aik:create
 * 
 * @author jbuhacoff
 */
public class CreateAdminUser extends AbstractSetupTask {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoginRegister.class);
    
    public static final String USERNAME = "(?:([a-zA-Z0-9_\\\\\\.@-]+))";
    public static final String PASSWORD = "(?:([a-zA-Z0-9_\\\\.\\\\, @!#$%^+=>?:{}()\\[\\]\\\"|;~`'*-/]+))";
    
    private File userFile;
    private File permissionFile;
    private String adminUsername;
    private String adminPassword;
    
    private String getUsername() {
        if (adminUsername != null)
            return adminUsername;
        
        if( System.getenv("TRUSTAGENT_ADMIN_USERNAME") != null && !System.getenv("TRUSTAGENT_ADMIN_USERNAME").isEmpty()) {
            return System.getenv("TRUSTAGENT_ADMIN_USERNAME");
        }
        log.info("CreateAdminUser: Since the user name is not specified creating a default user with name '{}'", "admin");
        return "admin";
    }
    
    private String getPassword() {
        if (adminPassword != null)
            return adminPassword;
        
        if( System.getenv("TRUSTAGENT_ADMIN_PASSWORD") != null && !System.getenv("TRUSTAGENT_ADMIN_PASSWORD").isEmpty() ) {
            return System.getenv("TRUSTAGENT_ADMIN_PASSWORD");
        }
        log.info("CreateAdminUser: Since the password is not specified, generating a random password.");
        return RandomUtil.randomHexString(32);
    }
    
    
    @Override
    protected void configure() throws Exception {
        TrustagentConfiguration trustagentConfiguration = new TrustagentConfiguration(getConfiguration());
        adminUsername = getUsername();
        adminPassword = getPassword();
        
        if (!ValidationUtil.isValidWithRegex(adminUsername, USERNAME)) {
            configuration(String.format("Username specified does not match regex: %s", USERNAME));
        }
        if (!ValidationUtil.isValidWithRegex(adminPassword, PASSWORD)) {
            configuration(String.format("Password specified does not match regex: %s", PASSWORD));
        }
                        
        userFile = trustagentConfiguration.getTrustagentUserFile();
        permissionFile = trustagentConfiguration.getTrustagentPermissionsFile();

        // Delete the user if it already exists so that the password gets updated.
        LoginDAO loginDAO = new LoginDAO(userFile, permissionFile);
        UserPassword userPassword = loginDAO.findUserByName(adminUsername);
        if( userPassword != null ) {
            // For password updates, we will just delete the user and recreate it with new password.
            loginDAO.deleteUserByName(adminUsername);
        }
        
    }

    @Override
    protected void validate() throws Exception {
        // ensure we have an admin user created 
        LoginDAO loginDAO = new LoginDAO(userFile, permissionFile);
        UserPassword userPassword = loginDAO.findUserByName(adminUsername);
        if( userPassword == null ) {
            validation("User does not exist: %s", adminUsername);
        }
        
        List<UserPermission> userPermissionList = loginDAO.getPermissions(adminUsername);
        if( userPermissionList == null ||  userPermissionList.isEmpty() ) {
            validation("User does not have permissions assigned: %s", adminUsername);
        } 
    }

    @Override
    protected void execute() throws Exception {
        log.info("Starting the process to configure the username and password.");
        
        Password pwd = new Password();
        pwd.execute(new String[] {adminUsername, adminPassword, "*:*"});

        // We need to store the user name here so that we can use for validation. Password will not be stored in the property file
        log.debug("Setting username {} in the configuration file.", adminUsername);
        getConfiguration().set(TrustagentConfiguration.TRUSTAGENT_ADMIN_USERNAME, adminUsername);
        
        // save the adminPassword to a file so the admin user can read it ; because it shouldn't be stored in the permanent configuration
        File privateDir = new File(Folders.configuration() + File.separator + "private");
        if( !privateDir.exists() ) { privateDir.mkdirs(); }
        if( Platform.isUnix() ) {
            Runtime.getRuntime().exec("chmod 700 "+privateDir.getAbsolutePath());
        }
        File passwordFile = privateDir.toPath().resolve("password.txt").toFile();
        FileUtils.writeStringToFile(passwordFile, ""); // first create an empty file so we can set permissions before writing the adminPassword to it
        if( Platform.isUnix() ) {
            Runtime.getRuntime().exec("chmod 600 "+passwordFile.getAbsolutePath());
        }
        FileUtils.writeStringToFile(passwordFile, adminPassword);            
    }
    
}
