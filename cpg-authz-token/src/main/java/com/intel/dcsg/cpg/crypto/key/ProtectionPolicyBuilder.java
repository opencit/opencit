/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.key;

import java.util.Arrays;

/**
 *
 * @author jbuhacoff
 */
public class ProtectionPolicyBuilder {
    private ProtectionPolicy protectionPolicy = new ProtectionPolicy();
    
    public static ProtectionPolicyBuilder factory() { return new ProtectionPolicyBuilder(); }
    
    public ProtectionPolicyBuilder aes128() {
        algorithms("AES");
        minKeyLengthBits(128);
        return this;
    }
    
    public ProtectionPolicyBuilder aes192() {
        algorithms("AES");
        minKeyLengthBits(192);
        return this;
    }

    public ProtectionPolicyBuilder aes256() {
        algorithms("AES");
        minKeyLengthBits(256);
        return this;
    }
    
    public ProtectionPolicyBuilder sha1() {
        digestAlgorithms("SHA-1");
        return this;
    }

    public ProtectionPolicyBuilder sha2() {
        digestAlgorithms("SHA-256", "SHA-384", "SHA-512");
        return this;
    }
    
    public ProtectionPolicyBuilder sha256() {
        digestAlgorithms("SHA-256");
        return this;
    }

    public ProtectionPolicyBuilder sha384() {
        digestAlgorithms("SHA-384");
        return this;
    }

    public ProtectionPolicyBuilder sha512() {
        digestAlgorithms("SHA-512");
        return this;
    }

    public ProtectionPolicyBuilder block() {
        modes("CBC");
        paddings("PKCS5Padding");
        return this;
    }
    
    public ProtectionPolicyBuilder stream() {
        modes("OFB8");
        paddings("NoPadding");
        return this;
    }
    
    /**
     * 
     * @param algorithm for example "AES" or "AES/OFB8/NoPadding" (in the second case you don't need to call mode or padding separately)
     * @return 
     */
    public ProtectionPolicyBuilder algorithms(String... algorithms) {
        for(String algorithm : algorithms) {
        if( algorithm.contains("/") ) {
            protectionPolicy.ciphers.add(algorithm);
        }
        else {
            protectionPolicy.algorithms.add( algorithm );
        }
        
        }
        return this;
    }

    /**
     * XXX TODO  need constants for known modes (in addition to this custom name)  or maybe helper methods like ofb8()  cbc()  etc.
     * @param mode for example "OFB8"
     * @return 
     */
    public ProtectionPolicyBuilder modes(String... modes) {
        protectionPolicy.modes.addAll(Arrays.asList(modes));
        return this;
    }
    
    /**
     * 
     * @param padding for example "NoPadding"
     * @return 
     */
    public ProtectionPolicyBuilder paddings(String... paddings) {
        protectionPolicy.paddings.addAll(Arrays.asList(paddings));
        return this;
    }
    
    /**
     * 
     * @param digestAlgorithm for example "SHA-256"
     * @return 
     */
    public ProtectionPolicyBuilder digestAlgorithms(String... digestAlgorithms) {
        protectionPolicy.digestAlgorithms.addAll(Arrays.asList(digestAlgorithms));
        return this;
    }

    /**
     * 
     * @param keyLengthBits for example 128 for AES-128
     * @return 
     */
    public ProtectionPolicyBuilder minKeyLengthBits(int minKeyLengthBits) {
        protectionPolicy.minKeyLengthBits = minKeyLengthBits;
        return this;
    }

    public ProtectionPolicy build() /*throws NoSuchAlgorithmException, NoSuchPaddingException*/ {
//        protection.cipher = Cipher.getInstance(String.format("%s/%s/%s", protection.algorithm, protection.mode, protection.padding));
        if( protectionPolicy.minKeyLengthBits == Integer.MAX_VALUE ) {
            throw new IllegalArgumentException("Missing minimum key length");
        }
        return protectionPolicy;
    }
}
