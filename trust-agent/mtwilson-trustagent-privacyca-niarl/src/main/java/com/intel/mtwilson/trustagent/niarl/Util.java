/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.niarl;

import com.intel.dcsg.cpg.crypto.RandomUtil;
import gov.niarl.his.privacyca.TpmModule;
import java.io.IOException;
import org.apache.commons.codec.binary.Hex;
import java.util.NoSuchElementException;

/**
 * Test TPM ownership status by attempting to change the SRK secret and then
 * (if successful) change it back to its original value:
 * 
 * hOldSecret = current SRK secret;
 * Tspi_ChangeAuth(hSRK, hTPM, hNewSecret); 
 * Tspi_ChangeAuth(hSRK, hTPM, hOldSecret);
 * 
 * @author jbuhacoff
 */
public class Util {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Util.class);
    
    public static boolean isOwner(byte[] secret) {
        try {
            //TpmModule.getCredential(secret, "EC");
            byte[] ekModulus = TpmModule.getEndorsementKeyModulus(secret, RandomUtil.randomByteArray(20));
            if( ekModulus != null ) { log.debug("EK modulus: {}", Hex.encodeHexString(ekModulus)); }
            return true;
        }
        catch(IOException | NoSuchElementException e) {
            log.debug("Failed ownership test (get endorsement credential)", e);
            return false;
        }
        catch(TpmModule.TpmModuleException e) {
            if( e.getErrorCode() != null && e.getErrorCode() == 2 ) {
                // error code 2 is TPM_BADINDEX which in this case means the EC
                // is not present; but the ownership test succeeded
                return true;
            }
            // error code 1 is TPM_AUTHFAIL which is the expected case when
            // we don't have ownership; and we'll also fail the test on any
            // other error
            log.debug("Failed ownership test (get endorsement credential) with error code {}", e.getErrorCode());
            return false;
        }
    }
    
    public static boolean isEndorsementCertificatePresent(byte[] secret) {
        try {
            byte[] ekCert = TpmModule.getCredential(secret, "EC");
            return ekCert != null && ekCert.length > 0;
        }
        catch(TpmModule.TpmModuleException e) {
            if( e.getErrorCode() == 2 ) {
                return false; // Endorsement Certificate is missing
            }
            else {
                log.error("Cannot determine presence of Endorsement Certificate");
                log.debug("TpmModule error: {}", e.getMessage());
                return false;
            }
        }
        catch(IOException e) {
            log.error("Cannot determine presence of Endorsement Certificate");
            log.debug("IO error: {}", e.getMessage());
            return false;
        }
    }
}
