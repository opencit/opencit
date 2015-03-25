/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.file.setup;

import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.dcsg.cpg.io.Platform;
import com.intel.mtwilson.Folders;
import com.intel.mtwilson.setup.LocalSetupTask;
import com.intel.mtwilson.crypto.password.PasswordUtil;
import com.intel.mtwilson.shiro.file.LoginDAO;
import com.intel.mtwilson.shiro.file.model.UserPassword;
import com.intel.mtwilson.shiro.file.model.UserPermission;
import java.io.File;
import java.util.List;
import org.apache.commons.io.FileUtils;

/**
 * @deprecated this setup task should not be used; instead the administrator should run the "password" command provided by mtwilson-shiro-file to create an admin username and password after installation
 * @author jbuhacoff
 */
public class AdminUser extends LocalSetupTask {
    private File userFile;
    private File permissionFile;
    private String username;
    private String password;
    private boolean isNewPassword = false;
    
    private String getUsername() {
        // gets environment variable MTWILSON_ADMIN_USERNAME, TRUSTAGENT_ADMIN_USERNAME, KMS_ADMIN_USERNAME, etc.
//        return Environment.get("ADMIN_USERNAME", "admin"); // avoid use of Environment outside bootstrap code
        // setup tasks configuration includes all-caps environment, so admin.username here will also be translated to ADMIN_USERNAME and MTWILSON_ADMIN_USERNAME 
        return getConfiguration().get("admin.username", "admin");
    }
    private String getPassword() {
        // gets environment variable MTWILSON_ADMIN_PASSWORD, TRUSTAGENT_ADMIN_PASSWORD, KMS_ADMIN_PASSWORD, etc.
//        String existing = Environment.get("ADMIN_PASSWORD");// avoid use of Environment outside bootstrap code
        String existing = getConfiguration().get("admin.password");
        if( existing != null ) {
            return existing;
        }
        isNewPassword = true;
        return RandomUtil.randomBase64String(8).replace("=","_"); 
    }
    
    
    @Override
    protected void configure() throws Exception {
        username = getUsername();
        password = getPassword();
        userFile = new File(Folders.configuration()+File.separator+"users.txt");
        permissionFile = new File(Folders.configuration()+File.separator+"permissions.txt");
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
            loginDAO.addPermission(username, "*:*");
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
