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
public class PublicKeyTlsPolicyTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PublicKeyTlsPolicyTest.class);
    private static List<PublicKey> publicKeys = new ArrayList<>();
    
    @BeforeClass
    public static void createPublicKeys() throws NoSuchAlgorithmException {
        for(int i=0; i<3; i++) {
            KeyPair keypair = RsaUtil.generateRsaKeyPair(1028);
            publicKeys.add(keypair.getPublic());
        }
    }
    
    public static class PublicKeyTlsPolicy {
        public List<PublicKey> publicKeys;
    }
    
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class PublicKeyDigest {

        public PublicKeyDigest() {
        }

        public PublicKeyDigest(String algorithm, byte[] digest) {
            this.algorithm = algorithm;
            this.digest = digest;
        }
        
        public String algorithm;
        public byte[] digest;
    }
    public static class PublicKeyDigestTlsPolicy {
        public List<PublicKeyDigest> publicKeyDigests;
    }
    
    @Test
    public void testPublicKeyPem() throws CryptographyException {
        StringBuilder pem = new StringBuilder();
        for(PublicKey publicKey : publicKeys) {
            pem.append(RsaUtil.encodePemPublicKey(publicKey));
        }
        String text = pem.toString();
        log.debug("PEM:\n{}", text);
        List<PublicKey> decoded = RsaUtil.decodePemPublicKeys(text);
        int max = publicKeys.size();
        for(int i=0; i<max; i++) {
            assertArrayEquals(String.format("record %d", i),publicKeys.get(i).getEncoded(), decoded.get(i).getEncoded());
        }
    }
    
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class PublicKeyJsonMeta {
        public PublicKeyJsonMeta() { }
        public int version = 1;
    }
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class PublicKeyDigestJsonMeta {
        public PublicKeyDigestJsonMeta() { }
        public int version = 1;
        public String algorithm = null;
    }
    
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class PublicKeyJson {
        public PublicKeyJson() { }
        public PublicKeyJsonMeta meta = null;
        public List<String> publicKeys = new ArrayList<>();
    }
    
    @Test
    public void testPublicKeyJson() throws CryptographyException, JsonProcessingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(new PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy());
        PublicKeyJson json = new PublicKeyJson();
        json.meta = new PublicKeyJsonMeta();
        for(PublicKey publicKey : publicKeys) {
            json.publicKeys.add(Base64.encodeBase64String(publicKey.getEncoded()));
        }
        String text = mapper.writeValueAsString(json);
        log.debug("JSON:\n{}", text);
        PublicKeyJson decoded = mapper.readValue(text, PublicKeyJson.class);
        int max = publicKeys.size();
        for(int i=0; i<max; i++) {
            assertEquals(String.format("record %d", i),Base64.encodeBase64String(publicKeys.get(i).getEncoded()), decoded.publicKeys.get(i));
        }
    }
    
    public static class PublicKeyDigestCsv {
        public PublicKeyDigestCsv() { }

        public PublicKeyDigestCsv(String algorithm, String digest) {
            this.algorithm = algorithm;
            this.digest = digest;
        }
        
        public String algorithm;
        public String digest;
    }
    
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class PublicKeyDigestJson {
        public PublicKeyDigestJson() { }
        public PublicKeyDigestJsonMeta meta = null;
        public List<PublicKeyDigest> publicKeyDigests = new ArrayList<>();
    }
    
    @Test
    public void testPublicKeyDigestCsv() throws JsonProcessingException, IOException {
        CsvSchema schema = CsvSchema.builder()
                .addColumn("algorithm", CsvSchema.ColumnType.STRING)
                .addColumn("digest", CsvSchema.ColumnType.STRING)
                .build();
        CsvMapper mapper = new CsvMapper();
        ArrayList<PublicKeyDigestCsv> lines = new ArrayList<>();
        for(PublicKey publicKey : publicKeys) {
            lines.add(new PublicKeyDigestCsv("sha256", Sha256Digest.digestOf(publicKey.getEncoded()).toBase64()));
        }
        String text = "# inserting a comment here\n"+mapper.writer(schema).writeValueAsString(lines);
        log.debug("CSV:\n{}", text);
        mapper.enable(CsvParser.Feature.TRIM_SPACES);
//        mapper.enable(CsvParser.Feature.WRAP_AS_ARRAY); // not needed.
        ArrayList<PublicKeyDigestCsv> decoded = new ArrayList<>();
        MappingIterator<PublicKeyDigestCsv> it = mapper.reader(PublicKeyDigestCsv.class).with(schema).readValues(text);
        while(it.hasNext()) {
            PublicKeyDigestCsv csv = it.next();
            if( csv.algorithm != null && csv.algorithm.startsWith("#")) {
                continue; // ignore comment lines
            }
            decoded.add(csv);
        }
//        ArrayList<PublicKeyDigestCsv> decoded = mapper.reader(PublicKeyDigestCsv.class).with(schema).readValue(text); // does not work: cannot cast PublicKeyDigestCsv to ArrayList
        int max = publicKeys.size();
        for(int i=0; i<max; i++) {
            assertEquals(Sha256Digest.digestOf(publicKeys.get(i).getEncoded()).toBase64(), decoded.get(i).digest);
        }
    }
    
    @Test
    public void testPublicKeyDigestJson() throws CryptographyException, JsonProcessingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(new PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy());
        PublicKeyDigestJson json = new PublicKeyDigestJson();
        for(PublicKey publicKey : publicKeys) {
            json.publicKeyDigests.add(new PublicKeyDigest("sha256",Sha256Digest.digestOf(publicKey.getEncoded()).toByteArray()));
        }
        String text = mapper.writeValueAsString(json);
        log.debug("JSON:\n{}", text);
        PublicKeyDigestJson decoded = mapper.readValue(text, PublicKeyDigestJson.class);
        int max = publicKeys.size();
        for(int i=0; i<max; i++) {
            assertArrayEquals(String.format("record %d", i),Sha256Digest.digestOf(publicKeys.get(i).getEncoded()).toByteArray(), decoded.publicKeyDigests.get(i).digest);
        }
    }
    @Test
    public void testPublicKeyDigestJsonWithMeta() throws CryptographyException, JsonProcessingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(new PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy());
        PublicKeyDigestJson json = new PublicKeyDigestJson();
        json.meta = new PublicKeyDigestJsonMeta();
        json.meta.algorithm = "sha256"; // can set it here if they are all the same
        for(PublicKey publicKey : publicKeys) {
            json.publicKeyDigests.add(new PublicKeyDigest(null,Sha256Digest.digestOf(publicKey.getEncoded()).toByteArray()));
        }
        String text = mapper.writeValueAsString(json);
        log.debug("JSON:\n{}", text);
        PublicKeyDigestJson decoded = mapper.readValue(text, PublicKeyDigestJson.class);
        int max = publicKeys.size();
        for(int i=0; i<max; i++) {
            assertArrayEquals(String.format("record %d", i),Sha256Digest.digestOf(publicKeys.get(i).getEncoded()).toByteArray(), decoded.publicKeyDigests.get(i).digest);
        }
    }
    
}
