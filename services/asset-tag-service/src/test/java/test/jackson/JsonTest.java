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
import com.intel.dcsg.cpg.io.ByteArray;
import java.util.Iterator;
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
    
    private static class StackItem {
        public String key;
        public JsonNode value;
        public StackItem(String key, JsonNode value) {
            this.key = key;
            this.value = value;
        }
    }
    
    @Test
    public void testPathJsonObject() throws IOException {
        String json = "{\"foo\":\"bar\",\"quux\":\"baz\",\"nested\":{\"shape\":\"square\",\"color\":\"red\"},\"list\":[\"a\",\"b\",\"c\"],\"enabled\":true,\"numbers\":[1,2,3,4]}";
        JsonNode root = mapper.readTree(json);
//        assertEquals("red", root.path("nested.color").asText()); // fails:   expected:<[red]> but was:<[]>
        assertEquals("red", root.get("nested").get("color").asText());
//        assertEquals("a", root.path("list[0]").asText()); // fails: expected:<[a]> but was:<[]>
        assertEquals("a", root.get("list").get(0).asText());
        assertEquals(true, root.path("enabled").booleanValue());
//        assertEquals(2, root.path("numbers[1]").intValue()); // would fail
        assertEquals(2, root.get("numbers").get(1).intValue());
    }
    
    @Test
    public void testWalkJsonObject() throws IOException {
        String json = "{\"foo\":\"bar\",\"quux\":\"baz\",\"nested\":{\"shape\":\"square\",\"color\":\"red\"},\"list\":[\"a\",\"b\",\"c\"],\"enabled\":true,\"numbers\":[1,2,3,4]}";
        JsonNode root = mapper.readTree(json);
        Stack<StackItem> stack = new Stack<StackItem>();
        stack.push(new StackItem("",root));
        while(!stack.empty()) {
            StackItem item = stack.pop();
            if( item.value.isObject() ) {
//                log.debug("node '{}' is object", item.key);
                Iterator<String> it = item.value.fieldNames();
                while(it.hasNext()) {
                    String fieldName = it.next();
                    JsonNode field = item.value.get(fieldName);
                    stack.push(new StackItem(StringUtils.join(new String[] { item.key, fieldName }, '.'), field));
                }
            }
            else if( item.value.isArray() ) {
//                log.debug("node '{}' is array", item.key);
                Iterator<JsonNode> it = item.value.elements();
                int i = 0;
                while(it.hasNext()) {
                    JsonNode element = it.next();
                    stack.push(new StackItem(String.format("%s[%d]", item.key, i), element));
                    i++;
                }
            }
            else if( item.value.isNull() ) {
//                log.debug("node '{}' is null", item.key);
                log.info(String.format("%s = null", item.key));
            }
            else if( item.value.isTextual() ) {
//                log.debug("node '{}' is textual", item.key);
                log.info(String.format("%s = %s", item.key, item.value.asText()));
            }
            else if( item.value.isBoolean()) {
                log.info(String.format("%s = %s", item.key, Boolean.valueOf(item.value.booleanValue()).toString()));
            }
            else if( item.value.isBinary()) {
                log.info(String.format("%s = %s", item.key, new ByteArray(item.value.binaryValue()).toHexString()));
            }
            else if( item.value.isNumber() ) {
//                log.debug("node '{}' is textual", item.key);
                if( item.value.isInt() ) {
                    log.info(String.format("%s = %d", item.key, item.value.intValue()));                    
                }
                else if( item.value.isLong() ) {
                    log.info(String.format("%s = %d", item.key, item.value.longValue()));                    
                }
                else if( item.value.isBigDecimal() ) {
                    log.info(String.format("%s = %s", item.key, item.value.decimalValue().toString()));
                }
                else if( item.value.isBigInteger() ) {
                    log.info(String.format("%s = %s", item.key, item.value.bigIntegerValue().toString()));
                }
                else if( item.value.isDouble()) {
                    log.info(String.format("%s = %f", item.key, item.value.doubleValue()));
                }
                else if( item.value.isFloat()) {
                    log.info(String.format("%s = %f", item.key, item.value.floatValue()));
                }
                else if( item.value.isShort()) {
                    log.info(String.format("%s = %d", item.key, item.value.shortValue()));
                }
                else {
                    log.debug(String.format("%s is %s", item.key, item.value.getNodeType().name()));                    
                }
            }
            else {
                log.debug("node '{}' is other", item.key);
                log.info(String.format("%s is %s", item.key, item.value.getNodeType().name()));
            }
        }
    }
    
}
