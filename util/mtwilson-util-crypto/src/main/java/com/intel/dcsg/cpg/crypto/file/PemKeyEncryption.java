/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.file;

/**
 *
 * @author jbuhacoff
 */
public interface PemKeyEncryption extends PemEncryption {
    
    String getContentKeyId();
    Integer getContentKeyLength(); // bits
    String getContentAlgorithm();
    String getContentMode();
    String getContentPaddingMode();
    
}
