/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.mtwilson.jackson.PublicKeyDeserializer;
import com.intel.mtwilson.jackson.PublicKeySerializer;
import java.security.KeyPair;
import java.security.PublicKey;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class JacksonTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JacksonTest.class);
    
    @JacksonXmlRootElement(localName="fruit")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Fruit {
        public String fruitName;
        public String fruitColor;
        @JsonSerialize(using=PublicKeySerializer.class)
        @JsonDeserialize(using=PublicKeyDeserializer.class)
        public PublicKey publicKey;
    }
    
    @Test
    public void testWriteDefault() throws Exception {
        KeyPair keypair = RsaUtil.generateRsaKeyPair(1024);
        ObjectMapper mapper = new ObjectMapper();
        Fruit fruit = new Fruit();
        fruit.fruitName = "apple";
        fruit.fruitColor = "red";
        fruit.publicKey = keypair.getPublic();
        String json = mapper.writeValueAsString(fruit);
        log.debug(json); // {"fruitName":"apple","fruitColor":"red","publicKey":"MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDBnOkYfoEG7pCGsJLxxX4WtDkB9Padlc+x+5vLA+mwcFkiDxGQSMr4zcj9XWhtMFNp7+nCg4dBOX0jczeYkRG1KxT9nRgasUvYdxF0xqyywsvViskWQUei75+rHyZ559aYWAGHEXoGK9acrpcTaLu1W46rISPe9ojBIWNj8KLqSwIDAQAB"}
//        assertEquals("{\"fruitName\":\"apple\",\"fruitColor\":\"red\"}", json);
        mapper.setPropertyNamingStrategy(new PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy());
        log.debug(mapper.writeValueAsString(fruit)); // {"fruit_name":"apple","fruit_color":"red"}
    }
    
    @Test
    public void testReadPublicKey() throws Exception {
        KeyPair keypair = RsaUtil.generateRsaKeyPair(1024);
        ObjectMapper mapper = new ObjectMapper();
        Fruit fruit = new Fruit();
        fruit.fruitName = "apple";
        fruit.fruitColor = "red";
        fruit.publicKey = keypair.getPublic();
        String json = mapper.writeValueAsString(fruit);
        log.debug(json); // {"fruitName":"apple","fruitColor":"red","publicKey":"MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDBnOkYfoEG7pCGsJLxxX4WtDkB9Padlc+x+5vLA+mwcFkiDxGQSMr4zcj9XWhtMFNp7+nCg4dBOX0jczeYkRG1KxT9nRgasUvYdxF0xqyywsvViskWQUei75+rHyZ559aYWAGHEXoGK9acrpcTaLu1W46rISPe9ojBIWNj8KLqSwIDAQAB"}
        Fruit copy = mapper.readValue(json, Fruit.class);
        log.debug("got fruit copy");
    }
    
    @Test
    public void testWriteUnderscores() throws Exception {
        KeyPair keypair = RsaUtil.generateRsaKeyPair(1024);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(new PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy());
        Fruit fruit = new Fruit();
        fruit.fruitName = "apple";
        fruit.fruitColor = "red";
        fruit.publicKey = keypair.getPublic();
        String json = mapper.writeValueAsString(fruit);
        log.debug(json); // {"fruit_name":"apple","fruit_color":"red"}
//        assertEquals("{\"fruit_name\":\"apple\",\"fruit_color\":\"red\"}", json);
    }
    
    
    @Test
    public void testWriteUnderscoresXml() throws Exception {
        KeyPair keypair = RsaUtil.generateRsaKeyPair(1024);
        XmlMapper mapper = new XmlMapper();
        mapper.setPropertyNamingStrategy(new PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy());
        Fruit fruit = new Fruit();
        fruit.fruitName = "apple";
        fruit.fruitColor = "red";
        fruit.publicKey = keypair.getPublic();
        String xml = mapper.writeValueAsString(fruit);
        log.debug(xml); // <fruit><fruit_name>apple</fruit_name><fruit_color>red</fruit_color></fruit>
//        assertEquals("<fruit><fruit_name>apple</fruit_name><fruit_color>red</fruit_color></fruit>", xml);
    }
    
    @Test
    public void testReadUnderscoresXml() throws Exception {
        String xml = "<fruit><fruit_name>apple</fruit_name><fruit_color>red</fruit_color></fruit>";
        XmlMapper mapper = new XmlMapper();
        mapper.setPropertyNamingStrategy(new PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy());
        Fruit fruit = mapper.readValue(xml, Fruit.class);
        log.debug("color: {}", fruit.fruitColor);
        log.debug("name: {}", fruit.fruitName);
        assertEquals("red", fruit.fruitColor);
        assertEquals("apple", fruit.fruitName);
    }
    
    /**
     * This test demonstrates tha tyou have to configure the proeprty naming strategy
     * before the first write or else it's ignored
     * @throws JsonProcessingException 
     */
    @Test
    public void testWriteUnderscoreIgnoredAfterFirstWrite() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Fruit fruit = new Fruit();
        fruit.fruitName = "apple";
        fruit.fruitColor = "red";
        String json = mapper.writeValueAsString(fruit);
        log.debug(json); // {"fruitName":"apple","fruitColor":"red"}
        assertEquals("{\"fruitName\":\"apple\",\"fruitColor\":\"red\"}", json);
        mapper.setPropertyNamingStrategy(new PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy());
        String json2 = mapper.writeValueAsString(fruit);
        log.debug(json2); // {"fruitName":"apple","fruitColor":"red"}, not the expected {"fruit_name":"apple","fruit_color":"red"}
        assertNotEquals("{\"fruit_name\":\"apple\",\"fruit_color\":\"red\"}", json2); 
        assertEquals("{\"fruitName\":\"apple\",\"fruitColor\":\"red\"}", json2);
    }
}
