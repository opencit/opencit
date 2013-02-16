/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mountwilson.trustagent;

/**
 *
 * @author jbuhacoff
 */
public class Diagnostic {
    public static void main(String[] args) {
        checkBouncycastlePresent();
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
}
