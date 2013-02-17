/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mountwilson.trustagent;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.provider.JDKDigestSignature;
import org.bouncycastle.jce.provider.JDKDigestSignature.SHA1WithRSAEncryption;
/**
 *
 * @author jbuhacoff
 */
public class Diagnostic {
    public static void main(String[] args) {
        checkBouncycastlePresent();
        Security.addProvider(new BouncyCastleProvider());        
        checkBouncycastleAlgorithms();
    }
    
    public static void checkBouncycastlePresent() {
        tryLoadingClass("org.bouncycastle.jce.provider.JDKDigestSignature");
        tryLoadingClass("org.bouncycastle.jce.provider.JDKDigestSignature$SHA1WithRSAEncryption");
    }
    
    private static void tryLoadingClass(String className) {
        try {
            Class.forName(className);
            System.out.println("Found class: "+className);
        }
        catch(ClassNotFoundException e) {
            System.err.println("Cannot find class: "+className+": "+e.toString());
        }
        catch(Exception e) {
            System.err.println("Cannot load class: "+className+": "+e.toString());
        }
    }
    
    public static void checkBouncycastleAlgorithms() {
        tryMacWithPassword("HmacSHA1", "hello world", "xyzzy");        
        trySha1WithRsaEncryption();
    }
    
    private static void tryMacWithPassword(String algorithmName, String message, String password) {
        try {
            SecretKeySpec key = new SecretKeySpec(password.getBytes(), algorithmName);
            Mac mac = Mac.getInstance(algorithmName, "BC"); // a string like "HmacSHA256"
            mac.init(key);
            byte[] digest = mac.doFinal(message.getBytes());
            System.out.println("Created "+algorithmName+" digest of length "+digest.length);
        }
        catch(NoSuchProviderException e) {
            System.err.println("Cannot use provider: BC: "+e.toString());
        }
        catch(NoSuchAlgorithmException e) {
            System.err.println("Cannot use algorithm: "+algorithmName+": "+e.toString());
        }
        catch(InvalidKeyException e) {
            System.err.println("Cannot use key: "+password+": "+e.toString());
        }
    }
    
    private static void trySha1WithRsaEncryption() {
        SHA1WithRSAEncryption alg = new SHA1WithRSAEncryption();
        System.out.println("Constructed algorithm: "+alg.getClass().getName());
    }
    
}
