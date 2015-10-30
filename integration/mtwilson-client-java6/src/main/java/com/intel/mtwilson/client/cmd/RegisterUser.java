/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.client.cmd;

import com.intel.mtwilson.ApiClient;
import com.intel.mtwilson.KeystoreUtil;
import com.intel.mtwilson.client.AbstractCommand;
import com.intel.dcsg.cpg.crypto.RsaCredentialX509;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.datatypes.ApiClientCreateRequest;
import com.intel.dcsg.cpg.io.Filename;
import com.intel.dcsg.cpg.tls.policy.TlsUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *     HostTrustResponse getHostTrust(Hostname hostname) throws IOException, ApiException, SignatureException;
 * 
 * @author jbuhacoff
 */
public class RegisterUser extends AbstractCommand {
    private Logger log = LoggerFactory.getLogger(getClass());
    
    @Override
    public void execute(String[] args) throws Exception {
      //  ApiClient api = getClient();
            if( args.length < 3 ) {
                System.err.println("Usage: RegisterUser /path/to/username.jks ServiceURL Role1[,Role2,...] [password]");
                System.err.println("ServiceURL is the URL to the management service");
                System.err.println("Try these roles:  Attestation,Whitelist,Security");
                System.exit(1);
            }
            // args[1] should be path to keystore (/path/to/directory/username.jks)
            File keystoreFile = new File(args[0]);
            // args[2] should be the url of the server to register with
            URL server = new URL(args[1]);
            // args[3] should be the roles being requested, comma-separated values  (Attestation,Whitelist,Security)
            String[] roles = StringUtils.split(args[2], ",");

            String username = Filename.decode(keystoreFile.getName().substring(0, keystoreFile.getName().lastIndexOf("."))); // username is everything before ".jks"
            String password = null;
            String tlsProtocol = "TLS"; // issue #870 allow user to specify tls protocol version, default to TLS
            // args[4] is optional password plaintext (not recommended) or environment variable name (recommended) (if not provided we will prompt)
            if( args.length > 3 ) { password = args[3]; }
            if( args.length > 4 ) { tlsProtocol = args[4]; }

            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            if( password == null || password.isEmpty() ) {
                System.out.print("Password: ");
                password = in.readLine();
            }
            else if( password.startsWith("env:") && password.length() > 4 ) {
                String varName = password.substring(4);
                password = System.getenv(varName);
            }

            if( password == null || password.isEmpty() || password.length() < 6 ) {
                System.err.println("The password must be at least six characters");
                System.exit(1);
            }
            
            
            SimpleKeystore keystore = new SimpleKeystore(keystoreFile, password);
            // download server's ssl certificates and add them to the keystore  and display for user to confirm later
            String[] tlsCertAliases0 = keystore.listTrustedSslCertificates();
            TlsUtil.addSslCertificatesToKeystore(keystore, server, tlsProtocol);
            String[] tlsCertAliases = keystore.listTrustedSslCertificates();
            String[] newTlsCertAliases = elementsAdded(tlsCertAliases0, tlsCertAliases);
            for(String alias : newTlsCertAliases) {
                X509Certificate cert = keystore.getX509Certificate(alias);
                System.out.println(String.format("Added TLS Certificate for %s with SHA-256 fingerprint %s", cert.getSubjectX500Principal().getName(), X509Util.sha256fingerprint(cert)));
            }
            
            // register the user with the server
            
            RsaCredentialX509 rsaCredential = keystore.getRsaCredentialX509(username, password);
            Properties p = new Properties();
            /*
            p.setProperty("mtwilson.api.ssl.policy", "TRUST_CA_VERIFY_HOSTNAME"); // must be secure out of the box!
            p.setProperty("mtwilson.api.ssl.requireTrustedCertificate", "true");
            p.setProperty("mtwilson.api.ssl.verifyHostname", "true");            
            */
            p.setProperty("mtwilson.api.ssl.policy", "TRUST_FIRST_CERTIFICATE");
            ApiClient c = new ApiClient(server, rsaCredential, keystore, new MapConfiguration(p)); //ConfigurationFactory.fromSystemEnvironment());
            ApiClientCreateRequest user = new ApiClientCreateRequest();
            user.setCertificate(rsaCredential.getCertificate().getEncoded());
            user.setRoles(roles);
            try {
                c.register(user);
            }
            catch(javax.net.ssl.SSLException e) {
                if( e.getMessage().contains("hostname in certificate didn't match") && !"false".equals(System.getenv("MTWILSON_API_SSL_VERIFY_HOSTNAME")) ) {
                    System.err.println(e.getMessage());
                    System.out.print("Do you want to continue anyway? [Y/N] ");
                    String ignoreHostname = in.readLine();
                    if( ignoreHostname != null && ignoreHostname.length() > 0 && ignoreHostname.toUpperCase().charAt(0) == 'Y' ) {
                        System.err.println("To avoid this prompt in the future, address the server by the hostname in its SSL certificate or set the environment variable MTWILSON_API_SSL_VERIFY_HOSTNAME=false");
                        Properties p2 = new Properties();
                        p2.setProperty("mtwilson.api.ssl.verifyHostname", "false");
                        c = new ApiClient(server, rsaCredential, keystore, new MapConfiguration(p2));
                        c.register(user);
                    }
                    else {
                        return;
                    }
                }
                else {
                    throw e;
                }
            }
            
                // download all ca certs from the server (root ca, privacy ca, saml ca, etc)  
                try {
                    Set<X509Certificate> cacerts = c.getRootCaCertificates();
                    for(X509Certificate cacert : cacerts) {
                        keystore.addTrustedCaCertificate(cacert, cacert.getSubjectX500Principal().getName()); 
                        log.debug("Added CA Certificate with alias {}, subject {}, fingerprint {}, from server {}",  cacert.getSubjectX500Principal().getName(), cacert.getSubjectX500Principal().getName(), DigestUtils.shaHex(cacert.getEncoded()), server.getHost());
                    }
                }
            catch(Exception e) {
                log.error("Cannot download Mt Wilson Root CA certificate from server");
            }
            // download privacy ca certificates
            try {
                Set<X509Certificate> cacerts = c.getPrivacyCaCertificates();
                for(X509Certificate cacert : cacerts) {
                    keystore.addTrustedCaCertificate(cacert, cacert.getSubjectX500Principal().getName()); 
                    log.debug("Added Privacy CA Certificate with alias {}, subject {}, fingerprint {}, from server {}", cacert.getSubjectX500Principal().getName(), cacert.getSubjectX500Principal().getName(), DigestUtils.shaHex(cacert.getEncoded()), server.getHost());
                }
            }
            catch(Exception e) {
                log.error("Cannot download Privacy CA certificate from server");
            }
            // download server's saml certificate and save in the keystore
            try {
                Set<X509Certificate> cacerts = c.getSamlCertificates();
                for(X509Certificate cert : cacerts) {
                    if( cert.getBasicConstraints() == -1 ) {  // -1 indicates the certificate is not a CA cert; so we add it as the saml cert
                        keystore.addTrustedSamlCertificate(cert, server.getHost());
                        log.debug("Added SAML Certificate with alias {}, subject {}, fingerprint {}, from server {}", cert.getSubjectX500Principal().getName(), cert.getSubjectX500Principal().getName(), DigestUtils.shaHex(cert.getEncoded()), server.getHost());
                    }
                    else {
                        keystore.addTrustedCaCertificate(cert, cert.getSubjectX500Principal().getName()); 
                        log.debug("Added SAML CA Certificate with alias {}, subject {}, fingerprint {}, from server {}", cert.getSubjectX500Principal().getName(), cert.getSubjectX500Principal().getName(), DigestUtils.shaHex(cert.getEncoded()), server.getHost());
                    }
                }
            }
            catch(Exception e) {
                log.error("Cannot download SAML certificate from server");
            }
            keystore.save();        
            System.out.println("OK");
    }

    private String[] elementsAdded(String[] from, String[] to) {
        HashSet<String> added = new HashSet<String>();
        added.addAll(Arrays.asList(to));
        added.removeAll(Arrays.asList(from));
        return added.toArray(new String[added.size()]);
    }
}
