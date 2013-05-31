/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.tls.policy;

import com.intel.dcsg.cpg.io.FileResource;
import com.intel.dcsg.cpg.io.Resource;
import com.intel.dcsg.cpg.tls.policy.impl.ConsoleTrustDelegate;
import com.intel.dcsg.cpg.tls.policy.impl.FirstCertificateTrustDelegate;
import com.intel.dcsg.cpg.x509.repository.CertificateRepository;
import com.intel.dcsg.cpg.x509.repository.KeystoreCertificateRepository;
import com.intel.dcsg.cpg.x509.repository.MutableCertificateRepository;
import com.intel.dcsg.cpg.x509.repository.PemMutableCertificateRepository;
import java.io.File;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

/**
 * This class provides a convenient way to instantiate common TlsPolicy choices.
 * You can create something similar in your own project to provide additional pre-built policies.
 * 
 * If you need to create a TlsPolicy based on a configuration such as a properties file,
 * you can create a simple wrapper around TlsPolicyFactory in your project to interpret the
 * properties and create a TlsPolicy. See the example in the Test Packages called TlsPolicyFactoryTest.
 * 
 * If you need more fine-grained control over the policy, use TlsPolicyBuilder. Note that at this
 * time the most common choices are available via TlsPolicyFactory.
 * 
 * @author jbuhacoff
 */
public class TlsPolicyFactory {
    
    /**
     * This policy is insecure because it does not provide authentication, encryption, or integrity.
     * @return 
     */
    public static TlsPolicy insecure() {
        return TlsPolicyBuilder.factory().insecure().build();
    }

    /**
     * This policy provides authentication with hostname verification, encryption, and integrity. 
     * 
     * @param repository of trusted certificates or certificate authorities
     * @return 
     */
    public static TlsPolicy strict(CertificateRepository repository) {
        return TlsPolicyBuilder.factory().strict(repository).build();
    }

    /**
     * 
     * @param keystorePath relative or absolute path to a java KeyStore (.jks) file
     * @param password
     * @return 
     */
    public static TlsPolicy strictWithKeystore(String keystorePath, String password)  throws IOException, CertificateException {
        return strictWithKeystore(new FileResource(new File(keystorePath)), password);
    }

    /**
     * 
     * @param keystoreResource containing a java KeyStore (.jks) file
     * @param password
     * @return 
     */
    public static TlsPolicy strictWithKeystore(Resource keystoreResource, String password) throws IOException, CertificateException {
        try {
            CertificateRepository repository = new KeystoreCertificateRepository(keystoreResource, password);
            return strict(repository);
        }
        catch(KeyStoreException e) {
            throw new IOException("Cannot open keystore: "+e.toString(), e);
        }
        catch(NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("Cannot open keystore: "+e.toString(), e);
        }
    }
    
    /**
     * Similar to the strict policy with two exceptions:
     * 1. hostname verification is disabled; this can be insecure because the host's key could be stolen and used in another host
     * 2. if the certificate repository is empty, the first certificate encountered is automatically trusted and added to the repository
     * @param repository
     * @return
     * @throws IOException
     * @throws CertificateException 
     */
    public static TlsPolicy trustFirstCertificate(MutableCertificateRepository repository) throws IOException, CertificateException {
        TrustDelegate delegate = new FirstCertificateTrustDelegate(repository);
        return TlsPolicyBuilder.factory().strict(repository).trustDelegate(delegate).skipHostnameVerification().build();
    }
    
    /**
     * Similar to the strict policy with one exception:
     * Hostname verification is disabled; this can be insecure because the host's key could be stolen and used in another host
     * @param repository
     * @return
     * @throws IOException
     * @throws CertificateException 
     */
    public static TlsPolicy trustKnownCertificate(CertificateRepository repository) throws IOException, CertificateException {
        return TlsPolicyBuilder.factory().strict(repository).skipHostnameVerification().build();
    }
    
    
    /**
     * Similar to the strict policy with two exceptions:
     * 1. hostname verification is disabled; this can be insecure because the host's key could be stolen and used in another host
     * 2. if the certificate repository is empty, the first certificate encountered is automatically trusted and added to the repository
     * @param keystorePath relative or absolute path to a java KeyStore (.jks) file
     * @param password
     * @return
     * @throws IOException
     * @throws CertificateException 
     */
    public static TlsPolicy trustFirstCertificateWithKeystore(String keystorePath, String password) throws IOException, CertificateException {
        return trustFirstCertificateWithKeystore(new FileResource(new File(keystorePath)), password);
    }
    
