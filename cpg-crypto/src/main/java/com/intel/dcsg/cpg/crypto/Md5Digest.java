package com.intel.dcsg.cpg.crypto;

/**
 * Representation of a single MD5 Digest. An MD5 Digest is a 16-byte value.
 * 
 * Implementation is flexible about the MD5 input format
 * and allows spaces, colons, uppercase and lowercase characters
 * 
 * 
 * @since 0.5.4
 * @author jbuhacoff
 */
public class Md5Digest extends AbstractMessageDigest {
    public Md5Digest() {
        super("MD5", 16);
    }
    
    public Md5Digest(byte[] value) {
        this();
        setBytes(value);
    }
    
    public Md5Digest(String hex) {
        this();
        setHex(hex);
    }
    
    public static Md5Digest valueOf(byte[] message) {
        return valueOf(Md5Digest.class, message);
//        MessageDigest hash = MessageDigest.getInstance("MD5");
//        byte[] digest = hash.digest(message);
//        return new Md5Digest(digest);
    }
}
