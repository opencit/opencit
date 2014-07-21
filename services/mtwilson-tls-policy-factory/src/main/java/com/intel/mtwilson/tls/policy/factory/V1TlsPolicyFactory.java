/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls.policy.factory;

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
import java.util.Collections;
import java.util.List;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author jbuhacoff
 * @deprecated need to migrate to Mt Wilson 2.0 tls policy types and factories
 */
public class V1TlsPolicyFactory {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TlsPolicyFactory.class);
    private static final V1TlsPolicyFactory instance = new V1TlsPolicyFactory();

    public static V1TlsPolicyFactory getInstance() {
        return instance;
    }

    public TlsPolicy getTlsPolicyWithKeystore(String tlsPolicyName, Resource resource) throws KeyManagementException, IOException {
        String password = My.configuration().getTlsKeystorePassword();
        SimpleKeystore tlsKeystore = new SimpleKeystore(resource, password); // XXX TODO only because txthost doesn't have the field yet... we should get the keystore from the txthost object
        TlsPolicy tlsPolicy = getTlsPolicyWithKeystore(tlsPolicyName, tlsKeystore); // XXX TODO not sure that this belongs in the http-authorization package, because policy names are an application-level thing (allowed configurations), and creating the right repository is an application-level thing too (mutable vs immutable, and underlying implementation - keystore, array, cms of pem-list.
        return tlsPolicy;
    }

    public TlsPolicy getTlsPolicyWithKeystore(SimpleKeystore tlsKeystore) throws IOException, KeyManagementException {
        return getTlsPolicyWithKeystore(null, tlsKeystore);
    }

    public TlsPolicy getTlsPolicyWithKeystore(String tlsPolicyName, SimpleKeystore tlsKeystore) throws IOException, KeyManagementException {
        if (tlsPolicyName == null) {
            tlsPolicyName = My.configuration().getDefaultTlsPolicyId();
        } // XXX for backwards compatibility with records that don't have a policy set, but maybe this isn't the place to put it - maybe it should be in the DAO that provides us the txthost object.
        
        if (tlsPolicyName == null) {
            throw new IllegalArgumentException("A TLS policy must be specified.");
        }
        
        String ucName = tlsPolicyName.toUpperCase();
        if (ucName.equals("TRUST_CA_VERIFY_HOSTNAME")) { // XXX TODO   use TlsPolicyName  
            for (X509Certificate cacert : getMtWilsonTrustedTlsCertificates()) {
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
        if (ucName.equals("TRUST_FIRST_CERTIFICATE")) {// XXX TODO   use TlsPolicyName  
//            return new TrustFirstCertificateTlsPolicy(new KeystoreCertificateRepository(tlsKeystore));
            KeystoreCertificateRepository repository = tlsKeystore.getRepository();
            return TlsPolicyBuilder.factory().strict(repository).trustDelegate(new FirstCertificateTrustDelegate(repository)).skipHostnameVerification().build();
        }
        if (ucName.equals("TRUST_KNOWN_CERTIFICATE")) {// XXX TODO   use TlsPolicyName  
//            return new TrustKnownCertificateTlsPolicy(new KeystoreCertificateRepository(tlsKeystore));
            return TlsPolicyBuilder.factory().strict(tlsKeystore.getRepository()).skipHostnameVerification().build();
        }
        if (ucName.equals("INSECURE")) {// XXX TODO   use TlsPolicyName  
            return new InsecureTlsPolicy();
        }
        throw new IllegalArgumentException("Unknown TLS Policy: " + tlsPolicyName);

    }
    
    private static TrustedTlsCertificateFileLoader cacertsLoader = new TrustedTlsCertificateFileLoader();
    public static List<X509Certificate> getMtWilsonTrustedTlsCertificates() {
        return cacertsLoader.getTlsTrustedCertificateAuthorities();
    }

    public static class TrustedTlsCertificateFileLoader {
    private static long tlsPemLastModified = 0;
    private static long tlsCrtLastModified = 0;
    private static final ArrayList<X509Certificate> tlsAuthorities = new ArrayList<>();
    
    public List<X509Certificate> getTlsTrustedCertificateAuthorities() {
        try {
            initTlsTrustedCertificateAuthorities();
            return tlsAuthorities;
        }
        catch(IOException e) {
            log.warn("Cannot initialize trusted certificate authorities: {}", e.getMessage());
            return Collections.EMPTY_LIST;
        }
    }
    // for backward compatibility, can load the mtwilson 1.x trusted tls cacerts file
    private void initTlsTrustedCertificateAuthorities() throws IOException {
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
                    try (final FileInputStream in = new FileInputStream(tlsPemFile)) {
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
                    try (final FileInputStream in = new FileInputStream(tlsCrtFile)) {
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
}
