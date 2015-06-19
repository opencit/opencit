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
    public static TpmModuleProvider tpm;

    public Tpm() {
        String os = System.getProperty("os.name").toLowerCase();
	if  (os.indexOf( "win" ) >= 0) { //Windows
            tpm = new TpmModuleWindows();
        } else { // should distinguish if it is TPM 1.2 or TPM 2.0. For now, set to TPM 1.2
            tpm = new TpmModule12();
        }
    }

    public static TpmModuleProvider getTpm() {
        return tpm;
    }
    
}
