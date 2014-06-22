/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls.policy.creator.impl;

import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.dcsg.cpg.tls.policy.impl.CertificateTlsPolicy;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.dcsg.cpg.x509.repository.CertificateRepository;
import com.intel.dcsg.cpg.x509.repository.HashSetMutableCertificateRepository;
import com.intel.mtwilson.tls.policy.TlsPolicyDescriptor;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyCreator;
import java.security.KeyManagementException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author jbuhacoff
 */
public class CertificateTlsPolicyCreator implements TlsPolicyCreator {

    @Override
    public TlsPolicy createTlsPolicy(TlsPolicyDescriptor tlsPolicyDescriptor) {
        if( "certificate".equalsIgnoreCase(tlsPolicyDescriptor.getPolicyType()) ) {
            try {
                CertificateRepository repository = getCertificateRepository(tlsPolicyDescriptor);
                return new CertificateTlsPolicy(repository); //TlsPolicyBuilder.factory().strict(repository).build();
            }
            catch(CertificateException | KeyManagementException e) {
                throw new IllegalArgumentException("Cannot create certificate policy from given repository", e);
            }
        }
        return null;
    }
    public static class CertificateMetadata {
        public String encoding; // base64
    }
    
   
    private CertificateRepository getCertificateRepository(TlsPolicyDescriptor tlsPolicyDescriptor) throws CertificateException, KeyManagementException {
        HashSetMutableCertificateRepository repository = new HashSetMutableCertificateRepository();
        if( "certificate".equals(tlsPolicyDescriptor.getPolicyType()) && tlsPolicyDescriptor.getData() != null  ) {
            for(String certificateBase64 : tlsPolicyDescriptor.getData()) {
                X509Certificate certificate = X509Util.decodeDerCertificate(Base64.decodeBase64(certificateBase64));
                repository.addCertificate(certificate);
            }
            return repository;
        }
        return null;
    }    
}
