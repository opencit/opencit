/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls.policy;

import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.io.Resource;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.dcsg.cpg.tls.policy.TlsPolicyBuilder;
import com.intel.dcsg.cpg.tls.policy.impl.FirstCertificateTrustDelegate;
import com.intel.dcsg.cpg.tls.policy.impl.InsecureTlsPolicy;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.dcsg.cpg.x509.repository.KeystoreCertificateRepository;
import com.intel.mtwilson.My;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author jbuhacoff
 */
public class TlsPolicyFactory {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TlsPolicyFactory.class);
    private static final TlsPolicyFactory instance = new TlsPolicyFactory();

    public static TlsPolicyFactory getInstance() {
        return instance;
    }
    private long tlsPemLastModified = 0;
    private long tlsCrtLastModified = 0;
    private ArrayList<X509Certificate> tlsAuthorities = new ArrayList<>();

    public TlsPolicy getTlsPolicyWithKeystore(String tlsPolicyName, Resource resource) throws KeyManagementException, IOException {
        String password = My.configuration().getTlsKeystorePassword();
        SimpleKeystore tlsKeystore = new SimpleKeystore(resource, password); 
        TlsPolicy tlsPolicy = getTlsPolicyWithKeystore(tlsPolicyName, tlsKeystore); 
        return tlsPolicy;
    }

    public TlsPolicy getTlsPolicyWithKeystore(SimpleKeystore tlsKeystore) throws IOException, KeyManagementException {
        return getTlsPolicyWithKeystore(null, tlsKeystore);
    }

    public TlsPolicy getTlsPolicyWithKeystore(String tlsPolicyName, SimpleKeystore tlsKeystore) throws IOException, KeyManagementException {
        if (tlsPolicyName == null) {
            tlsPolicyName = My.configuration().getDefaultTlsPolicyName();
        } 
        String ucName = tlsPolicyName.toUpperCase();
        if (ucName.equals("TRUST_CA_VERIFY_HOSTNAME")) {
            initTlsTrustedCertificateAuthorities();
            for (X509Certificate cacert : tlsAuthorities) {
                log.debug("Adding trusted TLS CA certificate {}", cacert.getSubjectX500Principal().getName());
                try {
                    tlsKeystore.addTrustedSslCertificate(cacert, cacert.getSubjectX500Principal().getName());
                } catch (KeyManagementException e) {
                    log.error("Cannot add TLS certificate authority to host keystore {}", cacert.getSubjectX500Principal().getName());
                }
            }
//            My.configuration().get tls keystore trusted cas; add them to tlsKeystore  beforee making the policy  so that a global keystore can be used;  or just use the global kesytore...
//            return new TrustCaAndVerifyHostnameTlsPolicy(new KeystoreCertificateRepository(tlsKeystore));
            return TlsPolicyBuilder.factory().strict(tlsKeystore.getRepository()).build();
        }
        if (ucName.equals("TRUST_FIRST_CERTIFICATE")) {
//            return new TrustFirstCertificateTlsPolicy(new KeystoreCertificateRepository(tlsKeystore));
            KeystoreCertificateRepository repository = tlsKeystore.getRepository();
            return TlsPolicyBuilder.factory().strict(repository).trustDelegate(new FirstCertificateTrustDelegate(repository)).skipHostnameVerification().build();
        }
        if (ucName.equals("TRUST_KNOWN_CERTIFICATE")) {
//            return new TrustKnownCertificateTlsPolicy(new KeystoreCertificateRepository(tlsKeystore));
            return TlsPolicyBuilder.factory().strict(tlsKeystore.getRepository()).skipHostnameVerification().build();
        }
        if (ucName.equals("INSECURE")) {
            return new InsecureTlsPolicy();
        }
        throw new IllegalArgumentException("Unknown TLS Policy: " + tlsPolicyName);

    }

    private void initTlsTrustedCertificateAuthorities() throws IOException {
        // read the trusted CA's
        String tlsCaFilename = My.configuration().getConfiguration().getString("mtwilson.tls.certificate.file", "mtwilson-tls.pem");
        if (tlsCaFilename != null) {
            if (!tlsCaFilename.startsWith("/")) {
                tlsCaFilename = String.format("/etc/intel/cloudsecurity/%s", tlsCaFilename);
            }
            if (tlsCaFilename.endsWith(".pem")) {
                File tlsPemFile = new File(tlsCaFilename);
                if (tlsPemFile.lastModified() > tlsPemLastModified) {
                    tlsPemLastModified = tlsPemFile.lastModified();
                    tlsAuthorities.clear();
                    try (FileInputStream in = new FileInputStream(tlsPemFile)) {
                        String content = IOUtils.toString(in);
                        List<X509Certificate> cacerts = X509Util.decodePemCertificates(content);
                        tlsAuthorities.addAll(cacerts);
                    } catch (CertificateException e) {
                        log.error("Cannot read trusted TLS CA certificates", e);
                    }
                }
            }
            if (tlsCaFilename.endsWith(".crt")) {
                File tlsCrtFile = new File(tlsCaFilename);
                if (tlsCrtFile.lastModified() > tlsCrtLastModified) {
                    tlsCrtLastModified = tlsCrtFile.lastModified();
                    tlsAuthorities.clear();
                    try (FileInputStream in = new FileInputStream(tlsCrtFile)) {
                        byte[] content = IOUtils.toByteArray(in);
                        X509Certificate cert = X509Util.decodeDerCertificate(content);
                        tlsAuthorities.add(cert);
                    } catch (CertificateException e) {
                        log.error("Cannot read trusted TLS CA certificates", e);
                    }
                }
            }
        }
    }
}
