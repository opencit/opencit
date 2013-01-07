/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.crypto;

import com.intel.mtwilson.crypto.RsaUtil;
import com.intel.mtwilson.crypto.X509Builder;
import com.intel.mtwilson.crypto.X509Util;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.commons.codec.binary.Base64;
import org.junit.Test;
import sun.security.x509.BasicConstraintsExtension;
import sun.security.x509.OIDMap;
import sun.security.x509.PKIXExtensions;

/**
 *
 * @author jbuhacoff
 */
public class X509UtilTest {
    @Test
    public void testCreateCaCertificate() throws NoSuchAlgorithmException, CertificateParsingException, IOException, CertificateEncodingException, CertificateException {
        KeyPair caKeys = RsaUtil.generateRsaKeyPair(1024);
        X509Certificate caCert = X509Builder.factory().selfSigned("CN=testca", caKeys).expires(30, TimeUnit.DAYS).keyUsageCertificateAuthority().build();
        System.out.println("created CA cert with CA flag: "+caCert.getBasicConstraints());
//        System.out.println("HERE IS THE PEM: "+X509Util.encodePemCertificate(caCert));
        System.out.println("basic constraints: "+caCert.getBasicConstraints());
        boolean keyUsage[] = caCert.getKeyUsage();
        if( keyUsage != null ) {
            System.out.println("key usage CA: "+keyUsage[5]);
        }
        else { System.out.println("keyUsage boolean[] is NULL"); }
        
        
    // not going to have extended key usages. the keys would be anyExtendedKeyUsage, serverAuth, clientAuth, codeSigning, emailProtection, ipsecEndSystem, ipsecTunnel, ipsecUser, timeStamping, OCSPSigning
            List<String> extKeyUsage = caCert.getExtendedKeyUsage();
            if( extKeyUsage != null ) {
            System.out.println("ExtendedKeyUsage count: "+extKeyUsage.size());
            for(String str: extKeyUsage) { System.out.println("ExtendedKeyUsage: "+str); }
            } else { System.out.println("ExtendedKeyUsage list is NULL"); }

    }
    
    
    @Test
    public void testReadCaCertificate() throws CertificateException {
        String pem = //"-----BEGIN CERTIFICATE-----\n" +
"MIIBwjCCASugAwIBAgIIUV/sIM8fxt0wDQYJKoZIhvcNAQELBQAwETEPMA0GA1UEAxMGdGVzdGNh\n" +
"MB4XDTEzMDEwNjA4MTkyNloXDTEzMDIwNTA4MTkyNlowETEPMA0GA1UEAxMGdGVzdGNhMIGfMA0G\n" +
"CSqGSIb3DQEBAQUAA4GNADCBiQKBgQCw4KY7iTN1O4fxVsZFnzqPM3EMDfduTbeNdvj0wIRCMffA\n" +
"lSlb8Ah6HEcw60jB93Fhc+a9ycd9k0VdmcRCAXlmAWd5RSk/Rw1G0Pr4M0rD4keUGGcu9ftXbPnm\n" +
"LM/wjnNaMjCJsItq6n591R7OFxeoWNd+wP5mKQQ0duIyZtMVjQIDAQABoyMwITAOBgNVHQ8BAf8E\n" +
"BAMCAgQwDwYDVR0TAQH/BAUwAwEB/zANBgkqhkiG9w0BAQsFAAOBgQCupt8BZuk1yBpuK+eA0SMh\n" +
"EeUXVCL3Hbc5dTjvnVqyUE+G1VFFZVttKuTtk5e2W8kjHY4A6Pab7HuWwlxAVDwBH/1OY3Nij1oS\n" +
"YEjPL8kq/nVEimCV87f2+OEkOH/jIiZPitwGKW+N+rARuBds9GF9s8njz/u5GETCdyuzvAVFQQ==\n" ;
//"-----END CERTIFICATE-----";
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        byte[] der = Base64.decodeBase64(pem);
        ByteArrayInputStream in = new ByteArrayInputStream(der);
        X509Certificate cert = (X509Certificate)certFactory.generateCertificate(in);
        int basicConstraints = cert.getBasicConstraints();
        boolean[] keyUsage = cert.getKeyUsage(); // index 5 is CA
//        List<String> extendedKeyUsage = cert.getExtendedKeyUsage();
        System.out.println("CA basic constraint: "+basicConstraints);
        System.out.println("CA key usage: "+keyUsage==null?"NULL":keyUsage[5]);
//        System.out.println("CA extended key usage: "+extendedKeyUsage==null?"NULL":extendedKeyUsage.get(0));
    }
}
