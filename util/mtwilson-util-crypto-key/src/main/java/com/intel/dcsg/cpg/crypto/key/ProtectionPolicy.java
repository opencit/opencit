/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.key;

import java.util.HashSet;

/**
 * Default settings for a protection policy deny all matches because all
 * allowed sets are set to empty and the minimum key length is set to 
 * Integer.MAX_VALUE.
 * 
 * When validating a protection against the policy,  the cipher list is 
 * checked first. If there is a match then algorith/mode/padding are not
 * checked separately. The minimum key length is always checked.
 * 
 * @author jbuhacoff
 */
public class ProtectionPolicy {
    protected HashSet<String> algorithms = new HashSet<String>(); // algorithm like "AES"
    protected HashSet<String> modes = new HashSet<String>(); // mode like "OFB8"
    protected HashSet<String> paddings = new HashSet<String>(); // padding like "NoPadding"
    protected int minKeyLengthBits = Integer.MAX_VALUE;
//    protected int blockSizeBytes;
    protected HashSet<String> digestAlgorithms = new HashSet<String>(); // like "SHA-256" used for integrity protection; may be null if the content is not protected
//    protected int digestSizeBytes; // 20 for SHA-1, 32 for SHA-256, ...
    protected transient HashSet<String> ciphers = new HashSet<String>();    //  used to restrict COMBINATIONS of algorithm/mode/padding  instead of individual settings 

    public HashSet<String> getAlgorithms() {
        return algorithms;
    }

    public HashSet<String> getModes() {
        return modes;
    }

    public HashSet<String> getPaddings() {
        return paddings;
    }

    public HashSet<String> getDigestAlgorithms() {
        return digestAlgorithms;
    }

    public int getMinKeyLengthBits() {
        return minKeyLengthBits;
    }

    public HashSet<String> getCiphers() {
        return ciphers;
    }

    public boolean acceptAlgorithm(String name) {
        return algorithms.contains(name);
    }
    public boolean acceptMode(String name) {
        return modes.contains(name);
    }
    public boolean acceptPadding(String name) {
        return paddings.contains(name);
    }
    public boolean acceptDigestAlgorithm(String name) {
        return digestAlgorithms.contains(name);
    }
    public boolean acceptMinKeyLengthBits(int n) {
        return n >= minKeyLengthBits;
    }
    public boolean acceptCipher(String name) {
        return ciphers.contains(name);
    }
    
    public boolean accept(Protection protection) {
        if( acceptCipher(protection.cipher) && acceptMinKeyLengthBits(protection.keyLengthBits) && acceptDigestAlgorithm(protection.digestAlgorithm) ) {
            return true;
        }
        if( acceptAlgorithm(protection.algorithm) && acceptMode(protection.mode) && acceptPadding(protection.padding) && acceptMinKeyLengthBits(protection.keyLengthBits) && acceptDigestAlgorithm(protection.digestAlgorithm) ) {
            return true;
        }
        return false;
    }
    
}
