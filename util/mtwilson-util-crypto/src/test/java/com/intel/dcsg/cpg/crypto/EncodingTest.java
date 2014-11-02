/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto;

import java.io.IOException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import org.apache.commons.codec.binary.Base64;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class EncodingTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EncodingTest.class);

    private static ArrayList<Sample> samples = new ArrayList<>();
    
    public static class Sample {
        public KeyPair keypair;
        public X509Certificate certificate;
    }
    
    @BeforeClass
    public static void createSamples() throws NoSuchAlgorithmException, CryptographyException, IOException {
        for(int i=0; i<10; i++) {
            Sample sample = new Sample();
            sample.keypair = RsaUtil.generateRsaKeyPair(4096);
            sample.certificate = RsaUtil.generateX509Certificate(String.format("CN=sample_%d",i), sample.keypair, 1);
            samples.add(sample);
        }
    }
    
    @Test
    public void testCertificateEncoding() throws CertificateEncodingException {
        for(Sample sample : samples) {
            log.debug("Certificate: {}", Base64.encodeBase64String(sample.certificate.getEncoded())); // all start with MIICJ
            // MIICJTCCAY6gAwIBAgIIebi9bYW6Z4gwDQYJKoZIhvcNAQELBQAwVTELMAkGA1UEBhMCVVMxHDAaBgNVBAoTE1RydXN0ZWQgRGF0YSBDZW50ZXIxEjAQBgNVBAsTCU10IFdpbHNvbjEUMBIGA1UEAwwLQ049c2FtcGxlXzAwHhcNMTQwNzExMTQxNDU5WhcNMTQwNzEyMTQxNDU5WjBVMQswCQYDVQQGEwJVUzEcMBoGA1UEChMTVHJ1c3RlZCBEYXRhIENlbnRlcjESMBAGA1UECxMJTXQgV2lsc29uMRQwEgYDVQQDDAtDTj1zYW1wbGVfMDCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEA0SLMa7UTLqJVYsPr6wuMn2HaRvpc0N18tqIS23FUnGLCI0wZpV74hUlSjbv5+D/OQOKhHjQzFlUK6dHaa8XFxa8WC633DY+iZNajRsl3XN1W51uQzI1wrxtOQAX7h34XmqM2diGsl4uk/ysiqwS19A2uWRRQ3WeDPzrdnDthpyUCAwEAATANBgkqhkiG9w0BAQsFAAOBgQBLsXUyoCmDXRafdabCE0r2djgCpIT7jxMYkDz67qwUMztwZRbaNF/um05kHDBhZCvoUH/NjWZ/hXnAB7mg+djJOQ2DEa0oi8eRewJE1CLXusp5tJvzkFrgYTQ0eoYnS97QJbNn//LaACKj+7aIdSx1hxKQhWXpir8vvHoLLhdCDQ==
            // 48, -126, 2, 37, 48, -126, 1, -114, -96, 3, 2, 1, 2, 2, 8
            //          SEQUENCE (30 82) with 02 25 bytes
            //          SEQUENCE (30 82) with 01 8E bytes
            //          INDEX 0 (A0 03) and INTEGER (02) LENGTH (01 byte)  VALUE  02  
            log.debug("Certificate: {}", sample.certificate.getEncoded()); 
        }
    }
    
    @Test
    public void testPublicKeyEncoding() throws CertificateEncodingException {
        for(Sample sample : samples) {
            log.debug("Public Key: {}", Base64.encodeBase64String(sample.certificate.getPublicKey().getEncoded())); 
            log.debug("Public Key: {}", sample.certificate.getPublicKey().getEncoded()); 
            // 1024 bit all start with MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQ         48, -127, -97,     48, 13, 6, 9, 42, -122,   72, -122, -9, 13, 1, 1, 1, 5, 0, 3, -127, -115,      0, 48, -127, -119,  2, -127, -127,       0
            //          example: MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCAzX1ej9DHtrhj85jGN5N/36DB9MLXmz4EOyRNO5yiInDbHhCKEhivRufZ0RB3p4wvZGjowIKSj9mkB0gGyvaCdeTuDZQGXH/wRjfE6BjRrYB/l57tym8hyCyVlaD2u7WM550zIXaEmBEg8hYPb02wXksX4K6o47DCtL9dm4FDtQIDAQAB
            //          7 bytes (30 81 9F 30 0D 06 09) then OID (2A  86 48 86 F7 0D 01 01 01)
            //          meaning of the 7 bytes:
            //          SEQUENCE (30 81) with 9F bytes
            //          SEQUENCE (30) with 0D bytes
            //          OID (06) with 09 bytes
            // 2048 bit all start with MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA   48, -126,   1, 34, 48, 13, 6, 9, 42, -122,   72, -122, -9, 13, 1, 1, 1, 5, 0, 3, -126,    1, 15,  0, 48, -126,    1, 10,    2, -126, 1, 1, 0
            //          example: MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAsqddwM4nX1UsIRiRlaid6EGk2V6B6Odd5M46NUXnbWUxfJAC9vy7+l89aK2MyjRc30K3OW8agTb+Ukwh8IjJAMDcMFFgdl8QXFH9daJgfvAxmuHuIw574Ggh2eFLlBcSkelrF6/XuUSO1+fJ/MgaoBO9fZXlRL0HOa3O3lbSBOJ4Yp53YrR/ewYKxgKbFrv1tYbpW4auDecf+38HvJYdFI5Ve4MJ7a/YivNK4bUJt/Bc21uQnT4fFjVAp1v+3vTQ5A2HmCM3nB3DNjwhLnMYEO+r2qYZwloyqQ2KLx8Y/mhHh5j1DCpr0/mmnOmQ0Qe8RmYL0yYO7r9mnecDDQDoKwIDAQAB
            //          8 bytes (30 82 01 22 30 0D 06 09) then OID (2A 86 48 86 F7 0D 01 01)
            //          meaning of the 8 bytes:
            //          SEQUENCE (30 82) with 02 22 bytes
            //          SEQUENCE (30) with 0D bytes
            //          OID (06) with 09 bytes
            // 4096 bit all start with MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEA   48, -126,   2, 34, 48, 13, 6, 9, 42, -122,   72, -122, -9, 13, 1, 1, 1, 5, 0, 3, -126,    2, 15,  0, 48, -126,    2, 10,    2, -126, 2, 1, 0
            //          example: MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEA1s2R8M4WoAhTeti/+TCSJ50Xc1c2wWcykhQDEi6qO4VFU4iCmTKqmgNuiSYAYG+mGmL6XFPzeId+2qSaRoJ1/T6iQ74iw6s64SFzNJjhHFvXGD4k9usy/51/0wezZfCWkxWm7LcLtwoMxHjHdb1O1W1Y8C5f+hLMqDlZkWrrTsxvh1zQKRLakiiRWN53Zo4bKrTlaZAtz3qAXUyfCIm9kAPaAG1nJ5GkdAM+ezRM9ejnhNCNa4nSyyydaaNWRo3APbV0J5k+GQeqgtVQvbGZ1GGWgYJqVGsOk9320VWBntRMPDtPpx9dZsUBtMT1quj8mzDr1djn8aPnTF4/z1c61YwVIjczJUa45X+KI+Qz4sB0V/gF7G4DoRCv0uwV7lE2hGnn9ezILT8HZHX0OJ2wRBJ9c+IKP5TEytfJpDm3b3f+s7v8aS3u7MC4PZ989ieUDybyx9pHsOWSXmQ3Aj/JpCSD9EjHJ6ATibdG3an8N7a2q3oydrGEbIjzPRBXzdxmJQ17UBgDJYCE1nUVv56S7LocV15D4UmTkz3i+H2WcBDbEYny/I8SJHHU2jQ1F/zTfGuNjGqQKYw0PZmoZ6PCN6RXKU+pHi/Pg8yMRK95KWN7l8JCAYD1qB/bbNb3XMEE2FdxJRHmslgggaAIlvdvZwZYyfhbE/UA+e5O+jMZYEcCAwEAAQ==
            //          8 bytes (30 82 02 22 30 0D 06 09) then OID (2A 86 48 86 F7 0D 01 01)
            //          meaning of the 8 bytes:
            //          SEQUENCE (30 82) with 01 22 bytes
            //          SEQUENCE (30) with 0D bytes
            //          OID (06) with 09 bytes
        }
    }
}
