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

    public String authentication_check() {

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
            return "Authentication Bipass Disabled";
        }

        try {

            boolean checkValue = getKeyforValue(ShiroFile);
            if (checkValue) {
                return "Authentication Bipass Enabled";
            } else {
                return "Authentication Bipass Disabled";
            }

        } catch (Exception ex) {
            return "Authentication Bipass Disabled";
        }
    }

    public boolean getKeyforValue(Ini ShiroFile) {

        try {

            for (String sectionName : ShiroFile.keySet()) {
                Section section = ShiroFile.get(sectionName);
                String HostfilterValue = "com.intel.mtwilson.shiro.authc.host.IniHostRealm";

                for (String KeyValue : section.keySet()) {
                    log.debug("\t" + KeyValue + "=" + section.get(KeyValue));
                    String ValueSet = section.get(KeyValue);
                    if (ValueSet.equals(HostfilterValue)) {
                        String AllowValue = KeyValue + ".allow";
                        String CheckAllowed = ShiroFile.getSectionProperty(sectionName, AllowValue);

                        if (CheckAllowed != null) {
                            return true;
                        }
                    } else {
                        continue;
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
