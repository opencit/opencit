/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.trustagent.tpmmodules;

import com.intel.mtwilson.trustagent.TrustagentConfiguration;
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
    public static TpmModuleProvider tpm;

    public Tpm() {
        String os = System.getProperty("os.name").toLowerCase();
	if  (os.indexOf( "win" ) >= 0) { //Windows
            tpm = new TpmModuleWindows();
        } 
        else { // should distinguish if it is TPM 1.2 or TPM 2.0. For now, set to TPM 1.2
            String tpmVersion =null;
            try {
                tpmVersion = TrustagentConfiguration.getTpmVersion();
            } catch (IOException ex) {
                Logger.getLogger(Tpm.class.getName()).log(Level.SEVERE, null, ex);
            }
            log.debug("Tpm version: {}", tpmVersion);
            if (tpmVersion.equals("1.2")) {
                tpm = new TpmModule12();
            }
            else { /* tpm 2.0 */
                tpm = new TpmModule20();
            }
        }
    }

    public static TpmModuleProvider getTpm() {
        return tpm;
    }
    
}
