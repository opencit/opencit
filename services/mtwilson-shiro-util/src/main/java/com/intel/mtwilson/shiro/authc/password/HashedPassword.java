/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.authc.password;

/**
 * 
 * @author jbuhacoff
 */
public interface HashedPassword {
    byte[] getPasswordHash();
    byte[] getSalt();
    int getIterations();
    String getAlgorithm();
}
