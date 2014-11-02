/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.tls.policy;

/**
 *
 * @author jbuhacoff
 */
public interface ProtocolSelector {
    /**
     * The accept method allows dynamic behaviors such as querying the platform
     * to determine the strongest algorithm available and only accepting that,
     * or accepting any version of TLS, or any protocol newer than SSLv2, etc.
     * @param protocolName like SSL, SSLv2, SSLv3, TLS, TLSv1.1, TLSv1.2
     * @return 
     */
    boolean accept(String protocolName);
    
    /**
     * TODO:  change return value to String[] to allow an ordered list of
     * acceptable protocols. 
     * @return the preferred protocol, like TLSv1.2
     */
    String preferred();
}
