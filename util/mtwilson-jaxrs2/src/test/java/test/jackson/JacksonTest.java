/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.mtwilson.jaxrs2.Document;
import java.util.Date;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class JacksonTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JacksonTest.class);
    
    @JacksonXmlRootElement(localName="fruit")
    public static class Fruit extends Document {
        public String fruitName;
        public String fruitColor;
        public Date modifiedOn;
    }
    
    @Test
    public void testWriteDefault() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Fruit fruit = new Fruit();
        fruit.fruitName = "apple";
        fruit.fruitColor = "red";
        String json = mapper.writeValueAsString(fruit);
        log.debug(json); // {"fruitName":"apple","fruitColor":"red"}
        assertEquals("{\"fruitName\":\"apple\",\"fruitColor\":\"red\"}", json);
        mapper.setPropertyNamingStrategy(new PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy());
        log.debug(mapper.writeValueAsString(fruit)); // {"fruit_name":"apple","fruit_color":"red"}
    }
    
    @Test
    public void testWriteUnderscores() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(new PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy());
        Fruit fruit = new Fruit();
        fruit.fruitName = "apple";
        fruit.fruitColor = "red";
        String json = mapper.writeValueAsString(fruit);
        log.debug(json); // {"fruit_name":"apple","fruit_color":"red"}
        assertEquals("{\"fruit_name\":\"apple\",\"fruit_color\":\"red\"}", json);
    }
    
    
    @Test
    public void testWriteUnderscoresXml() throws JsonProcessingException {
        XmlMapper mapper = new XmlMapper();
        mapper.setPropertyNamingStrategy(new PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy());
        Fruit fruit = new Fruit();
        fruit.fruitName = "apple";
        fruit.fruitColor = "red";
        String xml = mapper.writeValueAsString(fruit);
        log.debug(xml); // <fruit><fruit_name>apple</fruit_name><fruit_color>red</fruit_color></fruit>
        assertEquals("<fruit><fruit_name>apple</fruit_name><fruit_color>red</fruit_color></fruit>", xml);
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
    
    @Test
    public void testWriteUnderscoresWithEtag() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(new PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy());
        Fruit fruit = new Fruit();
        fruit.fruitName = "apple";
        fruit.fruitColor = "red";
        fruit.setModifiedOn(new Date());
        String json = mapper.writeValueAsString(fruit);
        log.debug(json); // {"modified_on":1397555037640,"fruit_name":"apple","fruit_color":"red","etag":"9fda622d89075e962a4be5086e3b8bcd7ca7d227"}
//        assertEquals("{\"fruit_name\":\"apple\",\"fruit_color\":\"red\"}", json);
    }
    
}
