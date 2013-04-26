/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.tls.policy;

import com.intel.dcsg.cpg.io.FileResource;
import com.intel.dcsg.cpg.tls.policy.impl.ConsoleTrustDelegate;
import com.intel.dcsg.cpg.x509.repository.MutableCertificateRepository;
import com.intel.dcsg.cpg.x509.repository.PemMutableCertificateRepository;
import java.io.File;
import java.io.IOException;
import java.security.cert.CertificateException;

/**
 * This class provides a convenient way to instantiate common TlsPolicy choices.
 * You can create something similar in your own project to provide additional pre-built policies.
 * 
 * @author jbuhacoff
 */
public class TlsPolicyCommon {
    public static TlsPolicy insecure() {
        return TlsPolicyBuilder.factory().insecure().build();
    }
    
    /**
     * Uses a file called "trusted-certs.pem" in the current directory as the certificate repository.
     * Uses the ConsoleTrustDelegate, so it is only useful in applications that have a console/terminal.
     * @return 
     */
    public static TlsPolicy consoleBrowser() throws IOException, CertificateException {
        MutableCertificateRepository repository = new PemMutableCertificateRepository(new FileResource(new File("trusted-certs.pem")));
        TrustDelegate delegate = new ConsoleTrustDelegate(repository);
        return TlsPolicyBuilder.factory().browser(repository, delegate).build();        
    }
    
    /**
     * Uses the specified file as the certificate repository.
     * Uses the ConsoleTrustDelegate, so it is only useful in applications that have a console/terminal.
     * @return 
     */
    public static TlsPolicy consoleBrowserWithRepository(File trustedCertificates) throws IOException, CertificateException {
        MutableCertificateRepository repository = new PemMutableCertificateRepository(new FileResource(trustedCertificates));
        TrustDelegate delegate = new ConsoleTrustDelegate(repository);
        return TlsPolicyBuilder.factory().browser(repository, delegate).build();        
    }
    
}
