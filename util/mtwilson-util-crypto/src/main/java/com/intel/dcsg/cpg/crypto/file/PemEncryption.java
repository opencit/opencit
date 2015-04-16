/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.file;

import com.intel.dcsg.cpg.io.pem.Pem;

/**
 *
 * @author jbuhacoff
 */
public interface PemEncryption {
    
    Pem getDocument();
    
    boolean isEncrypted();
    
    // key length, cipher mode, padding mode will be known when recipient looks up the key, or cipher mode and padding mode may also be indicated in the algorithm field
    String getEncryptionKeyId();
    String getEncryptionAlgorithm();
    String getEncryptionMode();
    String getEncryptionPaddingMode();
}
