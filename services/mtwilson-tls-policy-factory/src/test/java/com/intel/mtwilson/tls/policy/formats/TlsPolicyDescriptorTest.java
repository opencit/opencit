/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls.policy.formats;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.crypto.Sha256Digest;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.tls.policy.TlsPolicyDescriptor;
import com.intel.mtwilson.jackson.LowercaseWithHyphensStrategy;
import java.io.IOException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import org.apache.commons.codec.binary.Base64;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class TlsPolicyDescriptorTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TlsPolicyDescriptorTest.class);
    private static List<X509Certificate> certificates = new ArrayList<>();
    private static ObjectMapper json;
    private static XmlMapper xml;
    
    @BeforeClass
    public static void createX509Certificates() throws NoSuchAlgorithmException, CryptographyException, IOException {
        for(int i=0; i<3; i++) {
            KeyPair keypair = RsaUtil.generateRsaKeyPair(1028);
            X509Certificate certificate = RsaUtil.generateX509Certificate(String.format("CN=%d",i), keypair, 10); // valid for 10 days
            certificates.add(certificate);
        }
    }
    
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
    
    
    public static class TypeA { public String color = "red"; public String fruit = "apple"; }
    public static class TypeB { public String fruit; }
    @Test
    public void testIgnoreUnknownProperties() throws JsonProcessingException, IOException {
        String text = json.writeValueAsString(new TypeA());
        TypeB decoded = json.readValue(text, TypeB.class);
        log.debug("decoded: {}", json.writeValueAsString(decoded));
    }
    
    
    @Test
    public void testX509CertificateDescriptor() throws CryptographyException, CertificateEncodingException, CertificateException, JsonProcessingException, IOException {
        TlsPolicyDescriptor descriptor = new TlsPolicyDescriptor();
        descriptor.setData(new HashSet<String>());
        for(X509Certificate certificate : certificates) {
            descriptor.getData().add(Base64.encodeBase64String(certificate.getEncoded()));
        }
        String text = json.writeValueAsString(descriptor);
        log.debug("JSON:\n{}", text);
        log.debug("XML:\n{}", xml.writeValueAsString(descriptor));
        TlsPolicyDescriptor decoded = json.readValue(text, TlsPolicyDescriptor.class);
        int max = certificates.size();
        for(int i=0; i<max; i++) {
            assertTrue(String.format("record %d", i),decoded.getData().contains(Base64.encodeBase64String(certificates.get(i).getEncoded())));
        }
    }

    @Test
    public void testPublicKeyDescriptor() throws CryptographyException, CertificateEncodingException, CertificateException, JsonProcessingException, IOException {
        TlsPolicyDescriptor descriptor = new TlsPolicyDescriptor();
        descriptor.setData(new HashSet<String>());
        for(X509Certificate certificate : certificates) {
            descriptor.getData().add(Base64.encodeBase64String(certificate.getPublicKey().getEncoded()));
        }
        String text = json.writeValueAsString(descriptor);
        log.debug("JSON:\n{}", text);
        log.debug("XML:\n{}", xml.writeValueAsString(descriptor));
        TlsPolicyDescriptor decoded = json.readValue(text, TlsPolicyDescriptor.class);
        int max = certificates.size();
        for(int i=0; i<max; i++) {
            assertTrue(String.format("record %d", i),decoded.getData().contains(Base64.encodeBase64String(certificates.get(i).getPublicKey().getEncoded())));
        }
    }
    
    @Test
    public void testX509CertificateDigestDescriptor() throws CryptographyException, CertificateEncodingException, CertificateException, JsonProcessingException, IOException {
        TlsPolicyDescriptor descriptor = new TlsPolicyDescriptor();
        descriptor.setMeta(new HashMap<String,String>());
        descriptor.getMeta().put("digestAlgorithm","SHA256");
        descriptor.setData(new HashSet<String>());
        for(X509Certificate certificate : certificates) {
            descriptor.getData().add(Base64.encodeBase64String(Sha256Digest.digestOf(certificate.getEncoded()).toByteArray()));
        }
        String text = json.writeValueAsString(descriptor);
        log.debug("JSON:\n{}", text);
        log.debug("XML:\n{}", xml.writeValueAsString(descriptor));
        TlsPolicyDescriptor decoded = json.readValue(text, TlsPolicyDescriptor.class);
        int max = certificates.size();
        for(int i=0; i<max; i++) {
            assertTrue(String.format("record %d", i),decoded.getData().contains(Base64.encodeBase64String(Sha256Digest.digestOf(certificates.get(i).getEncoded()).toByteArray())));
        }
    }

    @Test
    public void testPublicKeyDigestDescriptor() throws CryptographyException, CertificateEncodingException, CertificateException, JsonProcessingException, IOException {
        TlsPolicyDescriptor descriptor = new TlsPolicyDescriptor();
        descriptor.setMeta(new HashMap<String,String>());
        descriptor.getMeta().put("digestAlgorithm","SHA256");
        descriptor.setData(new HashSet<String>());
        for(X509Certificate certificate : certificates) {
            descriptor.getData().add(Base64.encodeBase64String(Sha256Digest.digestOf(certificate.getPublicKey().getEncoded()).toByteArray()));
        }
        String text = json.writeValueAsString(descriptor);
        log.debug("JSON:\n{}", text);
         log.debug("XML:\n{}", xml.writeValueAsString(descriptor));
       TlsPolicyDescriptor decoded = json.readValue(text, TlsPolicyDescriptor.class);
        int max = certificates.size();
        for(int i=0; i<max; i++) {
            assertTrue(String.format("record %d", i),decoded.getData().contains(Base64.encodeBase64String(Sha256Digest.digestOf(certificates.get(i).getPublicKey().getEncoded()).toByteArray())));
        }
    }
    
}
