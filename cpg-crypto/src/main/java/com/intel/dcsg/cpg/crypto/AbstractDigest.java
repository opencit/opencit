/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto;

import java.util.Arrays;
import org.apache.commons.codec.binary.Hex;

/**
 *
 * @author jbuhacoff
 */
public class AbstractDigest {
    protected DigestAlgorithm algorithm;
    protected byte[] value;
    
    protected AbstractDigest(DigestAlgorithm algorithm) {
        this.algorithm = algorithm;
    }
    protected AbstractDigest(DigestAlgorithm algorithm, byte[] value) {
        if(!algorithm.isValid(value)) {
            throw new IllegalArgumentException("Invalid "+algorithm.name()+" digest: "+(value==null?"null":Hex.encodeHexString(value)));
        }
        this.algorithm = algorithm;
        this.value = value;
    }
    
    protected AbstractDigest(DigestAlgorithm algorithm, String hexValue) {
        if(!algorithm.isValidHex(hexValue)) {
            throw new IllegalArgumentException("Invalid "+algorithm.name()+" digest: "+(hexValue==null?"null":hexValue));
        }
        this.algorithm = algorithm;
        this.value = HexUtil.toByteArray(hexValue);        
    }

    public String algorithm() {
        return algorithm.name(); // MD5, SHA1, SHA256  ... XXX or do we want to return the java name MD5, SHA-1, SHA-256 ?  that one is in algorithm.algorithm()
    }
    
    public byte[] toByteArray() {
        return value;
    }

    public String toHexString() {
        return Hex.encodeHexString(value);
    }
    
    @Override
    public String toString() {
        return Hex.encodeHexString(value);
    }
    
    @Override
    public int hashCode() {
        return value.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AbstractDigest other = (AbstractDigest) obj;
        if (!Arrays.equals(value, other.value)) {
            return false;
        }
        return true;
    }
    
    
    /**
     * Returns a digest of the input parameter.
     * 
     * Subclasses can implement a valueOf function like this:
     * 
     * public Md5Digest valueOf(byte[] message) {
     *  return valueOf(Md5Digest.class, message);
     * }
     * 
     * Throws UnsupportedOperationException if the platform does not support the required digest algorithm.
     * This is an unchecked exception because users of this method are expecting that the algorithm will
     * be available (MD5, SHA1, etc) and are using a strongly typed object to represent it (a sublcass of
     * AstractMessageDigest). Thus if the algorithm is not available they cannot rewrite themselves to 
     * accommodate.   Similarly for the InstantiationException and IllegalAccessException, callers expect the
     * concrete subclasses of this class to be available so if they are not, it will not be handled.  For this
     * reason those exceptions are also wrapped by UnsupportedOperationException.
     * 
     * On the other hand, any code that takes the algorithm as an input parameter from a user and tries to
     * perform a digest should be using the MessageDigest class directly to be able to catch NoSuchAlgorithmException
     * and handle it appropriately.
     * 
     * @param <T>
     * @param clazz
     * @param message
     * @return
     * @throws UnsupportedOperationException if the algorithm specified by the clazz parameter isn't available; wraps NoSuchAlgorithmException
     * @throws IllegalArgumentException if the class specified by the clazz parameter cannot be instantiated due to an InstantiationException or an IllegalAccessException (wrapped)
     */
    /*
    protected static <T extends AbstractMessageDigest> T digestOf(Class<T> clazz, byte[] message) {
        try {
            T model = clazz.newInstance(); // throws InstantiationException, IllegalAccessException
            try {
                MessageDigest hash = MessageDigest.getInstance(model.algorithm()); // throws NoSuchAlgorithmException; example of algorithm is "MD5", "SHA-1", "SHA-256"
                byte[] digest = hash.digest(message);
                model.setBytes(digest);
                return model;
            }
            catch(NoSuchAlgorithmException e) {
                log.error("Cannot create instance of "+model.algorithm(), e);
                throw new UnsupportedOperationException("Missing algorithm implementation: "+model.algorithm(), e);
            }
        }
        catch(InstantiationException e) {
            // This can only be caused by a programming error: passing an abstract class, interface, array class, primitive type, or void; or passing a class that doesn't have an empty constructor
            log.error("Cannot create instance of "+clazz.getName(), e);
            throw new IllegalArgumentException("Invalid digest class: "+clazz.getName(), e);
        }
        catch(IllegalAccessException e) {
            // This can only happen if the subclass that was specified is in another package that we somehow don't have access to.
            log.error("Cannot create instance of "+clazz.getName(), e);
            throw new IllegalArgumentException("Access denied for class: "+clazz.getName(), e);
        }
    }
    */
        
}
