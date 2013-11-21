/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.jackson;

import test.restlet.*;
import com.intel.dcsg.cpg.io.UUID;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
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
public class XmlTest {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final XmlMapper mapper = new XmlMapper(); 

    @Test
    public void writeFruitResponse() throws IOException {
        FruitResponse r = new FruitResponse();
        r.setId(1);
        r.setName("apple");
        r.setColor("red");
        r.getLinks().put("author", new URL("http://localhost/authors/123"));
        log.debug("Fruit: {}", mapper.writeValueAsString(r));  
        // output: <FruitResponse><id>1</id><links><author>http://localhost/authors/123</author></links><color>red</color><name>apple</name></FruitResponse>
    }

    @Test
    public void writeFruitResponseEmptyLinks() throws IOException {
        FruitResponse r = new FruitResponse();
        r.setId(1);
        r.setName("apple");
        r.setColor("red");
        log.debug("Fruit: {}", mapper.writeValueAsString(r));  
        // output:  <FruitResponse><id>1</id><links/><color>red</color><name>apple</name></FruitResponse>
    }

    @Test
    public void writeFruitResponseNullLinks() throws IOException {
        FruitResponse r = new FruitResponse();
        r.setId(1);
        r.setName("apple");
        r.setColor("red");
        r.setLinks(null);
        log.debug("Fruit: {}", mapper.writeValueAsString(r));  
        // output: <FruitResponse><id>1</id><links/><color>red</color><name>apple</name></FruitResponse>
    }

    @Test
    public void writeFruitResponseNullColor() throws IOException {
        FruitResponse r = new FruitResponse();
        r.setId(1);
        r.setName("apple");
        r.setColor(null);
        r.setLinks(null);
        log.debug("Fruit: {}", mapper.writeValueAsString(r));  
        // output: <FruitResponse><id>1</id><links/><color/><name>apple</name></FruitResponse>

    }

    @Test
    public void writeFruitResponseOmitNullsWithNullColor() throws IOException {
        FruitResponseOmitNulls r = new FruitResponseOmitNulls();
        r.setId(1);
        r.setName("apple");
        r.setColor(null);
        r.setLinks(null);
        log.debug("Fruit: {}", mapper.writeValueAsString(r));  
        // output:  <FruitResponseOmitNulls><id>1</id><name>apple</name></FruitResponseOmitNulls>

    }
    
}
