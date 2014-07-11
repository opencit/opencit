/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls.policy.creator.impl;

import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.dcsg.cpg.tls.policy.impl.CertificateDigestTlsPolicy;
import com.intel.dcsg.cpg.tls.policy.impl.PublicKeyTlsPolicy;
import com.intel.mtwilson.tls.policy.TlsPolicyDescriptor;
import java.util.ArrayList;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class PolicyCreatorTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PolicyCreatorTest.class);
    
    @Test
    public void testCreateCertificateDigestTlsPolicy() {
        TlsPolicyDescriptor tlsPolicyDescriptor = new TlsPolicyDescriptor();
        tlsPolicyDescriptor.setPolicyType("certificate-digest");
        tlsPolicyDescriptor.setData(new ArrayList<String>());
        tlsPolicyDescriptor.getData().add("18 9a e6 e0 26 6f ae 63 8f 8c 9c b0 92 e1 ad 04 c3 a7 58 ab");
        CertificateDigestTlsPolicyCreator creator = new CertificateDigestTlsPolicyCreator();
        CertificateDigestTlsPolicy tlsPolicy = creator.createTlsPolicy(tlsPolicyDescriptor);
        assertNotNull(tlsPolicy);
    }
    
    @Test
    public void testCreatePublicKeyTlsPolicyFrom1024BitRsaPublicKey() {
        TlsPolicyDescriptor tlsPolicyDescriptor = new TlsPolicyDescriptor();
        tlsPolicyDescriptor.setPolicyType("public-key");
        tlsPolicyDescriptor.setData(new ArrayList<String>());
        tlsPolicyDescriptor.getData().add("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCAzX1ej9DHtrhj85jGN5N/36DB9MLXmz4EOyRNO5yiInDbHhCKEhivRufZ0RB3p4wvZGjowIKSj9mkB0gGyvaCdeTuDZQGXH/wRjfE6BjRrYB/l57tym8hyCyVlaD2u7WM550zIXaEmBEg8hYPb02wXksX4K6o47DCtL9dm4FDtQIDAQAB");
        PublicKeyTlsPolicyCreator creator = new PublicKeyTlsPolicyCreator();
        PublicKeyTlsPolicy tlsPolicy = creator.createTlsPolicy(tlsPolicyDescriptor);
        assertNotNull(tlsPolicy);
    }
    @Test
    public void testCreatePublicKeyTlsPolicyFrom2048BitRsaPublicKey() {
        TlsPolicyDescriptor tlsPolicyDescriptor = new TlsPolicyDescriptor();
        tlsPolicyDescriptor.setPolicyType("public-key");
        tlsPolicyDescriptor.setData(new ArrayList<String>());
        tlsPolicyDescriptor.getData().add("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAsqddwM4nX1UsIRiRlaid6EGk2V6B6Odd5M46NUXnbWUxfJAC9vy7+l89aK2MyjRc30K3OW8agTb+Ukwh8IjJAMDcMFFgdl8QXFH9daJgfvAxmuHuIw574Ggh2eFLlBcSkelrF6/XuUSO1+fJ/MgaoBO9fZXlRL0HOa3O3lbSBOJ4Yp53YrR/ewYKxgKbFrv1tYbpW4auDecf+38HvJYdFI5Ve4MJ7a/YivNK4bUJt/Bc21uQnT4fFjVAp1v+3vTQ5A2HmCM3nB3DNjwhLnMYEO+r2qYZwloyqQ2KLx8Y/mhHh5j1DCpr0/mmnOmQ0Qe8RmYL0yYO7r9mnecDDQDoKwIDAQAB");
        PublicKeyTlsPolicyCreator creator = new PublicKeyTlsPolicyCreator();
        PublicKeyTlsPolicy tlsPolicy = creator.createTlsPolicy(tlsPolicyDescriptor);
        assertNotNull(tlsPolicy);
    }
    @Test
    public void testCreatePublicKeyTlsPolicyFrom4096BitRsaPublicKey() {
        TlsPolicyDescriptor tlsPolicyDescriptor = new TlsPolicyDescriptor();
        tlsPolicyDescriptor.setPolicyType("public-key");
        tlsPolicyDescriptor.setData(new ArrayList<String>());
        tlsPolicyDescriptor.getData().add("MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEA1s2R8M4WoAhTeti/+TCSJ50Xc1c2wWcykhQDEi6qO4VFU4iCmTKqmgNuiSYAYG+mGmL6XFPzeId+2qSaRoJ1/T6iQ74iw6s64SFzNJjhHFvXGD4k9usy/51/0wezZfCWkxWm7LcLtwoMxHjHdb1O1W1Y8C5f+hLMqDlZkWrrTsxvh1zQKRLakiiRWN53Zo4bKrTlaZAtz3qAXUyfCIm9kAPaAG1nJ5GkdAM+ezRM9ejnhNCNa4nSyyydaaNWRo3APbV0J5k+GQeqgtVQvbGZ1GGWgYJqVGsOk9320VWBntRMPDtPpx9dZsUBtMT1quj8mzDr1djn8aPnTF4/z1c61YwVIjczJUa45X+KI+Qz4sB0V/gF7G4DoRCv0uwV7lE2hGnn9ezILT8HZHX0OJ2wRBJ9c+IKP5TEytfJpDm3b3f+s7v8aS3u7MC4PZ989ieUDybyx9pHsOWSXmQ3Aj/JpCSD9EjHJ6ATibdG3an8N7a2q3oydrGEbIjzPRBXzdxmJQ17UBgDJYCE1nUVv56S7LocV15D4UmTkz3i+H2WcBDbEYny/I8SJHHU2jQ1F/zTfGuNjGqQKYw0PZmoZ6PCN6RXKU+pHi/Pg8yMRK95KWN7l8JCAYD1qB/bbNb3XMEE2FdxJRHmslgggaAIlvdvZwZYyfhbE/UA+e5O+jMZYEcCAwEAAQ==");
        PublicKeyTlsPolicyCreator creator = new PublicKeyTlsPolicyCreator();
        PublicKeyTlsPolicy tlsPolicy = creator.createTlsPolicy(tlsPolicyDescriptor);
        assertNotNull(tlsPolicy);
    }
    @Test
    public void testCreatePublicKeyTlsPolicyFromCertificate() {
        TlsPolicyDescriptor tlsPolicyDescriptor = new TlsPolicyDescriptor();
        tlsPolicyDescriptor.setPolicyType("public-key");
        tlsPolicyDescriptor.setData(new ArrayList<String>());
        tlsPolicyDescriptor.getData().add("MIICJTCCAY6gAwIBAgIIebi9bYW6Z4gwDQYJKoZIhvcNAQELBQAwVTELMAkGA1UEBhMCVVMxHDAaBgNVBAoTE1RydXN0ZWQgRGF0YSBDZW50ZXIxEjAQBgNVBAsTCU10IFdpbHNvbjEUMBIGA1UEAwwLQ049c2FtcGxlXzAwHhcNMTQwNzExMTQxNDU5WhcNMTQwNzEyMTQxNDU5WjBVMQswCQYDVQQGEwJVUzEcMBoGA1UEChMTVHJ1c3RlZCBEYXRhIENlbnRlcjESMBAGA1UECxMJTXQgV2lsc29uMRQwEgYDVQQDDAtDTj1zYW1wbGVfMDCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEA0SLMa7UTLqJVYsPr6wuMn2HaRvpc0N18tqIS23FUnGLCI0wZpV74hUlSjbv5+D/OQOKhHjQzFlUK6dHaa8XFxa8WC633DY+iZNajRsl3XN1W51uQzI1wrxtOQAX7h34XmqM2diGsl4uk/ysiqwS19A2uWRRQ3WeDPzrdnDthpyUCAwEAATANBgkqhkiG9w0BAQsFAAOBgQBLsXUyoCmDXRafdabCE0r2djgCpIT7jxMYkDz67qwUMztwZRbaNF/um05kHDBhZCvoUH/NjWZ/hXnAB7mg+djJOQ2DEa0oi8eRewJE1CLXusp5tJvzkFrgYTQ0eoYnS97QJbNn//LaACKj+7aIdSx1hxKQhWXpir8vvHoLLhdCDQ==");
        PublicKeyTlsPolicyCreator creator = new PublicKeyTlsPolicyCreator();
        PublicKeyTlsPolicy tlsPolicy = creator.createTlsPolicy(tlsPolicyDescriptor);
        assertNotNull(tlsPolicy);
    }
    
}
