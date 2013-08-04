/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.io;

import java.math.BigInteger;
//import org.codehaus.jackson.annotate.JsonCreator;
//import org.codehaus.jackson.annotate.JsonValue;
//import com.fasterxml.jackson.annotation.JsonCreator;
//import com.fasterxml.jackson.annotation.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convenience class that adds conversion to/from ByteArray.
 * 
 * Automatically serializes/deserializes to UUID format string with Jackson (both 1.x and 2.x)
 * 
 * References:
 * http://www.ietf.org/rfc/rfc4122.txt   UUID 
 * 
 * @author jbuhacoff
 */
public class UUID {
    private static Logger log = LoggerFactory.getLogger(UUID.class);
    private final java.util.UUID uuid;
    private final ByteArray array;
    
    /**
     * Creates a new random UUID (type 4)
     */
    public UUID() {
        this(java.util.UUID.randomUUID());
    }
    
    /**
     * Creates a UUID object using the value of the specified UUID
     * @param uuid 
     */
    public UUID(java.util.UUID uuid) {
        this.uuid = uuid;
        BigInteger msb = BigInteger.valueOf(uuid.getMostSignificantBits());
        BigInteger lsb = BigInteger.valueOf(uuid.getLeastSignificantBits());
        BigInteger big = msb.add(BigInteger.ONE).shiftLeft(64).add(lsb); // move msb left 64 bits (8 bytes) to make room for lsb on the right;  adding one first is required, otherwise we always get an off-by-one error like this on the assertion:  f1a57ef26111443[9]a03bb01f893d8e7a  actual: f1a57ef26111443[8]a03bb01f893d8e7a
        array = new ByteArray(big);
    }
    
    public ByteArray toByteArray() {
        return array;
    }
    
    public BigInteger toBigInteger() {
        return array.toBigInteger();
    }
    
    
    public java.util.UUID uuidValue() {
        return uuid;
    }
    
    /**
     * 
     * @return the UUID representation (32 hex characters in groups of 8, 4, 4, 4, 12 separated by hyphens)
     */
    @org.codehaus.jackson.annotate.JsonValue // jackson 1.x
    @com.fasterxml.jackson.annotation.JsonValue // jackson 2.x
    @Override
    public String toString() {
        return uuid.toString();
    }
    
    /**
     * 
     * @return 32 hex characters (16 bytes) ; same as toString but without hyphens
     */
    public String toHexString() {
        return array.toHexString();
    }
    
    public static UUID valueOf(byte[] array) {
        if( array.length != 16 ) { throw new IllegalArgumentException("UUID must be 16 bytes"); }
        return valueOf(new ByteArray(array));
    }
    
    /**
     * 
     * @param array must be 16 bytes;  it's also ok to provide a 17 byte array if the MSB is zero (leading zero, which happens when you serialize a positive 128-bit BigInteger to byte array, to preserve the sign)
     * @return 
     */
    public static UUID valueOf(ByteArray array) {
        if( array.length() == 17 && array.getBytes()[0] == 0 ) {
            return valueOf(array.subarray(1)); // skip leading zero; recursive call
        }
        if( array.length() != 16 ) { throw new IllegalArgumentException("UUID must be 16 bytes"); }
        return new UUID(new java.util.UUID(array.subarray(0, 8).toBigInteger().longValue(), array.subarray(8, 8).toBigInteger().longValue()));
    }
    
    @org.codehaus.jackson.annotate.JsonCreator // jackson 1.x
    @com.fasterxml.jackson.annotation.JsonCreator // jackson 2.x
    public static UUID valueOf(String text) {
        if( text.length() != 32 && text.length() != 36 ) { throw new IllegalArgumentException("UUID must be 16 bytes; up to 4 hyphens allowed for standard UUID hex format"); }
        if( text.length() == 32 ) {
            return valueOf(ByteArray.fromHex(text));
        }
        if( text.length() == 36 ) {
            String withoutHyphens = text.replaceAll("-", ""); // should be exactly 4 hyphens
            if( withoutHyphens.length() != 32 ) { throw new IllegalArgumentException("UUID must be 16 bytes; up to 4 hyphens allowed for standard UUID hex format"); }
            return valueOf(ByteArray.fromHex(withoutHyphens));
        }
        throw new IllegalArgumentException("Unrecognized UUID format"); 
    }
    
    /**
     * 
     * @param number must be 128 bit number (small numbers are automatically zero-padded)
     * @return 
     */
    public static UUID valueOf(BigInteger number) {
        ByteArray array = new ByteArray(number);
        log.debug("Array length 1: {}", array.length());
        log.debug("Array: {}", array.toHexString());
        if( array.length() == 17 && array.getBytes()[0] == 0 ) {
            return valueOf(array.subarray(1)); // skip leading zero
        }
        if( array.length() < 16 ) {
            int padding = 16 - array.length();
            log.debug("Adding padding: {} bytes", padding);
            ByteArray zero = new ByteArray(new byte[padding]);
            return valueOf(ByteArray.concat(zero, array));
        }
        log.debug("Array length 2: {}", array.length());
        if( array.length() != 16 ) { throw new IllegalArgumentException("UUID must be 128 bits"); }
        return valueOf(array);
    }
    
    
}
