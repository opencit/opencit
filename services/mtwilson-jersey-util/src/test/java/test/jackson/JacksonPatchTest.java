/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.intel.mtwilson.jersey.Patch;
import com.intel.mtwilson.jersey.PatchLink;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author jbuhacoff
 */
public class JacksonPatchTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JacksonPatchTest.class);
    private static ObjectMapper mapper;
    
    @BeforeClass
    public static void createMapper() {
        mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(new PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy());
    }
    public static class Fruit {
        public String id;
        public String fruitName;
        public String fruitColor;
        public String groveId;
    }
    public static class Grove {
        public String id;
        public String groveName;
        public String city;
        public String state;
    }
    
    public static class FruitLink extends PatchLink<Fruit> {
        private Grove grove;
        public Grove getGrove() { return grove; }
        public void setGrove(Grove grove) { this.grove = grove; }
    }
    public static class FruitPatch extends Patch<Fruit,FruitLink> {
        
    }
    
    /**
     * 
     * {"id":null,"fruit_name":"apple","fruit_color":"red","grove_id":null}
     */
    @Test
    public void testWriteDefault() throws Exception {
        Fruit fruit = new Fruit();
        fruit.fruitName = "apple";
        fruit.fruitColor = "red";
        log.debug(mapper.writeValueAsString(fruit)); 
    }

    
    /**
     * {"select":{"id":null,"fruit_name":"apple","fruit_color":"red","grove_id":null},
     * "replace":null,
     * "link":{"grove":[{"grove":{"id":"1234","grove_name":"apple grove","city":"Apple Hill","state":"California"}}]},
     * "unlink":{},
     * "insert":null,
     * "delete":null,
     * "test":null}
     */
    @Test
    public void testWritePatch() throws Exception {
        Fruit fruit = new Fruit();
        fruit.fruitName = "apple";
        fruit.fruitColor = "red";
        Grove grove = new Grove();
        grove.id = "1234";
        grove.groveName = "apple grove";
        grove.city = "Apple Hill";
        grove.state = "California";
        FruitLink link = new FruitLink();
        link.setGrove(grove);
        FruitPatch patch = new FruitPatch();
        patch.setSelect(fruit);
        patch.link("grove", link);
        log.debug(mapper.writeValueAsString(patch)); 
    }
}
