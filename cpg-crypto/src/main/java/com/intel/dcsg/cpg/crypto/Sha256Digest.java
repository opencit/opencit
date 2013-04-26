package com.intel.dcsg.cpg.crypto;

/**
 * Representation of a single SHA1 Digest. An SHA256 Digest is a 32-byte value.
 * 
 * @since 0.5.4
 * @author jbuhacoff
 */
public class Sha256Digest extends AbstractMessageDigest {
    public Sha256Digest() {
        super("SHA-256", 32);
    }
    
    public Sha256Digest(byte[] value) {
        this();
        setBytes(value);
    }
    
    public Sha256Digest(String hex) {
        this();
        setHex(hex);
    }
    
    public static Sha256Digest valueOf(byte[] message) {
        return valueOf(Sha256Digest.class, message);
    }

}
