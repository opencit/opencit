/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.intel.mtwilson.atag.model.Configuration;
import java.io.IOException;
import java.util.Properties;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class ConfigurationTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConfigurationTest.class);

    /**
     * Output:
     * JSON: {"name":"test","content":{"color":"red","number":"5"}}
     * @throws IOException 
     */
    @Test
    public void testWriteJson() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Properties p = new Properties();
        p.setProperty("color", "red");
        p.setProperty("number", "5");
        Configuration c = new Configuration("test", p);
        log.debug("JSON: {}", mapper.writeValueAsString(c));
    }
    
    /**
     * Output:
     * XML: <Configuration xmlns=""><name>test</name><content><color>red</color><number>5</number></content></Configuration>
     * 
     * If you writeValueAsString(p) you would get: XML: <Properties xmlns=""><color>red</color><number>5</number></Properties>
     * 
     * @throws IOException 
     */
    @Test
    public void testWriteXml() throws IOException {
        XmlMapper mapper = new XmlMapper();
        Properties p = new Properties();
        p.setProperty("color", "red");
        p.setProperty("number", "5");
        Configuration c = new Configuration("test", p);
        log.debug("XML: {}", mapper.writeValueAsString(c));
    }
    
    /**
     * Output:
Properties XML: <?xml version="1.0" encoding="UTF-8" standalone="no"?>
<!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
<properties>
<comment>test</comment>
<entry key="color">red</entry>
<entry key="number">5</entry>
</properties>
     * @throws IOException 
     */
    @Test
    public void testWritePropertiesXml() throws IOException {
        Properties p = new Properties();
        p.setProperty("color", "red");
        p.setProperty("number", "5");
        Configuration c = new Configuration("test", p);
        log.debug("Properties XML: {}", c.getXmlContent());
    }
    
    
}
