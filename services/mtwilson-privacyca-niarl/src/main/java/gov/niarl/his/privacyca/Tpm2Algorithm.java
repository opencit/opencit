/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.niarl.his.privacyca;

/**
 *
 * @author dczech
 */
final public class Tpm2Algorithm {
    private Tpm2Algorithm() {
        
    }
    
    public enum Hash {
        SHA1,
        SHA256
    }
    
    public enum Symmetric {
        AES,
        SM4
    }
    
    public enum SymmetricMode {
        CTR,
        OFB,
        CBC,
        CFB,
        ECB,
    }
    
    public enum Asymmetric {
        RSA,
        ECDSA,
        ECDH
    }
}
