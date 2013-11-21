/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.restlet;

import com.intel.mtwilson.atag.model.Tag;
import com.intel.dcsg.cpg.io.UUID;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
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
    public void writeTagNullValues() throws IOException {
        Tag tag = new Tag(1, new UUID(), "city", "1.1.1.1", null);
        log.debug("Tag: {}", mapper.writeValueAsString(tag));  
        // output: {"id":1,"uuid":"2f2cba28-1c24-43dc-8882-d3ab9a5a1033","name":"city","oid":"1.1.1.1","values":null}
    }

    @Test
    public void readTagNullValues() throws IOException {
        Tag tag = mapper.readValue("{\"id\":1,\"uuid\":\"2f2cba28-1c24-43dc-8882-d3ab9a5a1033\",\"name\":\"city\",\"oid\":\"1.1.1.1\",\"values\":null}", Tag.class);
        assertNull(tag.getValues());
        log.debug("Tag: {}", String.format("id: %s  uuid: %s  name: %s  oid: %s  values: %s", String.valueOf(tag.getId()), tag.getUuid(), tag.getName(), tag.getOid(), tag.getValues()));
        // output; Tag: id: 1  uuid: 2f2cba28-1c24-43dc-8882-d3ab9a5a1033  name: city  oid: 1.1.1.1  values: null
    }
    
    @Test
    public void writeTagEmptyValues() throws IOException {
        Tag tag = new Tag(1, new UUID(), "city", "1.1.1.1", new ArrayList<String>());
        log.debug("Tag: {}", mapper.writeValueAsString(tag));  
        // output: {"id":1,"uuid":"2f2cba28-1c24-43dc-8882-d3ab9a5a1033","name":"city","oid":"1.1.1.1","values":[]}
    }

    @Test
    public void readTagEmptyValues() throws IOException {
        Tag tag = mapper.readValue("{\"id\":1,\"uuid\":\"2f2cba28-1c24-43dc-8882-d3ab9a5a1033\",\"name\":\"city\",\"oid\":\"1.1.1.1\",\"values\":[]}", Tag.class);
        log.debug("Tag: {}", String.format("id: %s  uuid: %s  name: %s  oid: %s  values: %s", String.valueOf(tag.getId()), tag.getUuid(), tag.getName(), tag.getOid(), StringUtils.join(tag.getValues(), ", ")));
        // output; Tag: id: 1  uuid: 2f2cba28-1c24-43dc-8882-d3ab9a5a1033  name: city  oid: 1.1.1.1  values: 
    }
    
    @Test
    public void writeTagWithValues() throws IOException {
        ArrayList<String> values = new ArrayList<String>();
        values.add("san jose");
        values.add("folsom");
        Tag tag = new Tag(1, new UUID(), "city", "1.1.1.1", values);
        log.debug("Tag: {}", mapper.writeValueAsString(tag));  
        // output: {"id":1,"uuid":"2f2cba28-1c24-43dc-8882-d3ab9a5a1033","name":"city","oid":"1.1.1.1","values":["san jose","folsom"]}
    }
    
    @Test
    public void readTagWithValues() throws IOException {
        Tag tag = mapper.readValue("{\"id\":1,\"uuid\":\"2f2cba28-1c24-43dc-8882-d3ab9a5a1033\",\"name\":\"city\",\"oid\":\"1.1.1.1\",\"values\":[\"san jose\",\"folsom\"]}", Tag.class);
        log.debug("Tag: {}", String.format("id: %s  uuid: %s  name: %s  oid: %s  values: %s", String.valueOf(tag.getId()), tag.getUuid(), tag.getName(), tag.getOid(), StringUtils.join(tag.getValues(), ", ")));
        // output: Tag: id: 1  uuid: 2f2cba28-1c24-43dc-8882-d3ab9a5a1033  name: city  oid: 1.1.1.1  values: san jose, folsom
    }
}
