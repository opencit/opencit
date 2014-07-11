/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls.policy;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.dcsg.cpg.crypto.Sha256Digest;
import com.intel.dcsg.cpg.validation.ValidationUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 *
 * @author jbuhacoff
 */
public class TlsPolicyDescriptorTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TlsPolicyDescriptorTest.class);
    private static ObjectMapper json;
    private static XmlMapper xml;

    @BeforeClass
    public static void createMapper() {
        json = new ObjectMapper();
        json.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        json.setPropertyNamingStrategy(new PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy());
        json.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        json.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        xml = new XmlMapper(/*jsonFactory*/);
//        xml.setPropertyNamingStrategy(new LowercaseWithHyphensStrategy());
        xml.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        xml.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        xml.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    
    @Test
    public void testEmptyTlsPolicyDescriptor() throws JsonProcessingException, IOException {
       TlsPolicyDescriptor descriptor = new TlsPolicyDescriptor();
       log.debug("empty descriptor json: {}", json.writeValueAsString(descriptor));
       log.debug("empty descriptor xml: {}", xml.writeValueAsString(descriptor));       
       TlsPolicyDescriptor copy = json.readValue(json.writeValueAsString(descriptor), TlsPolicyDescriptor.class);
       log.debug("copy empty: {}", json.writeValueAsString(copy));
    }

    @Test
    public void testInsecureTlsPolicyDescriptor() throws JsonProcessingException, IOException {
       TlsPolicyDescriptor descriptor = new TlsPolicyDescriptor();
       descriptor.setPolicyType("INSECURE");
       log.debug("insecure descriptor json: {}", json.writeValueAsString(descriptor));
       log.debug("insecure descriptor xml: {}", xml.writeValueAsString(descriptor));
       TlsPolicyDescriptor copy = json.readValue(json.writeValueAsString(descriptor), TlsPolicyDescriptor.class);
       log.debug("copy public-key-digest: {}", json.writeValueAsString(copy));
    }

    @Test
    public void testPublicKeyDigestTlsPolicyDescriptor() throws JsonProcessingException, IOException {
       TlsPolicyDescriptor descriptor = new TlsPolicyDescriptor();
       descriptor.setPolicyType( "public-key-digest");
       descriptor.setMeta(new HashMap<String,String>());
       descriptor.getMeta().put("digestAlgorithm", "SHA-256"); // MD5, SHA-1, SHA-256, SHA-384, SHA-512
       descriptor.getMeta().put("digestEncoding", "base64"); // base64 or hex
       descriptor.setData(new ArrayList<String>());
       descriptor.getData().add(Sha256Digest.digestOf(RandomUtil.randomByteArray(50)).toBase64());
       log.debug("public-key-digest descriptor json: {}", json.writeValueAsString(descriptor));
       log.debug("public-key-digest descriptor xml: {}", xml.writeValueAsString(descriptor));
       TlsPolicyDescriptor copy = json.readValue(json.writeValueAsString(descriptor), TlsPolicyDescriptor.class);
       log.debug("copy public-key-digest: {}", json.writeValueAsString(copy));
    }

    @Test
    public void testTrustFirstCertificateTlsPolicyDescriptor() throws JsonProcessingException {
       TlsPolicyDescriptor descriptor = new TlsPolicyDescriptor();
       descriptor.setPolicyType("TRUST_FIRST_CERTIFICATE");
       descriptor.setProtection(new TlsProtection());
       descriptor.getProtection().authentication = true;
       descriptor.getProtection().encryption = true;
       descriptor.setCiphers("RSA,AES,SHA,!EC,!MD5"); // means any rsa, aes, or sha algorithm, but no elliptic curve algorithms and no md5
       descriptor.setProtocols("!SSL,-TLS,+TLS1.2"); // means no ssl version at all, and only tls 1.2 or greater
       log.debug("trust-first-certificate descriptor json: {}", json.writeValueAsString(descriptor));
       log.debug("trust-first-certificate descriptor xml: {}", xml.writeValueAsString(descriptor));
    }

    
    @Test(expected=IllegalArgumentException.class)
    public void testInvalidTrustFirstCertificateTlsPolicyDescriptor() throws JsonProcessingException {
       TlsPolicyDescriptor descriptor = new TlsPolicyDescriptor();
       descriptor.setPolicyType("TRUST_FIRST_CERTIFICATE*"); // illegal character *  
       descriptor.setProtection(new TlsProtection());
       descriptor.getProtection().authentication = true;
       descriptor.getProtection().encryption = true;
       descriptor.setCiphers("RSA,AES,SHA,!EC,!MD5");
       descriptor.setProtocols("!SSL,-TLS,+TLS1.2");
       ValidationUtil.validate(descriptor);
    }
    
}
