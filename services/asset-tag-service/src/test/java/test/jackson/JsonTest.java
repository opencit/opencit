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
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class JsonTest {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final ObjectMapper mapper = new ObjectMapper(); // or from  org.fasterxml.jackson.databind.ObjectMapper

    @Test
    public void writeFruitResponse() throws IOException {
        FruitResponse r = new FruitResponse();
        r.setId(1); // will not see in output because the base class has @JsonIgnore on the id field
        r.setName("apple");
        r.setColor("red");
        r.getLinks().put("author", new URL("http://localhost/authors/123"));
        log.debug("Fruit: {}", mapper.writeValueAsString(r));  
        // output: {"links":{"author":"http://localhost/authors/123"},"color":"red","name":"apple"}
    }

    @Test
    public void writeFruitResponseEmptyLinks() throws IOException {
        FruitResponse r = new FruitResponse();
        r.setId(1);
        r.setName("apple");
        r.setColor("red");
        log.debug("Fruit: {}", mapper.writeValueAsString(r));  
        // output: {"links":{},"color":"red","name":"apple"}
    }

    @Test
    public void writeFruitResponseNullLinks() throws IOException {
        FruitResponse r = new FruitResponse();
        r.setId(1);
        r.setName("apple");
        r.setColor("red");
        r.setLinks(null);
        log.debug("Fruit: {}", mapper.writeValueAsString(r));  
        // output: {"links":null,"color":"red","name":"apple"}
    }

    @Test
    public void writeFruitResponseNullColor() throws IOException {
        FruitResponse r = new FruitResponse();
        r.setId(1);
        r.setName("apple");
        r.setColor(null);
        r.setLinks(null);
        log.debug("Fruit: {}", mapper.writeValueAsString(r));  
        // output: import com.fasterxml.jackson.annotation.JsonInclude;

    }

    @Test
    public void writeFruitResponseOmitNullsWithNullColor() throws IOException {
        FruitResponseOmitNulls r = new FruitResponseOmitNulls();
        r.setId(1);
        r.setName("apple");
        r.setColor(null);
        r.setLinks(null);
        log.debug("Fruit: {}", mapper.writeValueAsString(r));  
        // output:  {"name":"apple"}

    }
    
}
