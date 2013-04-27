package com.intel.dcsg.cpg.crypto;

/**
 * Representation of a single MD5 Digest. An MD5 Digest is a 16-byte value.
 * 
 * Implementation is flexible about the MD5 input format
 * and allows spaces, colons, uppercase and lowercase characters
 * 
 * 
 * @since 0.1
 * @author jbuhacoff
 */
public class Md5Digest extends AbstractDigest {
    private static final DigestAlgorithm ALGORITHM = DigestAlgorithm.MD5;
    
    /**
     * This constructor exists so the valueOf methods can instantiate an MD5 object
     * and set its value after validating the input. If they were to call the public
     * constructors, the validation would happen again there redundantly.
     */
    protected Md5Digest() {
        super(ALGORITHM);
    }
    
    /**
     * Use this constructor if you expect that the bytes are a valid MD5 digest.
     * If they are not a valid MD5 digest an IllegalArgumentException exception will be thrown.
     * 
     * If you need to create an Md5Digest where the input may be invalid, use valueOf.
     * 
     * @param value 
     */
    public Md5Digest(byte[] value) {
        super(ALGORITHM, value);
    }
    
    /**
     * Use this constructor if you expect that the hex string represents a valid MD5 digest.
     * If it does not look like an MD5 digest an IllegalArgumentException exception will be thrown.
     * 
     * If you need to create an Md5Digest where the input may be invalid, use valueOf.
     * 
     * @param hex 
     */
    public Md5Digest(String hex) {
        super(ALGORITHM, hex);
    }
    
    /**
     * 
     * @param value can be null
     * @return true if value is a valid MD5 digest
     */
    public static boolean isValid(byte[] value) {
        return ALGORITHM.isValid(value);
    }
    
    /**
     * @param hex without any punctuation or spaces; can be null
     * @return true if the value is a valid hex representation of an MD5 digest
     */
    public static boolean isValidHex(String hexValue) {
        return ALGORITHM.isValidHex(hexValue);
    }

    /**
     * Assumes the input represents an MD5 digest and creates a new instance of Md5Digest to wrap it.
     * This method does NOT compute a digest. If the input is not a valid MD5 representation, a null
     * will be returned.
     * 
     * Callers must always check the return value for null. 
     * 
     * @param digest
     * @return 
     */
    public static Md5Digest valueOf(byte[] digest) {
        if( isValid(digest) ) {
            Md5Digest MD5 = new Md5Digest();
            MD5.value = digest;
            return MD5;
        }
        return null;
    }

    /**
     * Assumes the input represents an MD5 digest and creates a new instance of Md5Digest to wrap it.
     * This method does NOT compute a digest. If the input is not a valid MD5 representation, a null
     * will be returned.
     * 
     * Callers must always check the return value for null. 
     * 
     * @param hex
     * @return 
     */
    public static Md5Digest valueOf(String hex) {
        if( isValidHex(hex) ) {
            Md5Digest digest = new Md5Digest();
            digest.value = HexUtil.toByteArray(hex); // throws HexFormatException if invalid, but shouldn't happen since we check isValid() first
            return digest;
        }
        return null;
    }
    
    /**
     * Computes the MD5 digest of the input and returns the result.
     * @param message
     * @return 
     */
    public static Md5Digest digestOf(byte[] message) {
        return new Md5Digest(ALGORITHM.digest(message));
    }
    
}
