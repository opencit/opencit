/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.crypto;

import com.intel.mtwilson.crypto.CryptographyException;
import com.intel.mtwilson.crypto.RsaUtil;
import com.intel.mtwilson.x500.DN;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class SubjectAlternativeNameTest {
    @Test
    public void testExtractSubjectAlternativeName() throws NoSuchAlgorithmException, GeneralSecurityException, IOException, CryptographyException {
        KeyPair keypair = RsaUtil.generateRsaKeyPair(1024);
        X509Certificate rsa = RsaUtil.generateX509Certificate("Test Cert", "ip:1.2.3.4", keypair, 30);
        String alternativeName = RsaUtil.ipAddressAlternativeName(rsa);
        System.out.println(alternativeName);
    }
    
    @Test
    public void testExtractSubjectAlternativeNameNull() throws NoSuchAlgorithmException, GeneralSecurityException, IOException, CryptographyException {
        KeyPair keypair = RsaUtil.generateRsaKeyPair(1024);
        X509Certificate rsa = RsaUtil.generateX509Certificate("Test Cert", keypair, 30);
        String alternativeName = RsaUtil.ipAddressAlternativeName(rsa);
        System.out.println(alternativeName); // should be null
    }
    
    @Test
    public void testCommonNameFromLdapName() {
        DN dn = new DN("CN=abc,OU=def,O=ghi,C=US");
        System.out.println(dn.get("CN")); 
        assertEquals("abc", dn.get("CN"));
        DN dn2 = new DN("CN=ABC,CN=abc,OU=def,O=ghi,C=US");
        System.out.println(dn2.get("CN"));
        assertEquals("ABC", dn2.get("CN")); // retrieves only the first one
    }
}
