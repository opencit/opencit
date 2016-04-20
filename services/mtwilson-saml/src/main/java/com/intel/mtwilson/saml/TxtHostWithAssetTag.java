/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.saml;

import com.intel.mtwilson.tag.model.X509AttributeCertificate;
import com.intel.mtwilson.datatypes.TxtHost;

/**
 * TEMPORARY class to encapsulate TxtHost and asset tag attributes until
 * the APIs are updated
 * 
 * @author jbuhacoff
 */
public class TxtHostWithAssetTag {
//    private String hostId; // host id to use in the assertion - could be a UUID, or an AIK SHA1, or an IP address, or a hardware UUID
    private TxtHost host;
    private X509AttributeCertificate tagCertificate;

    public TxtHostWithAssetTag() { }
    public TxtHostWithAssetTag(TxtHost host, X509AttributeCertificate tagCertificate) {
        this.host = host;
        this.tagCertificate = tagCertificate;
    }
    
    public TxtHost getHost() {
        return host;
    }

    
    public void setHost(TxtHost host) {
        this.host = host;
    }

    public X509AttributeCertificate getTagCertificate() {
        return tagCertificate;
    }

    
    public void setTagCertificate(X509AttributeCertificate tagCertificate) {
        this.tagCertificate = tagCertificate;
    }
    
    
}
