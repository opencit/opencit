/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.rfc822;

import javax.crypto.SecretKey;

/**
 * XXX TODO this interface should probably be in com.intel.dcsg.cpg.crypto 
 * @author jbuhacoff
 */
public interface SecretKeyFinder {
    
    /**
     * 
     * @param keyId
     * @return the corresponding SecretKey or null if it was not found
     */
    SecretKey find(String keyId);
}
