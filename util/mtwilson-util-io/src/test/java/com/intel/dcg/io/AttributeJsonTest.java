/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcg.io;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.dcsg.cpg.io.Attributes;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class AttributeJsonTest {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AttributeJsonTest.class);

    public static class AttributesWithUseClassPropertyType {

        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "type")
        protected Map<String, Object> attributes = new HashMap<>();

        @JsonAnyGetter
        public Map<String, Object> getAttributeMap() {
            log.debug("AttributesWithUseClassPropertyType.getAttributeMap");
            return attributes;
        }

        @JsonIgnore
        public void setAttributeMap(Map<String, Object> map) {
            log.debug("AttributesWithUseClassPropertyType.setAttributeMap");
            attributes = map;
        }

        @JsonAnySetter
        public void set(String key, Object value) {
            log.debug("AttributesWithUseClassPropertyType.set {} -> {}", key, value);
            attributes.put(key, value);
        }

        public Object get(String key) {
            log.debug("AttributesWithUseClassPropertyType.get {} -> {}", key, attributes.get(key));
            return attributes.get(key);
        }
    }

    public static class RectangleWithPrivateMembers extends AttributesWithUseClassPropertyType {

        private Integer length;
        private Integer width;

        public Integer getLength() {
            log.debug("RectangleWithPrivateMembers.getLength -> {}", length);
            return length;
        }

        public Integer getWidth() {
            log.debug("RectangleWithPrivateMembers.getWidth -> {}", width);
            return width;
        }

        public void setLength(Integer length) {
            log.debug("RectangleWithPrivateMembers.setLength {}", length);
            this.length = length;
        }

        public void setWidth(Integer width) {
            log.debug("RectangleWithPrivateMembers.setWidth {}", width);
            this.width = width;
        }
    }

    public static class RectangleWithAttributeMembers extends AttributesWithUseClassPropertyType {

//        @JsonGetter("length")
        public Integer getLength() {
            log.debug("RectangleWithAttributeMembers.getLength -> {}", (Integer) get("length"));
            return (Integer) get("length");
        }

//        @JsonGetter("width")
        public Integer getWidth() {
            log.debug("RectangleWithAttributeMembers.getWidth -> {}", (Integer) get("width"));
            return (Integer) get("width");
        }

//        @JsonSetter("length")
        public void setLength(Integer length) {
            log.debug("RectangleWithAttributeMembers.setLength {}", length);
            set("length", length);
        }

//        @JsonSetter("width")
        public void setWidth(Integer width) {
            log.debug("RectangleWithAttributeMembers.setWidth {}", width);
            set("width", width);
        }
    }

    /**
     * putting @JsonUnwrapped on an attributes HashMap or on a getter  resulted in rectangle: {"attributes":{"color":"red"},"length":5,"width":2}  which is like annotation is ignored
     * 
     * Using the Attributes class worked:  rectangle: {"color":"red","length":5,"width":2}
     */
    public static class ParentWithAttributesMap {
        /*
        protected Map<String, Object> attributes = new HashMap<>();

        @JsonUnwrapped
        public Map<String, Object> getAttributes() {
            return attributes;
        }

        public void setAttributes(Map<String, Object> attributes) {
            this.attributes = attributes;
        }
        */
        
        @JsonUnwrapped
        protected final Attributes attributes = new Attributes();

        public Attributes getAttributes() {
            log.debug("getAttributes");
            return attributes;
        }

    }
    
    public static class RectangleWithAttributesMap extends ParentWithAttributesMap {
        private Integer length;
        private Integer width;
        
        public RectangleWithAttributesMap() {
            getAttributes().exclude("length", "width");
        }

        public Integer getLength() {
            log.debug("RectangleWithAttributesMap.getLength {}", length);
            return length;
        }

        public void setLength(Integer length) {
            log.debug("RectangleWithAttributesMap.setLength {}", length);
            this.length = length;
        }

        public Integer getWidth() {
            log.debug("RectangleWithAttributesMap.getWidth {}", width);
            return width;
        }

        public void setWidth(Integer width) {
            log.debug("RectangleWithAttributesMap.setWidth {}", width);
            this.width = width;
        }

        
        
    }
    
    
    /**
     * <pre>
     * 2015-03-08 06:52:55,352 DEBUG [main] c.i.d.i.AttributeJsonTest [AttributeJsonTest.java:110] testPrivateJsonSerialize
     * 2015-03-08 06:52:55,383 DEBUG [main] c.i.d.i.AttributeJsonTest [AttributeJsonTest.java:71] RectangleWithPrivateMembers.setLength 5
     * 2015-03-08 06:52:55,383 DEBUG [main] c.i.d.i.AttributeJsonTest [AttributeJsonTest.java:76] RectangleWithPrivateMembers.setWidth 2
     * 2015-03-08 06:52:55,383 DEBUG [main] c.i.d.i.AttributeJsonTest [AttributeJsonTest.java:46] AttributesWithUseClassPropertyType.set color -> red
     * 2015-03-08 06:52:55,601 DEBUG [main] c.i.d.i.AttributeJsonTest [AttributeJsonTest.java:61] RectangleWithPrivateMembers.getLength -> 5
     * 2015-03-08 06:52:55,617 DEBUG [main] c.i.d.i.AttributeJsonTest [AttributeJsonTest.java:66] RectangleWithPrivateMembers.getWidth -> 2
     * 2015-03-08 06:52:55,617 DEBUG [main] c.i.d.i.AttributeJsonTest [AttributeJsonTest.java:34] AttributesWithUseClassPropertyType.getAttributeMap
     * 2015-03-08 06:52:55,617 DEBUG [main] c.i.d.i.AttributeJsonTest [AttributeJsonTest.java:116] rectangle: {"length":5,"width":2,"color":"red"}
     * </pre>
     *
     * @throws JsonProcessingException
     */
    @Test
    public void testPrivateJsonSerialize() throws JsonProcessingException {
        log.debug("testPrivateJsonSerialize");
        RectangleWithPrivateMembers rectangle = new RectangleWithPrivateMembers();
        rectangle.setLength(5);
        rectangle.setWidth(2);
        rectangle.set("color", "red");
        ObjectMapper mapper = new ObjectMapper();
        log.debug("rectangle: {}", mapper.writeValueAsString(rectangle)); // rectangle: {"length":5,"width":2,"color":"red"}
    }

    /**
     * <pre>
     * 2015-03-08 06:55:22,153 DEBUG [main] c.i.d.i.AttributeJsonTest [AttributeJsonTest.java:134] testPrivateJsonDeserialize
     * 2015-03-08 06:55:22,418 DEBUG [main] c.i.d.i.AttributeJsonTest [AttributeJsonTest.java:71] RectangleWithPrivateMembers.setLength 5
     * 2015-03-08 06:55:22,418 DEBUG [main] c.i.d.i.AttributeJsonTest [AttributeJsonTest.java:76] RectangleWithPrivateMembers.setWidth 2
     * 2015-03-08 06:55:22,418 DEBUG [main] c.i.d.i.AttributeJsonTest [AttributeJsonTest.java:46] AttributesWithUseClassPropertyType.set color -> red
     * 2015-03-08 06:55:22,434 DEBUG [main] c.i.d.i.AttributeJsonTest [AttributeJsonTest.java:61] RectangleWithPrivateMembers.getLength -> 5
     * 2015-03-08 06:55:22,449 DEBUG [main] c.i.d.i.AttributeJsonTest [AttributeJsonTest.java:66] RectangleWithPrivateMembers.getWidth -> 2
     * 2015-03-08 06:55:22,449 DEBUG [main] c.i.d.i.AttributeJsonTest [AttributeJsonTest.java:34] AttributesWithUseClassPropertyType.getAttributeMap
     * 2015-03-08 06:55:22,449 DEBUG [main] c.i.d.i.AttributeJsonTest [AttributeJsonTest.java:138] rectangle: {"length":5,"width":2,"color":"red"}
     * 2015-03-08 06:55:22,449 DEBUG [main] c.i.d.i.AttributeJsonTest [AttributeJsonTest.java:51] AttributesWithUseClassPropertyType.get color -> red
     * 2015-03-08 06:55:22,449 DEBUG [main] c.i.d.i.AttributeJsonTest [AttributeJsonTest.java:61] RectangleWithPrivateMembers.getLength -> 5
     * 2015-03-08 06:55:22,449 DEBUG [main] c.i.d.i.AttributeJsonTest [AttributeJsonTest.java:66] RectangleWithPrivateMembers.getWidth -> 2
     * 2015-03-08 06:55:22,449 DEBUG [main] c.i.d.i.AttributeJsonTest [AttributeJsonTest.java:51] AttributesWithUseClassPropertyType.get length -> null
     * 2015-03-08 06:55:22,449 DEBUG [main] c.i.d.i.AttributeJsonTest [AttributeJsonTest.java:51] AttributesWithUseClassPropertyType.get width -> null
     * *
     * </pre>
     *
     * @throws JsonProcessingException
     * @throws IOException
     */
    @Test
    public void testPrivateJsonDeserialize() throws JsonProcessingException, IOException {
        log.debug("testPrivateJsonDeserialize");
        String json = "{\"length\":5,\"width\":2,\"color\":\"red\",\"length\":9}"; // second length overrides first, but still goes to the setter/getter
        ObjectMapper mapper = new ObjectMapper();
        RectangleWithPrivateMembers rectangle = mapper.readValue(json, RectangleWithPrivateMembers.class);
        log.debug("rectangle: {}", mapper.writeValueAsString(rectangle));
        assertEquals("red", rectangle.get("color"));
        assertEquals(9, (Object) rectangle.getLength());
        assertEquals(2, (Object) rectangle.getWidth());
        assertNull(rectangle.get("length"));
        assertNull(rectangle.get("width"));
    }

    /**
     * <pre>
     * 2015-03-08 06:55:55,519 DEBUG [main] c.i.d.i.AttributeJsonTest [AttributeJsonTest.java:167] testAttributeJsonSerialize
     * 2015-03-08 06:55:55,551 DEBUG [main] c.i.d.i.AttributeJsonTest [AttributeJsonTest.java:96] RectangleWithPrivateMembers.setLength 5
     * 2015-03-08 06:55:55,551 DEBUG [main] c.i.d.i.AttributeJsonTest [AttributeJsonTest.java:46] AttributesWithUseClassPropertyType.set length -> 5
     * 2015-03-08 06:55:55,551 DEBUG [main] c.i.d.i.AttributeJsonTest [AttributeJsonTest.java:101] RectangleWithPrivateMembers.setWidth 2
     * 2015-03-08 06:55:55,551 DEBUG [main] c.i.d.i.AttributeJsonTest [AttributeJsonTest.java:46] AttributesWithUseClassPropertyType.set width -> 2
     * 2015-03-08 06:55:55,551 DEBUG [main] c.i.d.i.AttributeJsonTest [AttributeJsonTest.java:46] AttributesWithUseClassPropertyType.set color -> red
     * 2015-03-08 06:55:55,769 DEBUG [main] c.i.d.i.AttributeJsonTest [AttributeJsonTest.java:34] AttributesWithUseClassPropertyType.getAttributeMap
     * 2015-03-08 06:55:55,785 DEBUG [main] c.i.d.i.AttributeJsonTest [AttributeJsonTest.java:173] rectangle: {"color":"red","width":2,"length":5}
     * *
     * </pre>
     *
     * @throws JsonProcessingException
     */
    @Test
    public void testAttributeJsonSerialize() throws JsonProcessingException {
        log.debug("testAttributeJsonSerialize");
        RectangleWithAttributeMembers rectangle = new RectangleWithAttributeMembers();
        rectangle.setLength(5);
        rectangle.setWidth(2);
        rectangle.set("color", "red");
        ObjectMapper mapper = new ObjectMapper();
        log.debug("rectangle: {}", mapper.writeValueAsString(rectangle)); // rectangle: {"length":5,"width":2,"color":"red"}
    }

    /**
     * <pre>
     * 2015-03-08 06:57:57,395 DEBUG [main] c.i.d.i.AttributeJsonTest [AttributeJsonTest.java:192] testAttributeJsonDeserialize
     * 2015-03-08 06:57:57,676 DEBUG [main] c.i.d.i.AttributeJsonTest [AttributeJsonTest.java:46] AttributesWithUseClassPropertyType.set length -> 5
     * 2015-03-08 06:57:57,676 DEBUG [main] c.i.d.i.AttributeJsonTest [AttributeJsonTest.java:46] AttributesWithUseClassPropertyType.set width -> 2
     * 2015-03-08 06:57:57,676 DEBUG [main] c.i.d.i.AttributeJsonTest [AttributeJsonTest.java:46] AttributesWithUseClassPropertyType.set color -> red
     * 2015-03-08 06:57:57,692 DEBUG [main] c.i.d.i.AttributeJsonTest [AttributeJsonTest.java:34] AttributesWithUseClassPropertyType.getAttributeMap
     * 2015-03-08 06:57:57,692 DEBUG [main] c.i.d.i.AttributeJsonTest [AttributeJsonTest.java:196] rectangle: {"color":"red","width":2,"length":5}
     * 2015-03-08 06:57:57,692 DEBUG [main] c.i.d.i.AttributeJsonTest [AttributeJsonTest.java:51] AttributesWithUseClassPropertyType.get color -> red
     * 2015-03-08 06:57:57,692 DEBUG [main] c.i.d.i.AttributeJsonTest [AttributeJsonTest.java:51] AttributesWithUseClassPropertyType.get length -> 5
     * 2015-03-08 06:57:57,692 DEBUG [main] c.i.d.i.AttributeJsonTest [AttributeJsonTest.java:85] RectangleWithAttributeMembers.getLength -> 5
     * 2015-03-08 06:57:57,692 DEBUG [main] c.i.d.i.AttributeJsonTest [AttributeJsonTest.java:51] AttributesWithUseClassPropertyType.get length -> 5
     * 2015-03-08 06:57:57,707 DEBUG [main] c.i.d.i.AttributeJsonTest [AttributeJsonTest.java:51] AttributesWithUseClassPropertyType.get width -> 2
     * 2015-03-08 06:57:57,707 DEBUG [main] c.i.d.i.AttributeJsonTest [AttributeJsonTest.java:91] RectangleWithAttributeMembers.getWidth -> 2
     * 2015-03-08 06:57:57,707 DEBUG [main] c.i.d.i.AttributeJsonTest [AttributeJsonTest.java:51] AttributesWithUseClassPropertyType.get width -> 2
     * 2015-03-08 06:57:57,707 DEBUG [main] c.i.d.i.AttributeJsonTest [AttributeJsonTest.java:51] AttributesWithUseClassPropertyType.get length -> 5
     * 2015-03-08 06:57:57,707 DEBUG [main] c.i.d.i.AttributeJsonTest [AttributeJsonTest.java:51] AttributesWithUseClassPropertyType.get width -> 2
     * </pre>
     *
     * @throws JsonProcessingException
     * @throws IOException
     */
    @Test
    public void testAttributeJsonDeserialize() throws JsonProcessingException, IOException {
        log.debug("testAttributeJsonDeserialize");
        String json = "{\"length\":5,\"width\":2,\"color\":\"red\",\"length\":9}"; // second length overrides first
        ObjectMapper mapper = new ObjectMapper();
        RectangleWithAttributeMembers rectangle = mapper.readValue(json, RectangleWithAttributeMembers.class);
        log.debug("rectangle: {}", mapper.writeValueAsString(rectangle));
        assertEquals("red", rectangle.get("color"));
        assertEquals(9, (Object) rectangle.getLength());
        assertEquals(2, (Object) rectangle.getWidth());
        assertEquals(9, rectangle.get("length"));
        assertEquals(2, rectangle.get("width"));
    }
    
    
    @Test
    public void testParentWithAttributesSerialize() throws JsonProcessingException {
        log.debug("testParentWithAttributesSerialize");
        RectangleWithAttributesMap rectangle = new RectangleWithAttributesMap();
        rectangle.setLength(5);
        rectangle.setWidth(2);
        rectangle.getAttributes().set("color", "red");
        rectangle.getAttributes().set("area", 10);
        ObjectMapper mapper = new ObjectMapper();
        log.debug("rectangle: {}", mapper.writeValueAsString(rectangle)); // rectangle: {"attributes":{"color":"red"},"length":5,"width":2}
    }
    
    /**
     * if the exclude method is not called to prevent length and width from being added, the
     * output would be like this:  // rectangle: {"color":"red","width":1,"length":2,"length":5,"width":2}
     * @throws JsonProcessingException 
     */
    @Test(expected=IllegalArgumentException.class)
    public void testParentWithConflictingAttributesSerialize() throws JsonProcessingException {
        log.debug("testParentWithAttributesSerialize");
        RectangleWithAttributesMap rectangle = new RectangleWithAttributesMap();
        rectangle.setLength(5);
        rectangle.setWidth(2);
        rectangle.getAttributes().set("color", "red");
        rectangle.getAttributes().set("length", 2); // throws IllegalArgumentException , must use setLength instead
        rectangle.getAttributes().set("width", 1);
        ObjectMapper mapper = new ObjectMapper();
        log.debug("rectangle: {}", mapper.writeValueAsString(rectangle)); // rectangle: {"color":"red","width":1,"length":2,"length":5,"width":2}
    }
    
    
    @Test
    public void testParentWithAttributesDeserialize() throws JsonProcessingException, IOException {
        log.debug("testParentWithAttributesDeserialize");
        String json = "{\"length\":5,\"width\":2,\"color\":\"red\",\"area\":10}";
        ObjectMapper mapper = new ObjectMapper();
        RectangleWithAttributesMap rectangle = mapper.readValue(json, RectangleWithAttributesMap.class);
        log.debug("rectangle: {}", mapper.writeValueAsString(rectangle));
        assertEquals("red", rectangle.getAttributes().get("color"));
        assertEquals(5, (Object) rectangle.getLength());
        assertEquals(2, (Object) rectangle.getWidth());
        assertEquals(null, rectangle.getAttributes().get("length"));
        assertEquals(null, rectangle.getAttributes().get("width"));
        assertEquals(10, rectangle.getAttributes().get("area")); // Integer
    }
    
    public static class Flavor {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
        
    }
    
    @Test
    public void testParentWithPojoAttributesSerialize() throws JsonProcessingException {
        log.debug("testParentWithPojoAttributesSerialize");
        RectangleWithAttributesMap rectangle = new RectangleWithAttributesMap();
        rectangle.setLength(5);
        rectangle.setWidth(2);
        Flavor sweet = new Flavor();
        sweet.setName("candy");
        rectangle.getAttributes().set("color", "red");
        rectangle.getAttributes().set("area", 10);
        rectangle.getAttributes().set("flavor", sweet);
        ObjectMapper mapper = new ObjectMapper();
        log.debug("rectangle: {}", mapper.writeValueAsString(rectangle)); // rectangle: {"color":"red","flavor":{"name":"candy"},"length":5,"width":2}
    }
    
    public void testParentWithPojoAttributesDeserialize() throws JsonProcessingException, IOException {
        log.debug("testParentWithPojoAttributesDeserialize");
        String json = "{\"color\":\"red\",\"flavor\":{\"name\":\"candy\"},\"length\":5,\"width\":2,\"area\":10}";
        ObjectMapper mapper = new ObjectMapper();
        RectangleWithAttributesMap rectangle = mapper.readValue(json, RectangleWithAttributesMap.class);
        log.debug("rectangle: {}", mapper.writeValueAsString(rectangle));
        assertEquals("red", rectangle.getAttributes().get("color"));
        assertEquals(5, (Object) rectangle.getLength());
        assertEquals(2, (Object) rectangle.getWidth());
        assertEquals(null, rectangle.getAttributes().get("length"));
        assertEquals(null, rectangle.getAttributes().get("width"));
        assertEquals(10, rectangle.getAttributes().get("area")); // Integer
//        assertEquals("candy", ((Flavor)rectangle.getAttributes().get("flavor")).getName()); // throws exception because flavor gets deserialized as linkedhashmap because there's no type information
        Map flavor = (Map)rectangle.getAttributes().get("flavor");
        assertEquals("candy", flavor.get("name"));
    }
    
}
