/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.setup;

import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.dcsg.cpg.io.Platform;
import com.intel.mtwilson.Folders;
import com.intel.mtwilson.setup.LocalSetupTask;
import com.intel.mtwilson.crypto.password.PasswordUtil;
import com.intel.mtwilson.shiro.file.LoginDAO;
import com.intel.mtwilson.shiro.file.model.UserPassword;
import com.intel.mtwilson.shiro.file.model.UserPermission;
import com.intel.mtwilson.trustagent.TrustagentConfiguration;
import java.io.File;
import java.util.List;
import org.apache.commons.io.FileUtils;

/**
 * NOTE: this task is deprecated by mtwilson-shiro-setup/mtwilson-shiro-file including a similar
 * task but without specific permissions, so an admin can either set the
 * username and passowrd in environment variables or just create the username
 * automatically with this task and add application-specific permissions using
 * a command immediately after.
 * 
 * Trust agent admin user permissions:  tpm:provision, tpm:quote, host:info, aik:create
 * 
 * @author jbuhacoff
 */
public class CreateAdminUser extends LocalSetupTask {
    private File userFile;
    private File permissionFile;
    private String username;
    private String password;
    private boolean isNewPassword = false;
    
    private String getUsername() {
        if( System.getenv("TRUSTAGENT_ADMIN_USERNAME") != null ) {
            return System.getenv("TRUSTAGENT_ADMIN_USERNAME");
        }
        return "admin";
    }
    private String getPassword() {
        if( System.getenv("TRUSTAGENT_ADMIN_PASSWORD") != null ) {
            return System.getenv("TRUSTAGENT_ADMIN_PASSWORD");
        }
        isNewPassword = true;
        return RandomUtil.randomBase64String(8).replace("=","_"); 
    }
    
    
    @Override
    protected void configure() throws Exception {
        username = getUsername();
        password = getPassword();
        TrustagentConfiguration trustagentConfiguration = new TrustagentConfiguration(getConfiguration());
        userFile = trustagentConfiguration.getTrustagentUserFile();
        permissionFile = trustagentConfiguration.getTrustagentPermissionsFile();
    }

    @Override
    protected void validate() throws Exception {
        // ensure we have an admin user created 
        LoginDAO loginDAO = new LoginDAO(userFile, permissionFile);
        UserPassword userPassword = loginDAO.findUserByName(username);
        if( userPassword == null ) {
            validation("User does not exist: %s", username);
        }
        List<UserPermission> userPermissionList = loginDAO.getPermissions(username);
        if( userPermissionList == null ||  userPermissionList.isEmpty() ) {
            validation("User does not have permissions assigned: %s", username);
        }
    }

    @Override
    protected void execute() throws Exception {
        LoginDAO loginDAO = new LoginDAO(userFile, permissionFile);
        UserPassword user = loginDAO.findUserByName(username);
        if( user == null ) {
            user = new UserPassword();
            user.setUsername(username);
            user.setAlgorithm("SHA256");
            user.setIterations(1);
            user.setSalt(RandomUtil.randomByteArray(8));
            user.setPasswordHash(PasswordUtil.hash(password.getBytes(), user));
            loginDAO.createUser(user);
            loginDAO.addPermission(username, "tpm:provision");
            loginDAO.addPermission(username, "tpm:quote");
            loginDAO.addPermission(username, "host:info");
            loginDAO.addPermission(username, "aik:create");
        }
        if( isNewPassword ) {
            // save the password to a file so the admin user can read it ; because it shouldn't be stored in the permanent configuration
            File privateDir = new File(Folders.configuration() + File.separator + "private");
            if( !privateDir.exists() ) { privateDir.mkdirs(); }
            if( Platform.isUnix() ) {
                Runtime.getRuntime().exec("chmod 700 "+privateDir.getAbsolutePath());
            }
            File passwordFile = privateDir.toPath().resolve("password.txt").toFile();
            FileUtils.writeStringToFile(passwordFile, ""); // first create an empty file so we can set permissions before writing the password to it
            if( Platform.isUnix() ) {
                Runtime.getRuntime().exec("chmod 600 "+passwordFile.getAbsolutePath());
            }
            FileUtils.writeStringToFile(passwordFile, password);            
        }
    }
    
}
