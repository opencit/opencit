/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls.policy.provider;

import com.intel.mtwilson.tls.policy.TlsPolicyChoice;
import com.intel.mtwilson.tls.policy.TlsPolicyDescriptor;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyProvider;
import com.intel.mtwilson.tls.policy.factory.V1TlsPolicyFactory;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.codec.binary.Base64;

/**
 * Loads default TLS Policy from value of mtwilson.default.tls.policy.id in
 * mtwilson.properties or in database configuration table.
 *
 * @author jbuhacoff
 */
public class V1CacertsFileTlsPolicyProvider implements TlsPolicyProvider {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(V1CacertsFileTlsPolicyProvider.class);


    @Override
    public TlsPolicyChoice getTlsPolicyChoice() {
        try {
            TlsPolicyChoice tlsPolicyChoice = new TlsPolicyChoice();
            tlsPolicyChoice.setTlsPolicyDescriptor(new TlsPolicyDescriptor());
            tlsPolicyChoice.getTlsPolicyDescriptor().setPolicyType("certificate");
            tlsPolicyChoice.getTlsPolicyDescriptor().setMeta(new HashMap<String,String>());
            tlsPolicyChoice.getTlsPolicyDescriptor().getMeta().put("encoding", "base64"); // see also CertificateMetadata
            tlsPolicyChoice.getTlsPolicyDescriptor().setData(new ArrayList<String>());
            for(X509Certificate certificate : V1TlsPolicyFactory.getMtWilsonTrustedTlsCertificates()) {
                try {
                tlsPolicyChoice.getTlsPolicyDescriptor().getData().add(Base64.encodeBase64String(certificate.getEncoded()));
                } catch(CertificateEncodingException e) {
                    log.warn("Cannot encode certificate {}, skipping: {}", certificate.getSubjectX500Principal().getName(), e.getMessage());
                }
            }
            return tlsPolicyChoice;
        }
        catch(Exception e) {
            log.warn("Cannot initialize V1 trusted certificate authorities", e);
            return null;
        }
    }
}
