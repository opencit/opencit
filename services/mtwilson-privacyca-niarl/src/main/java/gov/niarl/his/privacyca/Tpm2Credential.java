/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.niarl.his.privacyca;

import java.io.Serializable;

/**
 * This class represents the repsonse to a TPM2_MakeCredential command.
 * It does not matter whether or not the response was given from a real tpm,
 * or a purely external implementation of this command. 
 * See http://www.trustedcomputinggroup.org/wp-content/uploads/TPM-Rev-2.0-Part-3-Commands-01.16-code.pdf
 * Section 12.6 "TPM2_MakeCredential" for more information.
 * @author dczech
 */
public class Tpm2Credential {
    // Size in bytes of TPM Union structures
    public final static int TPM2B_ID_OBJECT_SIZE = 134;
    public final static int TPM2B_ENCRYPTED_SECRET_SIZE = 258;
    
    private byte[] credentialBlob; // encrypted blob of credential
    private byte[] secret; // asymmetrically encrypted key, which encrypted the "credential" blob
    
    public Tpm2Credential(byte[] credentialBlob, byte[] secret) {
        this.credentialBlob = credentialBlob;
        this.secret = secret;        
    }
    
    public byte[] getCredential() {
        return credentialBlob;
    }        
    
    public byte[] getSecret() {
        return secret;
    }
}
