/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.key;

import javax.crypto.Cipher;

/**
 * XXX TODO  may need to split this up into the 3 categories:
 * 1) confidentiality (symmetric key info)  
 * 2) integrity (digest info)
 * 3) authentication (asymmetric key inof - currently not here)
 * and make each one an interface:
 * public interface Integrity {
 * String getDigestAlgorithm();
 * int getDigestSizeBytes();
 * }
 * public interface Confidentiality {
 * String getAlgorithm(); // AES
 * String getMode();
 * String getPadding();
 * String getCipher(); //  format is  algorithm/mode/padding
 * int getKeyLengthBits();
 * int blockSizeBytes();
 * }
 * public interface Authentication {
 * String getAlgorithm(); //  RSA
 * int getKeyLengthBits(); 
 * }
 * then public class Protection implements Confidentiality,Integrity,Authentication { ... }
 * that way individual methods that require a protection object as a parameter can use
 * one of the specific interfaces if they're only looking for digest info, or cipher info.
 * and also then other objects could be passed to them too, as well as  evaluate
 * other objects against the protection policy .
 * @author jbuhacoff
 */
public class Protection {
    protected String algorithm; // algorithm like "AES"
    protected String mode; // mode like "OFB8"
    protected String padding; // padding like "NoPadding"
    protected int keyLengthBits;
    protected int blockSizeBytes;
    protected String digestAlgorithm; // like "SHA-256" used for integrity protection; may be null if the content is not protected
    protected int digestSizeBytes; // 20 for SHA-1, 32 for SHA-256, ...
    protected transient String cipher;    //  in the format  algorithm/mode/padding   or just algorithm  if mode and padding are not specified (dangerous - because the crypto provider will use its default so the platforma nd provider become a part of the message specification)
    
    public String getAlgorithm() {
        return algorithm;
    }

    public int getBlockSizeBytes() {
        return blockSizeBytes;
    }

    public int getKeyLengthBits() {
        return keyLengthBits;
    }

    public String getMode() {
        return mode;
    }

    
    public String getPadding() {
        return padding;
    }

    public String getDigestAlgorithm() {
        return digestAlgorithm;
    }

    public int getDigestSizeBytes() {
        return digestSizeBytes;
    }

    public String getCipher() {
        return cipher;
    }
    
    
    
}
