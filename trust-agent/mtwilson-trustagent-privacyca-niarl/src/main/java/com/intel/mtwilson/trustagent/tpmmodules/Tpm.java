/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.trustagent.tpmmodules;

import com.intel.mtwilson.trustagent.TrustagentConfiguration;
import gov.niarl.his.privacyca.TpmModule;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author hxia5
 */
public class Tpm {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Tpm.class);
    public static TpmModuleProvider tpmModule = null;
    public static String tpmVersion=null;
    public static String pcrBanks=null;  // initialized to SHA1 to accomodate tpm1.2. this value is updated if tpm version is 2.0 

    public static String getTpmVersion() {
        if (tpmVersion==null) {
            try {
                tpmVersion = TrustagentConfiguration.getTpmVersion();
            } catch (IOException ex) {
                Logger.getLogger(Tpm.class.getName()).log(Level.SEVERE, null, ex);
            }
            log.debug("Tpm version: {}", tpmVersion);
        }
        return tpmVersion;
    }

    public static void setTpmVersion(String tpmVersion) {
        Tpm.tpmVersion = tpmVersion;
    }
    
    public Tpm() {
        getTpmVersion();
        findModule();
    }

    public static TpmModuleProvider getModule() {
        if (tpmModule == null) {
            findModule();
        }
        return tpmModule;
    }
    
    public static String getpcrBanks() {
        if (pcrBanks==null) {
            try {
                pcrBanks=getModule().getPcrBanks();
                log.debug("tpm.getpcrBanks: {}", pcrBanks);
            } catch (IOException ex) {
                Logger.getLogger(Tpm.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TpmModule.TpmModuleException ex) {
                Logger.getLogger(Tpm.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return pcrBanks;
    }
    
    private static void findModule() {
        String os = System.getProperty("os.name").toLowerCase();
	if  (os.indexOf( "win" ) >= 0) { //Windows
            tpmModule = new TpmModuleWindows();
        } 
        else { // should distinguish if it is TPM 1.2 or TPM 2.0.
            getTpmVersion();
            if (tpmVersion.equals("1.2")) {
                tpmModule = new TpmModule12();
            }
            else { /* tpm 2.0 */
                tpmModule = new TpmModule20();
            }
        }
    }
}
