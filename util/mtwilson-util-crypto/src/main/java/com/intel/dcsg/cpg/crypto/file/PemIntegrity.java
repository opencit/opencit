/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.file;

import com.intel.dcsg.cpg.io.pem.Pem;
import java.util.List;

/**
 *
 * @author jbuhacoff
 */
public interface PemIntegrity {
    Pem getDocument();
    boolean isIntegrated();
    
    String getIntegrityKeyId();
    Integer getIntegrityKeyLength();
    String getIntegrityAlgorithm();
    List<String> getIntegrityManifest();
}
