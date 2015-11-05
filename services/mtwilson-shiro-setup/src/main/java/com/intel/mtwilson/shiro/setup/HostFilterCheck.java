/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.shiro.setup;

import com.intel.mtwilson.Folders;
import com.intel.mtwilson.setup.LocalSetupTask;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.shiro.config.Ini;

/**
 *
 * @author hmgowda
 */
public class HostFilterCheck extends LocalSetupTask {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HostFilterCheck.class);

    private File ShiroIniFile;

    public boolean authentication_check() {

        //check if the shiro.ini file exists
        //if it does not exist, display an appropriate error message
        ShiroIniFile = new File(Folders.configuration() + File.separator + "shiro.ini");
        if (!ShiroIniFile.exists()) {
            configuration("File not found: shiro.ini");
        }

        //read the shiro.ini file and check for hostfilter
        Ini ShiroFile = new Ini();

        try {
            ShiroFile.load(FileUtils.readFileToString(ShiroIniFile));
        } catch (IOException ex) {
            log.debug("Shiro.ini file not found");
            return false;
        }

        try {
            String hostfilterCheck = ShiroFile.getSectionProperty("main", "hostAllow");
            log.debug("String value of hostfilter : " + hostfilterCheck);
            if (hostfilterCheck == null) {
                return false;
            }

            String hostfilterValue = "com.intel.mtwilson.shiro.authc.host.HostAuthenticationFilter";

            if (hostfilterValue.equals(hostfilterCheck)) {
                return true;
            }

            return false;
        }catch (Exception ex) {
            return false;
        }
    }

    @Override
    protected void configure() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void validate() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void execute() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
