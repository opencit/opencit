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
import org.apache.commons.io.FileUtils;
import org.apache.shiro.config.Ini;
import org.apache.shiro.config.Ini.Section;

/**
 *
 * @author hmgowda
 */
public class HostFilterCheck extends LocalSetupTask {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HostFilterCheck.class);

    private File ShiroIniFile;

    public boolean authentication_check() {

        //check if the shiro.ini file exists
        ShiroIniFile = new File(Folders.configuration() + File.separator + "shiro.ini");
        if (!ShiroIniFile.exists()) {
            configuration("File not found: shiro.ini");
        }

        //Read the Shiro.ini file to check for hostFilter and IniHostRealm         
        Ini ShiroFile = new Ini();

        try {
            ShiroFile.load(FileUtils.readFileToString(ShiroIniFile));
        } catch (IOException ex) {
            log.debug("Shiro.ini file not found");
            return false;
        }

        try {

            boolean checkHostRealm = getHostRealmValue(ShiroFile);
            boolean checkHostFilter = getHostFilterValue(ShiroFile);
            if (checkHostRealm || checkHostFilter) {
                return true;
            } else {
                return false;
            }

        } catch (Exception ex) {
            return false;
        }
    }

    //Function to check if the IniHostRealm is the Shiro.ini file
    public boolean getHostRealmValue(Ini ShiroFile) {

        try {

            for (String sectionName : ShiroFile.keySet()) {
                Section section = ShiroFile.get(sectionName);
                String IniHostRealmValue = "com.intel.mtwilson.shiro.authc.host.IniHostRealm";
                log.debug("Checking for IniHostRealm property in Shiro.ini file");

                for (String KeyValue : section.keySet()) {
                    //log.debug("\t" + KeyValue + "=" + section.get(KeyValue));
                    String ValueSet = section.get(KeyValue);
                    if (ValueSet.equals(IniHostRealmValue)) {
                        String AllowValue = KeyValue + ".allow";
                        String CheckAllowed = ShiroFile.getSectionProperty(sectionName, AllowValue);

                        if (CheckAllowed != null) {
                            return true;
                        }
                    }
                }
                return false;
            }
        } catch (Exception ex) {
            log.debug("Error during reading the INI file");
            return false;
        }
        return false;
    }

    //Function to check if the HostAuthenticationFilter is the Shiro.ini file
    public boolean getHostFilterValue(Ini ShiroFile) {

        try {

            for (String sectionName : ShiroFile.keySet()) {
                Section section = ShiroFile.get(sectionName);
                String HostfilterValue = "com.intel.mtwilson.shiro.authc.host.HostAuthenticationFilter";
                log.debug("Checking for HostAuthenticationFilter property in Shiro.ini file");

                for (String KeyValue : section.keySet()) {
                    //log.debug("\t" + KeyValue + "=" + section.get(KeyValue));
                    String ValueSet = section.get(KeyValue);
                    if (ValueSet.equals(HostfilterValue)) {
                        String AllowValue = KeyValue + ".allow";
                        String CheckAllowed = ShiroFile.getSectionProperty(sectionName, AllowValue);

                        if (CheckAllowed != null) {
                            return true;
                        }
                    }
                }
                return false;

            }
        } catch (Exception ex) {
            log.debug("Error during reading the INI file");
            return false;
        }
        return false;
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
