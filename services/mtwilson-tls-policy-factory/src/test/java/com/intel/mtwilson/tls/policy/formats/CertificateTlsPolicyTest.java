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
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.crypto.Sha256Digest;
import com.intel.dcsg.cpg.x509.X509Util;
import java.io.IOException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.codec.binary.Base64;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class CertificateTlsPolicyTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CertificateTlsPolicyTest.class);
    private static List<X509Certificate> certificates = new ArrayList<>();
    
    @BeforeClass
    public static void createX509Certificates() throws NoSuchAlgorithmException, CryptographyException, IOException {
        for(int i=0; i<3; i++) {
            KeyPair keypair = RsaUtil.generateRsaKeyPair(1028);
            X509Certificate certificate = RsaUtil.generateX509Certificate(String.format("CN=%d",i), keypair, 10); // valid for 10 days
            certificates.add(certificate);
        }
    }
    
    public static class X509CertificateTlsPolicy {
        public List<X509Certificate> certificates;
    }
    
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class X509CertificateDigest {

        public X509CertificateDigest() {
        }

        public X509CertificateDigest(String algorithm, byte[] digest) {
            this.algorithm = algorithm;
            this.digest = digest;
        }
        
        public String algorithm;
        public byte[] digest;
    }
    public static class X509CertificateDigestTlsPolicy {
        public List<X509CertificateDigest> publicKeyDigests;
    }
    
    @Test
    public void testX509CertificatePem() throws CryptographyException, CertificateEncodingException, CertificateException {
        StringBuilder pem = new StringBuilder();
        for(X509Certificate certificate : certificates) {
            pem.append(X509Util.encodePemCertificate(certificate));
        }
        String text = pem.toString();
        log.debug("PEM:\n{}", text);
        List<X509Certificate> decoded = X509Util.decodePemCertificates(text);
        int max = certificates.size();
        for(int i=0; i<max; i++) {
            assertArrayEquals(String.format("record %d", i),certificates.get(i).getEncoded(), decoded.get(i).getEncoded());
        }
    }
    
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class X509CertificateJsonMeta {
        public X509CertificateJsonMeta() { }
        public int version = 1;
    }
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class X509CertificateDigestJsonMeta {
        public X509CertificateDigestJsonMeta() { }
        public int version = 1;
        public String algorithm = null;
    }
    
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class X509CertificateJson {
        public X509CertificateJson() { }
        public X509CertificateJsonMeta meta = null;
        public List<String> certificates = new ArrayList<>();
    }
    
    @Test
    public void testX509CertificateJson() throws CryptographyException, JsonProcessingException, IOException, CertificateEncodingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(new PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy());
        X509CertificateJson json = new X509CertificateJson();
        json.meta = new X509CertificateJsonMeta();
        for(X509Certificate certificate : certificates) {
            json.certificates.add(Base64.encodeBase64String(certificate.getEncoded()));
        }
        String text = mapper.writeValueAsString(json);
        log.debug("JSON:\n{}", text);
        X509CertificateJson decoded = mapper.readValue(text, X509CertificateJson.class);
        int max = certificates.size();
        for(int i=0; i<max; i++) {
            assertEquals(String.format("record %d", i),Base64.encodeBase64String(certificates.get(i).getEncoded()), decoded.certificates.get(i));
        }
    }
    
    public static class X509CertificateDigestCsv {
        public X509CertificateDigestCsv() { }

        public X509CertificateDigestCsv(String algorithm, String digest) {
            this.algorithm = algorithm;
            this.digest = digest;
        }
        
        public String algorithm;
        public String digest;
    }
    
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class X509CertificateDigestJson {
        public X509CertificateDigestJson() { }
        public X509CertificateDigestJsonMeta meta = null;
        public List<X509CertificateDigest> certificateDigests = new ArrayList<>();
    }
    
    @Test
    public void testX509CertificateDigestCsv() throws JsonProcessingException, IOException, CertificateEncodingException {
        CsvSchema schema = CsvSchema.builder()
                .addColumn("algorithm", CsvSchema.ColumnType.STRING)
                .addColumn("digest", CsvSchema.ColumnType.STRING)
                .build();
        CsvMapper mapper = new CsvMapper();
        ArrayList<X509CertificateDigestCsv> lines = new ArrayList<>();
        for(X509Certificate certificate : certificates) {
            lines.add(new X509CertificateDigestCsv("sha256", Sha256Digest.digestOf(certificate.getEncoded()).toBase64()));
        }
        String text = "# inserting a comment here\n"+mapper.writer(schema).writeValueAsString(lines);
        log.debug("CSV:\n{}", text);
        mapper.enable(CsvParser.Feature.TRIM_SPACES);
//        mapper.enable(CsvParser.Feature.WRAP_AS_ARRAY); // not needed.
        ArrayList<X509CertificateDigestCsv> decoded = new ArrayList<>();
        MappingIterator<X509CertificateDigestCsv> it = mapper.reader(X509CertificateDigestCsv.class).with(schema).readValues(text);
        while(it.hasNext()) {
            X509CertificateDigestCsv csv = it.next();
            if( csv.algorithm != null && csv.algorithm.startsWith("#")) {
                continue; // ignore comment lines
            }
            decoded.add(csv);
        }
//        ArrayList<X509CertificateDigestCsv> decoded = mapper.reader(X509CertificateDigestCsv.class).with(schema).readValue(text); // does not work: cannot cast X509CertificateDigestCsv to ArrayList
        int max = certificates.size();
        for(int i=0; i<max; i++) {
            assertEquals(Sha256Digest.digestOf(certificates.get(i).getEncoded()).toBase64(), decoded.get(i).digest);
        }
    }
    
    @Test
    public void testX509CertificateDigestJson() throws CryptographyException, JsonProcessingException, IOException, CertificateEncodingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(new PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy());
        X509CertificateDigestJson json = new X509CertificateDigestJson();
        for(X509Certificate certificate : certificates) {
            json.certificateDigests.add(new X509CertificateDigest("sha256",Sha256Digest.digestOf(certificate.getEncoded()).toByteArray()));
        }
        String text = mapper.writeValueAsString(json);
        log.debug("JSON:\n{}", text);
        X509CertificateDigestJson decoded = mapper.readValue(text, X509CertificateDigestJson.class);
        int max = certificates.size();
        for(int i=0; i<max; i++) {
            assertArrayEquals(String.format("record %d", i),Sha256Digest.digestOf(certificates.get(i).getEncoded()).toByteArray(), decoded.certificateDigests.get(i).digest);
        }
    }
    @Test
    public void testX509CertificateDigestJsonWithMeta() throws CryptographyException, JsonProcessingException, IOException, CertificateEncodingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(new PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy());
        X509CertificateDigestJson json = new X509CertificateDigestJson();
        json.meta = new X509CertificateDigestJsonMeta();
        json.meta.algorithm = "sha256"; // can set it here if they are all the same
        for(X509Certificate certificate : certificates) {
            json.certificateDigests.add(new X509CertificateDigest(null,Sha256Digest.digestOf(certificate.getEncoded()).toByteArray()));
        }
        String text = mapper.writeValueAsString(json);
        log.debug("JSON:\n{}", text);
        X509CertificateDigestJson decoded = mapper.readValue(text, X509CertificateDigestJson.class);
        int max = certificates.size();
        for(int i=0; i<max; i++) {
            assertArrayEquals(String.format("record %d", i),Sha256Digest.digestOf(certificates.get(i).getEncoded()).toByteArray(), decoded.certificateDigests.get(i).digest);
        }
    }
    
}
