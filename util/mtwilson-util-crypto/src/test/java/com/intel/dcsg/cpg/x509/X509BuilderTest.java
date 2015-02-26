/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.x509;

import com.intel.dcsg.cpg.crypto.RandomUtil;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import static org.junit.Assert.*;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.validation.Fault;
import java.io.IOException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.asn1.DEROctetString;

/**
 *
 * @author jbuhacoff
 */
public class X509BuilderTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(X509BuilderTest.class);

    @Test
    public void testCreateWithNoncriticalExtension() throws NoSuchAlgorithmException, IOException, CertificateEncodingException {
        byte[] randomData = RandomUtil.randomByteArray(16);
        String exampleOid = "1.2.3.4";
        boolean found = false;
        KeyPair keys = RsaUtil.generateRsaKeyPair(1024);
        X509Builder builder = X509Builder.factory().selfSigned("CN=testcert", keys).expires(30, TimeUnit.DAYS).noncriticalExtension(exampleOid, randomData);
        X509Certificate cert = builder.build();
        if( cert == null ) {
            for(Fault fault : builder.getFaults()) {
                log.error("Cannot create certificate: {}", fault.toString());
//                if( !fault.getFaults().isEmpty() ) {
//                    log.error("{} related faults", fault.getFaults().size());
//                }
            }
            fail();
        }
        assertNotNull(cert);
        assertNotNull(cert.getNonCriticalExtensionOIDs());
        assertFalse(cert.getNonCriticalExtensionOIDs().isEmpty());
        for(String oid : cert.getNonCriticalExtensionOIDs()) {
            log.debug("Non-critical extension oid:{} value:{}", oid, Base64.encodeBase64String(cert.getExtensionValue(oid)));
            if( oid.equals(exampleOid) ) {
                byte[] derEncoded = cert.getExtensionValue(oid);
                DEROctetString octetString = (DEROctetString)DEROctetString.fromByteArray(derEncoded);
                assertArrayEquals(randomData, octetString.getOctets());
                found = true;
            }
        }
        assertTrue(found);
        
        log.debug("Certificate: {}", X509Util.encodePemCertificate(cert));
        
        /**
         * Example:
         * <pre>
Certificate:
    Data:
        Version: 3 (0x2)
        Serial Number: 9532442007545174784 (0x844a098035bddb00)
    Signature Algorithm: sha256WithRSAEncryption
        Issuer: CN=testcert
        Validity
            Not Before: Feb  3 09:18:13 2015 GMT
            Not After : Mar  5 09:18:13 2015 GMT
        Subject: CN=testcert
        Subject Public Key Info:
            Public Key Algorithm: rsaEncryption
                Public-Key: (1024 bit)
                Modulus:
                    00:eb:bf:d4:4e:e7:f9:20:b2:11:50:43:ea:e7:c4:
                    4a:fa:41:cf:79:61:30:44:27:2f:2b:4d:ee:c6:e5:
                    a3:5b:5a:fb:b3:12:ae:d0:64:b0:e4:a3:c3:07:2f:
                    d1:b5:ee:94:c9:6a:1d:8c:31:f8:bc:21:04:64:e8:
                    6f:32:d8:ec:31:15:47:af:52:e7:b7:0f:46:43:15:
                    b9:75:06:43:03:c8:aa:d4:33:37:55:ec:6f:fb:c6:
                    5e:be:1e:e9:81:96:28:a5:0c:f9:1f:eb:d2:a2:a4:
                    4a:03:56:ac:ad:cb:0b:dc:1e:fc:3d:54:c2:99:64:
                    10:9d:a9:cc:91:f2:4a:bf:37
                Exponent: 65537 (0x10001)
        X509v3 extensions:
            1.2.3.4:
                ..'.....8...$..R
    Signature Algorithm: sha256WithRSAEncryption
         81:5d:64:27:a5:f6:49:6b:c5:3b:e2:d3:d6:63:49:97:06:f1:
         2a:c8:02:6b:70:96:2f:95:a9:86:e3:5d:f8:2f:93:c4:2e:18:
         e6:c7:96:54:3a:0a:b9:96:23:51:d5:16:aa:e1:7c:99:f3:31:
         de:7d:b3:d9:c1:79:82:91:39:56:72:4b:e8:44:6b:1f:3d:bd:
         20:12:e7:91:e0:b5:e9:4c:21:10:d0:54:0d:50:03:cf:74:5f:
         4e:f6:85:a3:a3:26:13:f9:56:13:dc:c8:40:f4:e0:38:6b:85:
         d1:d1:bc:24:7d:32:81:29:a9:ff:e3:f1:e7:6e:1d:eb:89:65:
         62:13
-----BEGIN CERTIFICATE-----
MIIBvzCCASigAwIBAgIJAIRKCYA1vdsAMA0GCSqGSIb3DQEBCwUAMBMxETAPBgNV
BAMTCHRlc3RjZXJ0MB4XDTE1MDIwMzA5MTgxM1oXDTE1MDMwNTA5MTgxM1owEzER
MA8GA1UEAxMIdGVzdGNlcnQwgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBAOu/
1E7n+SCyEVBD6ufESvpBz3lhMEQnLytN7sblo1ta+7MSrtBksOSjwwcv0bXulMlq
HYwx+LwhBGTobzLY7DEVR69S57cPRkMVuXUGQwPIqtQzN1Xsb/vGXr4e6YGWKKUM
+R/r0qKkSgNWrK3LC9we/D1UwplkEJ2pzJHySr83AgMBAAGjGzAZMBcGAyoDBAQQ
8wcnHNGwHvI4G6oJJAy4UjANBgkqhkiG9w0BAQsFAAOBgQCBXWQnpfZJa8U74tPW
Y0mXBvEqyAJrcJYvlamG4134L5PELhjmx5ZUOgq5liNR1Raq4XyZ8zHefbPZwXmC
kTlWckvoRGsfPb0gEueR4LXpTCEQ0FQNUAPPdF9O9oWjoyYT+VYT3MhA9OA4a4XR
0bwkfTKBKan/4/Hnbh3riWViEw==
-----END CERTIFICATE-----
         * </pre>
         * 
         */
    }
    
    
    @Test
    public void testCreateWithCriticalExtension() throws NoSuchAlgorithmException, IOException, CertificateEncodingException {
        byte[] randomData = RandomUtil.randomByteArray(16);
        String exampleOid = "1.2.3.4";
        boolean found = false;
        KeyPair keys = RsaUtil.generateRsaKeyPair(1024);
        X509Builder builder = X509Builder.factory().selfSigned("CN=testcert", keys).expires(30, TimeUnit.DAYS).criticalExtension(exampleOid, randomData);
        X509Certificate cert = builder.build();
        if( cert == null ) {
            for(Fault fault : builder.getFaults()) {
                log.error("Cannot create certificate: {}", fault.toString());
//                if( fault.getCause() != null ) {
//                    log.error("Cause", fault.getCause());
//                }
            }
            fail();
        }
        assertNotNull(cert);
        assertNotNull(cert.getCriticalExtensionOIDs());
        assertFalse(cert.getCriticalExtensionOIDs().isEmpty());
        for(String oid : cert.getCriticalExtensionOIDs()) {
            log.debug("Critical extension oid:{} value:{}", oid, Base64.encodeBase64String(cert.getExtensionValue(oid)));
            if( oid.equals(exampleOid) ) {
                byte[] derEncoded = cert.getExtensionValue(oid);
                DEROctetString octetString = (DEROctetString)DEROctetString.fromByteArray(derEncoded);
                assertArrayEquals(randomData, octetString.getOctets());
                found = true;
            }
        }
        assertTrue(found);
        
        log.debug("Certificate: {}", X509Util.encodePemCertificate(cert));
        
        /**
         * Example:
         * <pre>
Certificate:
    Data:
        Version: 3 (0x2)
        Serial Number: 13386443204757799908 (0xb9c6332b9987c3e4)
    Signature Algorithm: sha256WithRSAEncryption
        Issuer: CN=testcert
        Validity
            Not Before: Feb  3 09:28:31 2015 GMT
            Not After : Mar  5 09:28:31 2015 GMT
        Subject: CN=testcert
        Subject Public Key Info:
            Public Key Algorithm: rsaEncryption
                Public-Key: (1024 bit)
                Modulus:
                    00:aa:2a:ca:90:5c:c6:00:7d:dc:e3:a4:07:de:91:
                    56:0d:71:00:5f:e1:71:1b:f3:a3:a0:50:00:04:4c:
                    1a:d7:e2:bc:ca:1d:a6:29:26:c3:24:2c:f1:84:c4:
                    b3:d8:c1:3b:6a:7b:63:95:c7:e1:50:d2:eb:89:f9:
                    5c:3f:ad:bb:70:53:ea:41:57:20:aa:80:47:40:13:
                    a5:ed:bb:17:b7:cb:66:70:37:f9:19:b6:92:55:fa:
                    99:0e:9d:96:31:f2:8b:d2:eb:c5:f8:dc:1b:cf:bc:
                    81:28:7a:dc:14:bf:95:ea:ac:7f:1c:77:5c:61:80:
                    fc:be:ce:ec:8c:a2:6c:13:33
                Exponent: 65537 (0x10001)
        X509v3 extensions:
            1.2.3.4: critical
                .[......IPY>` ..
    Signature Algorithm: sha256WithRSAEncryption
         7f:ad:11:df:02:01:5e:2f:ed:3c:9c:81:c8:50:43:4b:c9:3e:
         84:b4:7d:cc:f8:5e:24:02:c2:4d:07:4a:88:3d:a9:ac:54:43:
         e3:4e:60:e9:ad:0a:62:92:e7:05:52:6b:0d:fa:4a:09:a6:fc:
         94:9e:40:ae:c5:43:ad:9f:ac:3e:e6:8d:dd:35:f8:db:68:a4:
         a2:74:08:8b:4a:74:cb:1f:35:b0:02:ab:d1:4f:c7:08:cc:49:
         64:35:2b:26:4b:44:a9:a3:e7:7f:e8:b2:74:97:1e:23:17:a1:
         4f:01:41:95:70:a5:16:a2:a0:2c:55:b6:49:1f:89:b0:95:d0:
         31:b1
-----BEGIN CERTIFICATE-----
MIIBwjCCASugAwIBAgIJALnGMyuZh8PkMA0GCSqGSIb3DQEBCwUAMBMxETAPBgNV
BAMTCHRlc3RjZXJ0MB4XDTE1MDIwMzA5MjgzMVoXDTE1MDMwNTA5MjgzMVowEzER
MA8GA1UEAxMIdGVzdGNlcnQwgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBAKoq
ypBcxgB93OOkB96RVg1xAF/hcRvzo6BQAARMGtfivModpikmwyQs8YTEs9jBO2p7
Y5XH4VDS64n5XD+tu3BT6kFXIKqAR0ATpe27F7fLZnA3+Rm2klX6mQ6dljHyi9Lr
xfjcG8+8gSh63BS/leqsfxx3XGGA/L7O7IyibBMzAgMBAAGjHjAcMBoGAyoDBAEB
/wQQ1FvWFKzC489JUFk+YCCi+jANBgkqhkiG9w0BAQsFAAOBgQB/rRHfAgFeL+08
nIHIUENLyT6EtH3M+F4kAsJNB0qIPamsVEPjTmDprQpikucFUmsN+koJpvyUnkCu
xUOtn6w+5o3dNfjbaKSidAiLSnTLHzWwAqvRT8cIzElkNSsmS0Spo+d/6LJ0lx4j
F6FPAUGVcKUWoqAsVbZJH4mwldAxsQ==
-----END CERTIFICATE-----
         * </pre>
         * 
         */
    }    
}
