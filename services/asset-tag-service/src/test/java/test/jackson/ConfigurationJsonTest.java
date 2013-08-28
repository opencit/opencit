/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import test.restlet.*;
import com.intel.dcsg.cpg.io.UUID;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.intel.dcsg.cpg.io.ByteArray;
import com.intel.mtwilson.atag.model.Configuration;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Properties;
import java.util.Stack;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class ConfigurationJsonTest {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final ObjectMapper mapper = new ObjectMapper(); // or from  org.fasterxml.jackson.databind.ObjectMapper

    /**
     * Example output when JSON parser is not enabled:
     * 
Configuration: {"uuid":"e0089aaa-496b-4ac2-a778-5945eb765a4d","links":{"author":"http://localhost/authors/123"},"name":"main","contentType":"JSON","content":"{\"color\":\"red\",\"enabled\":true,\"numbers\":[1,2,3]}"}
     * 
     * Example output with JSON parser:
     * 
Configuration: {"uuid":"702045f2-86d6-463a-902a-e36f4d80d723","links":{"author":"http://localhost/authors/123"},"name":"main","contentType":"JSON","content":{"color":"red","enabled":true,"numbers":[1,2,3]}}
     * 
     * @throws IOException 
     */
    @Test
    public void readJsonConfiguration() throws IOException {
        Configuration doc = new Configuration();
        doc.setId(1);
        doc.setUuid(new UUID());
        doc.setName("main");
        doc.setContentType(Configuration.ContentType.JSON);
        doc.setContent("{\"color\":\"red\",\"enabled\":true,\"numbers\":[1,2,3]}");
        doc.getLinks().put("author", new URL("http://localhost/authors/123"));
        log.debug("Configuration: {}", mapper.writeValueAsString(doc));  
    }

    @Test
    public void writeJsonConfiguration() throws IOException {
        String input = "{\"uuid\":\"9e552d4f-097d-473b-a53e-0fe17ffec8a8\",\"links\":{},\"name\":\"main\",\"contentType\":\"JSON\",\"content\":{\"allowTagsInCertificateRequests\":\"true\",\"allowAutomaticTagSelection\":false,\"automaticTagSelectionName\":\"default\",\"approveAllCertificateRequests\":false}}";
        Configuration configuration = mapper.readValue(input, Configuration.class);
        log.debug("Configuration: {}", mapper.writeValueAsString(configuration));  
    }
    
    /**
     * Output example when XML parser is not enabled:
     * 
Configuration: {"uuid":"844bce8f-9711-48b9-943b-68f9a80492bc","links":{"author":"http://localhost/authors/123"},"name":"main","contentType":"XML","content":"<configuration><color>red</color><enabled>true</enabled><numbers><number>1</number><number>2</number><number>3</number></numbers></configuration>"}
     * 
     * WRONG:   "numbers":{"number":"3"} is incorrect:
     * <configuration><color>red</color><enabled>true</enabled><numbers><number>1</number><number>2</number><number>3</number></numbers></configuration>
     * Configuration: {"uuid":"dead030e-6fcf-49a1-a7a4-6c781759c63e","links":{"author":"http://localhost/authors/123"},"name":"main","contentType":"XML","content":{"color":"red","enabled":"true","numbers":{"number":"3"}}}
     * 
     * WRONG:   "numbers":"3"  is incorrect
     * <configuration><color>red</color><enabled>true</enabled><numbers>1</numbers><numbers>2</numbers><numbers>3</numbers></configuration>
     * Configuration: {"uuid":"8388a415-c61d-4b4e-9878-41d844d3d8f5","links":{"author":"http://localhost/authors/123"},"name":"main","contentType":"XML","content":{"color":"red","enabled":"true","numbers":"3"}}
     * 
     * 
     * WRONG:  "numbers":{"numbers":"3"}  is incorrect  even though this is how XmlMapper serializes the numbers field by default:
     * <configuration><color>red</color><enabled>true</enabled><numbers><numbers>1</numbers><numbers>2</numbers><numbers>3</numbers></numbers></configuration>
     * Configuration: {"uuid":"9ee88673-cc87-4026-8a4f-f30fced07296","links":{"author":"http://localhost/authors/123"},"name":"main","contentType":"XML","content":{"color":"red","enabled":"true","numbers":{"numbers":"3"}}}

     * 
     * @throws IOException 
     */
    @Test
    public void readXmlConfiguration() throws IOException {
        Configuration doc = new Configuration();
        doc.setId(1);
        doc.setUuid(new UUID());
        doc.setName("main");
        doc.setContentType(Configuration.ContentType.XML);
        doc.setContent("<configuration><color>red</color><enabled>true</enabled><numbers><numbers>1</numbers><numbers>2</numbers><numbers>3</numbers></numbers></configuration>"); 
        doc.getLinks().put("author", new URL("http://localhost/authors/123"));
        log.debug("Configuration: {}", mapper.writeValueAsString(doc));  
    }
   
    /**
     * Example output:
     * 
Configuration: {"uuid":"2e1ced50-e7fa-4f63-a407-7e2591ad3342","links":{"author":"http://localhost/authors/123"},"name":"main","contentType":"PROPERTIES","content":{"numbers":"1,2,3","color":"red","enabled":"true"}}
     * 
     * 
     * @throws IOException 
     */
    @Test
    public void readPropertiesConfiguration() throws IOException {
        Configuration doc = new Configuration();
        doc.setId(1);
        doc.setUuid(new UUID());
        doc.setName("main");
        doc.setContentType(Configuration.ContentType.PROPERTIES);
        doc.setContent("#Tue Aug 27 22:47:06 PDT 2013\n" +"numbers=1,2,3\n" +"color=red\n" +"enabled=true\n"); 
        doc.getLinks().put("author", new URL("http://localhost/authors/123"));
        log.debug("Configuration: {}", mapper.writeValueAsString(doc));  
    }

    /**
     * 
     * Example output for properties format:
#Tue Aug 27 22:47:06 PDT 2013
numbers=1,2,3
color=red
enabled=true
     * 
     * Example output for xml format:
     * 
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
<properties>
<entry key="numbers">1,2,3</entry>
<entry key="color">red</entry>
<entry key="enabled">true</entry>
</properties>
     * 
     */
    @Test
    public void writePropertiesConfiguration() throws IOException {
        Properties p = new Properties();
        p.setProperty("color", "red");
        p.setProperty("enabled","true");
        p.setProperty("numbers","1,2,3");
        StringWriter writer = new StringWriter();
        p.store(writer, null);
        log.debug("Configuration: {}", writer.toString());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        p.storeToXML(out, null);
        log.debug("Configuration xml: {}", out.toString());
    }
    
    public static class SampleRootConfiguration {
        public String color = "red";
        public boolean enabled = true;
        public ArrayList<Integer> numbers = new ArrayList<Integer>();
        public SampleRootConfiguration() {
            numbers.add(1);
            numbers.add(2);
            numbers.add(3);
        }
    }

    /**
     * Output:
     * sample config: <SampleRootConfiguration><color>red</color><enabled>true</enabled><numbers><numbers>1</numbers><numbers>2</numbers><numbers>3</numbers></numbers></SampleRootConfiguration>
     * 
     * @throws IOException E
     */
    @Test
    public void writeXmlConfiguration() throws IOException {
        SampleRootConfiguration config = new SampleRootConfiguration();
        XmlMapper xml = new XmlMapper();
        log.debug("sample config: {}", xml.writeValueAsString(config));
    }
}
