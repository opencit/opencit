/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.trustagent.tpmmodules;

/**
 *
 * @author hxia5
 */
public class Tpm {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Tpm.class);
    public static TpmModuleProvider tpmModule = null;

    public Tpm() {
        findModule();
    }

    private static void findModule() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.indexOf("win") >= 0) { //Windows
            tpmModule = new TpmModuleWindows();
        } else { // should distinguish if it is TPM 1.2 or TPM 2.0.
            tpmModule = new TpmModule12();
        }
    }

    public static TpmModuleProvider getTpm() {
        if (tpmModule == null) {
            findModule();
        }
        return tpmModule;
    }
    
}
