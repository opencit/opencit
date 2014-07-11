/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.crypto;

import com.intel.dcsg.cpg.crypto.HmacCredential;
import com.intel.dcsg.cpg.tls.policy.TlsUtil;
import com.intel.mtwilson.KeystoreUtil;
import com.intel.mtwilson.ApiClient;
import com.intel.mtwilson.api.*;
import com.intel.mtwilson.datatypes.OsData;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Properties;
import org.apache.commons.codec.binary.Hex;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class SaveSSLCertificates {

    @Test
    public void testSaveSSLCerts() throws IOException, NoSuchAlgorithmException, KeyManagementException, CertificateEncodingException {
        X509Certificate[] certs = TlsUtil.getServerCertificates(new URL("https://10.1.71.81:8181/AttestationService"));
        for(X509Certificate cert : certs) {
            System.out.println(String.format("Subject: %s", cert.getSubjectX500Principal().getName()));
            System.out.println(String.format("Issuer: %s", cert.getIssuerX500Principal().getName()));
            System.out.println(String.format("Not Before: %s", cert.getNotBefore().toString()));
            System.out.println(String.format("Not After: %s", cert.getNotAfter().toString()));
            byte[] certBytes = cert.getEncoded();
            MessageDigest hashMd5 = MessageDigest.getInstance("MD5");
            byte[] digestMd5 = hashMd5.digest(certBytes);
            System.out.println(String.format("MD5: %s", new String(Hex.encodeHex(digestMd5))));
            MessageDigest hashSha1 = MessageDigest.getInstance("SHA-1");
            byte[] digestSha1 = hashSha1.digest(certBytes);
            System.out.println(String.format("SHA-1: %s", new String(Hex.encodeHex(digestSha1))));
            MessageDigest hashSha256 = MessageDigest.getInstance("SHA-256");
            byte[] digestSha256 = hashSha256.digest(certBytes);
            System.out.println(String.format("SHA-256: %s", new String(Hex.encodeHex(digestSha256))));
        }
    }
    
    public static void main(String[] args) throws MalformedURLException, NoSuchAlgorithmException, KeyManagementException, UnsupportedEncodingException, IOException, ApiException, SignatureException, KeyStoreException, CertificateException, ClientException {
        URL url = new URL("https://10.1.71.81:8181");
        try {
            // test API client against a server and require valid certs
            Properties p = new Properties();
            p.setProperty("mtwilson.api.ssl.requireTrustedCertificate", "true");
            p.setProperty("mtwilson.api.ssl.verifyHostname", "false");
            ApiClient c = new ApiClient(url, new HmacCredential("cloudportal@intel","nU8jTeJaFJZ7TJdMb4g4wAOljEHqwyFoRGvrsPjxrST8icOU"), p);
            List<OsData> list = c.listAllOS();
            System.out.println(String.format("Got list size %i", list.size()));            
        }
        catch(javax.net.ssl.SSLPeerUnverifiedException e) {
            System.out.println(String.format("SSL certificate for server %s is not trusted", url.toExternalForm()));
            System.out.println("Add certificate to trust store and try again? (Y/N) ");
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            String saveCertAndTryAgain = in.readLine().trim();
            if( saveCertAndTryAgain.toUpperCase().startsWith("Y") ) {
                // download server SSL certificates
                X509Certificate[] serverCertificates = TlsUtil.getServerCertificates(url);
                // create a new temporary trust store and add those certificates
                KeyStore keystore = KeyStore.getInstance("JKS");
                keystore.load(null, null);
                String aliasBasename = "tmp";
                int certificateNumber = 0;
                for(X509Certificate cert : serverCertificates) {
                    certificateNumber++;
                    String alias = String.format("%s%i", aliasBasename, certificateNumber);
                    keystore.setCertificateEntry(alias, cert);
                }
                File tmp = File.createTempFile("keystore", ".jks"); // IOException.  // creates a temporary file
                KeystoreUtil.save(keystore, "changeit", tmp);
                System.out.println("Keystore is in "+tmp.getAbsolutePath());
                // try the call again
                Properties p = new Properties();
                p.setProperty("mtwilson.api.ssl.requireTrustedCertificate", "true");
                p.setProperty("mtwilson.api.ssl.verifyHostname", "false");
                p.setProperty("mtwilson.api.truststore", tmp.getAbsolutePath()); 
                ApiClient c = new ApiClient(url, new HmacCredential("cloudportal@intel","nU8jTeJaFJZ7TJdMb4g4wAOljEHqwyFoRGvrsPjxrST8icOU"), p);
                List<OsData> list = c.listAllOS();
                System.out.println(String.format("Got list size %i", list.size()));            
                
            }
        }
        /*
         * 
mtwilson.api.clientId=
mtwilson.api.secretKey=
         * 
        System.out.println("URL: "+baseurl.toExternalForm());
        System.out.print("Root password: ");
         * 
         */
    }
    
}
