/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package test.jackson;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class JacksonAnyGetterTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JacksonAnyGetterTest.class);
    private static ObjectMapper mapper = new ObjectMapper();
    
    public static class Rectangle {
        private HashMap<String,Object> extensions = new HashMap<>();
        @JsonProperty("length")
        public Integer length;
        public Integer width;

        @JsonAnyGetter
        public HashMap<String, Object> getExtensions() {
            return extensions;
        }
        @JsonAnySetter
        public void setExtension(String key, String value) {
            extensions.put(key, value);
        }
        
    }
    
    @Test
    public void testRectangleWithExtensions() throws JsonProcessingException {
        Rectangle shape = new Rectangle();
        shape.length = 3;
        shape.width = 2;
        shape.setExtension("color", "blue");
        shape.setExtension("length", "5");
        log.debug("shape: {}", mapper.writeValueAsString(shape)); // shape: {"width":2,"length":3,"color":"blue","length":"5"}
    }
    
}
