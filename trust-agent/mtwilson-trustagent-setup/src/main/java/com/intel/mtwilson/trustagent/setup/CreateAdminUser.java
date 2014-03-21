/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.setup;

import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.dcsg.cpg.io.Platform;
import com.intel.mtwilson.MyFilesystem;
import com.intel.mtwilson.setup.LocalSetupTask;
import com.intel.mtwilson.shiro.authc.password.PasswordCredentialsMatcher;
import com.intel.mtwilson.shiro.file.LoginDAO;
import com.intel.mtwilson.shiro.file.model.UserPassword;
import com.intel.mtwilson.trustagent.TrustagentConfiguration;
import java.io.File;
import org.apache.commons.io.FileUtils;

/**
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
        return RandomUtil.randomBase64String(8).replace("=","_"); // TODO: use a larger alphabet with more special characters
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
            user.setPasswordHash(PasswordCredentialsMatcher.passwordHash(password.getBytes(), user));
            loginDAO.createUser(user);
            loginDAO.addPermission(username, "tpm:provision");
            loginDAO.addPermission(username, "tpm:quote");
            loginDAO.addPermission(username, "host:info");
            loginDAO.addPermission(username, "aik:create");
        }
        if( isNewPassword ) {
            // save the password to a file so the admin user can read it ; because it shouldn't be stored in the permanent configuration
            File privateDir = new File(MyFilesystem.getApplicationFilesystem().getConfigurationPath() + File.separator + "private");
            if( Platform.isUnix() ) {
                Runtime.getRuntime().exec("chmod 700 "+privateDir.getAbsolutePath());
            }
            File passwordFile = privateDir.toPath().resolve("password.txt").toFile();
            if( !privateDir.exists() ) { privateDir.mkdirs(); }
            FileUtils.writeStringToFile(passwordFile, ""); // first create an empty file so we can set permissions before writing the password to it
            if( Platform.isUnix() ) {
                Runtime.getRuntime().exec("chmod 600 "+passwordFile.getAbsolutePath());
            }
            FileUtils.writeStringToFile(passwordFile, password);            
        }
        // TODO: also create a guest "challenger" user with only tpm:quote permission, and a "hostmanager" user with tpm:quote and host:info which mtwilson would need.
    }
    
}
