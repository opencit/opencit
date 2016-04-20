/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls.policy.creator.impl;

import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.dcsg.cpg.tls.policy.impl.CertificateDigestTlsPolicy;
import com.intel.dcsg.cpg.tls.policy.impl.CertificateTlsPolicy;
import com.intel.dcsg.cpg.tls.policy.impl.PublicKeyTlsPolicy;
import com.intel.mtwilson.tls.policy.TlsPolicyDescriptor;
import java.util.ArrayList;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
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
    public void testCreatePublicKeyTlsPolicyFrom1024BitRsaPublicKeyHex() {
        TlsPolicyDescriptor tlsPolicyDescriptor = new TlsPolicyDescriptor();
        tlsPolicyDescriptor.setPolicyType("public-key");
        tlsPolicyDescriptor.setData(new ArrayList<String>());
        tlsPolicyDescriptor.getData().add("30 81 9f 30 0d 06 09 2a 86 48 86 f7 0d 01 01 01 05 00 03 81 8d 00 30 81 89 02 81 81 00 80 cd 7d 5e 8f d0 c7 b6 b8 63 f3 98 c6 37 93 7f df a0 c1 f4 c2 d7 9b 3e 04 3b 24 4d 3b 9c a2 22 70 db 1e 10 8a 12 18 af 46 e7 d9 d1 10 77 a7 8c 2f 64 68 e8 c0 82 92 8f d9 a4 07 48 06 ca f6 82 75 e4 ee 0d 94 06 5c 7f f0 46 37 c4 e8 18 d1 ad 80 7f 97 9e ed ca 6f 21 c8 2c 95 95 a0 f6 bb b5 8c e7 9d 33 21 76 84 98 11 20 f2 16 0f 6f 4d b0 5e 4b 17 e0 ae a8 e3 b0 c2 b4 bf 5d 9b 81 43 b5 02 03 01 00 01");
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
    
    @Test
    public void testCreateCertificatePolicyFromCertificate() {
        TlsPolicyDescriptor tlsPolicyDescriptor = new TlsPolicyDescriptor();
        tlsPolicyDescriptor.setPolicyType("certificate");
        tlsPolicyDescriptor.setData(new ArrayList<String>());
        tlsPolicyDescriptor.getData().add(
"MIIBoTCCAQoCCQCUbSaqp+HiojANBgkqhkiG9w0BAQUFADAVMRMwEQYDVQQDEwox\n" +
"MC4xLjcxLjkxMB4XDTE0MDQxNDIyMjYwMFoXDTI0MDQxMTIyMjYwMFowFTETMBEG\n" +
"A1UEAxMKMTAuMS43MS45MTCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEAw0WR\n" +
"uXZRsdcmtNb6IFHenIuq/WPEM+R7zCpVlaC4iujCGSBRjeGKyvy8ZS09c4NXv7/T\n" +
"CYYitdn8O6560gY9VTlTm6sMeGYuFb0i8BkBXIiVgBhJqoTVpJXc3+DhU/tXHPNg\n" +
"yYvd+ykkRZsDtNEeunJ7yY3A+pti8jeNOHSSXHkCAwEAATANBgkqhkiG9w0BAQUF\n" +
"AAOBgQCGFGDSPUCI6UXnjQ43vg3U9Ges4O737VLlf8FIh0tGP0GTBHK10HWOJG3Q\n" +
"DaK4tHuDbQQ5fzUKiwOUscryZgZ2elNDTBwoWcREypFMAY1n8laC6ao4rEGXpyik\n" +
"5OrFa3NLtY8duK2HgD5DsD96ysYToqaK+Ks14htCqgY2m9XJKA==");
        CertificateTlsPolicyCreator creator = new CertificateTlsPolicyCreator();
        CertificateTlsPolicy tlsPolicy = creator.createTlsPolicy(tlsPolicyDescriptor);
        assertNotNull(tlsPolicy);
    }
    
}
