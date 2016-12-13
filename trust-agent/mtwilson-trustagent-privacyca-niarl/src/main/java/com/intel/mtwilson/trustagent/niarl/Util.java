/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.niarl;

import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.mtwilson.trustagent.TrustagentConfiguration;
import gov.niarl.his.privacyca.IdentityOS;
import gov.niarl.his.privacyca.TpmModule;
import com.intel.mtwilson.common.CommandResult;
import com.intel.mtwilson.common.CommandUtil;
import com.intel.mtwilson.common.TAException;
import gov.niarl.his.privacyca.TpmUtils;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.apache.commons.codec.binary.Hex;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    
    public static boolean isOwner(byte[] secret) throws TAException, IOException {
        
        /* Dertermine based on the OS type and TPM version */
        if (IdentityOS.isWindows()) { 
            /* return true for now since Windows usually take the ownership of TPM be default 
             * need to check later for exceptions
            */
            return true;
        }
        else { /* this should also branch based on if it is TPM 1.2 or TPM 2.0 since the interaction with them is different */
            String tpmVersion = "1.2";
            try {
                tpmVersion = TrustagentConfiguration.getTpmVersion();
            } catch (IOException ex) {
                Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
            }
            log.debug("Tpm version: {}", tpmVersion);
            if (tpmVersion.equals("1.2")) {
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
            else {
                CommandResult result = CommandUtil.runCommand("tpm2-isowner " + TpmUtils.byteArrayToHexString(secret));
                if (result != null && result.getStdout() != null) {
                    if(result.getStdout().contains("1")) 
                        return true;
                }
                return false;        
            }
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
    
    public static byte[] fixMakeCredentialBlobForWindows(byte[] in) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bos);
        
        final int SECRET_SIZE = 134;
        //final int ASYM_SIZE = 256 + 2;

        ByteBuffer buf = ByteBuffer.wrap(in);
        int secretLength = buf.order(ByteOrder.LITTLE_ENDIAN).getShort();
        
        out.writeShort((short)secretLength);
        
        byte[] b = new byte[secretLength];
        
        buf.get(b);
        
        out.write(b);
        
        buf.position(SECRET_SIZE);
        
        int asymLength = buf.order(ByteOrder.LITTLE_ENDIAN).getShort();
        
        out.writeShort((short)asymLength);
        
        byte [] c = new byte[asymLength];                
        
        buf.get(c);
        
        out.write(c);
        
        return bos.toByteArray();
    }
}