    /**
     * Similar to the strict policy with two exceptions:
     * 1. hostname verification is disabled; this can be insecure because the host's key could be stolen and used in another host
     * 2. if the certificate repository is empty, the first certificate encountered is automatically trusted and added to the repository
     * @param keystoreResource containing a java KeyStore (.jks) file
     * @param password
     * @return
     * @throws IOException
     * @throws CertificateException 
     */
    public static TlsPolicy trustFirstCertificateWithKeystore(Resource keystoreResource, String password) throws IOException, CertificateException {
        try {
            MutableCertificateRepository repository = new KeystoreCertificateRepository(keystoreResource, password);
            return trustFirstCertificate(repository);
        }
        catch(KeyStoreException e) {
            throw new IOException("Cannot open keystore: "+e.toString(), e);
        }
        catch(NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("Cannot open keystore: "+e.toString(), e);
        }
    }
    
    /**
     * Similar to the strict policy with one exception:
     * Hostname verification is disabled; this can be insecure because the host's key could be stolen and used in another host
     * @param keystorePath relative or absolute path to a java KeyStore (.jks) file
     * @param password
     * @return
     * @throws IOException
     * @throws CertificateException 
     */
    public static TlsPolicy trustKnownCertificateWithKeystore(String keystorePath, String password) throws IOException, CertificateException {
        return trustKnownCertificateWithKeystore(new FileResource(new File(keystorePath)), password);
    }
    
    
    /**
     * Similar to the strict policy with one exception:
     * Hostname verification is disabled; this can be insecure because the host's key could be stolen and used in another host
     * @param keystoreResource containing a java KeyStore (.jks) file
     * @param password
     * @return
     * @throws IOException
     * @throws CertificateException 
     */
    public static TlsPolicy trustKnownCertificateWithKeystore(Resource keystoreResource, String password) throws IOException, CertificateException {
        try {
            CertificateRepository repository = new KeystoreCertificateRepository(keystoreResource, password);
            return trustKnownCertificate(repository);
        }
        catch(KeyStoreException e) {
            throw new IOException("Cannot open keystore: "+e.toString(), e);
        }
        catch(NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("Cannot open keystore: "+e.toString(), e);
        }
    }
    
    
    /**
     * XXX THIS WAS USED MAINLY FOR DEMONSTRATION PURPOSES; THE .PEM FILE DOES NOT HAVE ANY AUTHENTICATION
     * SO IT IS INSECURE TO USE IT
     * Uses a file called "trusted-certs.pem" in the current directory as the certificate repository.
     * Uses the ConsoleTrustDelegate, so it is only useful in applications that have a console/terminal.
     * @return 
     */
    /*
    public static TlsPolicy consoleBrowser() throws IOException, CertificateException {
        MutableCertificateRepository repository = new PemMutableCertificateRepository(new FileResource(new File("trusted-certs.pem")));
        TrustDelegate delegate = new ConsoleTrustDelegate(repository);
        return TlsPolicyBuilder.factory().strict(repository).trustDelegate(delegate).build();        
    }*/
    
    /**
     * Uses the specified file as the certificate repository.
     * Uses the ConsoleTrustDelegate, so it is only useful in applications that have a console/terminal.
     * @return 
     */
    /*
    public static TlsPolicy consoleBrowserWithRepository(File trustedCertificates) throws IOException, CertificateException {
        MutableCertificateRepository repository = new PemMutableCertificateRepository(new FileResource(trustedCertificates));
        TrustDelegate delegate = new ConsoleTrustDelegate(repository);
        return TlsPolicyBuilder.factory().strict(repository).trustDelegate(delegate).build();        
    }

    public static TlsPolicy consoleBrowserWithRepository(Resource trustedCertificates) throws IOException, CertificateException {
        MutableCertificateRepository repository = new PemMutableCertificateRepository(trustedCertificates);
        TrustDelegate delegate = new ConsoleTrustDelegate(repository);
        return TlsPolicyBuilder.factory().strict(repository).trustDelegate(delegate).build();        
    }

    public static TlsPolicy trustFirstCertificate(File trustedCertificates) throws IOException, CertificateException {
        MutableCertificateRepository repository = new PemMutableCertificateRepository(new FileResource(trustedCertificates));
        TrustDelegate delegate = new FirstCertificateTrustDelegate(repository);
        return TlsPolicyBuilder.factory().strict(repository).trustDelegate(delegate).skipHostnameVerification().build();
    }

    public static TlsPolicy trustFirstCertificate(Resource trustedCertificates) throws IOException, CertificateException {
        MutableCertificateRepository repository = new PemMutableCertificateRepository(trustedCertificates);
        TrustDelegate delegate = new FirstCertificateTrustDelegate(repository);
        return TlsPolicyBuilder.factory().strict(repository).trustDelegate(delegate).skipHostnameVerification().build();
    }*/
    
}
