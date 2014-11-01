/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.intel.mtwilson.repository.FilterCriteria;
import com.intel.mtwilson.jaxrs2.Patch;
import com.intel.mtwilson.jaxrs2.PatchLink;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
//import org.codehaus.jackson.map.annotate.JsonSerialize;
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
        public List<String> farmerIds;
    }
    public static class Grove {
        public String id;
        public String groveName;
        public String city;
        public String state;
    }
    public static class Farmer {
        public String id;
        public String farmerName;
    }
    @JsonSerialize(include=JsonSerialize.Inclusion.NON_EMPTY) // jackson 1.9
    @JsonInclude(JsonInclude.Include.NON_EMPTY) // jackson 2.0
    public static class FruitCriteria implements FilterCriteria<Fruit> {
        public String id;
        public String fruitNameEquals;
        public String fruitColorEquals;
    }
    @JsonSerialize(include=JsonSerialize.Inclusion.NON_EMPTY) // jackson 1.9
    @JsonInclude(JsonInclude.Include.NON_EMPTY) // jackson 2.0
    public static class FruitLink implements PatchLink<Fruit> {
        private Grove grove;
        private List<Farmer> farmers;
        public Grove getGrove() { return grove; }
        public void setGrove(Grove grove) { this.grove = grove; }
        public List<Farmer> getFarmers() { return farmers; }
        public void setFarmers(List<Farmer> farmers) { this.farmers = farmers; }
    }
    public static class FruitPatch extends Patch<Fruit,FruitCriteria,FruitLink> {
        
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
     * 
     * {
     *   "select":{"fruit_name_equals":"apple"},
     *   "link":{
     *     "grove":{"id":"1234","grove_name":"apple grove","city":"Apple Hill","state":"California"},
     *     "farmers":[
     *       {"id":"1","farmer_name":"Jed"},
     *       {"id":"2","farmer_name":"Billy"}
     *     ]
     *   }
     * }
     * 
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
        ArrayList<Farmer> farmers = new ArrayList<Farmer>();
        Farmer farmer1 = new Farmer();
        farmer1.id = "1";
        farmer1.farmerName = "Jed";
        Farmer farmer2 = new Farmer();
        farmer2.id = "2";
        farmer2.farmerName = "Billy";
        farmers.add(farmer1);
        farmers.add(farmer2);
        FruitCriteria search = new FruitCriteria();
        search.fruitNameEquals = "apple";
        FruitLink link = new FruitLink();
        link.setGrove(grove);
        link.setFarmers(farmers);
        FruitPatch patch = new FruitPatch();
        patch.setSelect(search);
        patch.setLink(link);
        log.debug(mapper.writeValueAsString(patch)); 
    }
    
    /**
2014-01-08 09:34:30,530 DEBUG [main] t.j.JacksonPatchTest [JacksonPatchTest.java:134] patch select fruit_name_equals apple
2014-01-08 09:34:30,545 DEBUG [main] t.j.JacksonPatchTest [JacksonPatchTest.java:135] patch link grove apple grove
2014-01-08 09:34:30,546 DEBUG [main] t.j.JacksonPatchTest [JacksonPatchTest.java:138] patch link farmer Jed
2014-01-08 09:34:30,547 DEBUG [main] t.j.JacksonPatchTest [JacksonPatchTest.java:138] patch link farmer Billy     * 
     * @throws IOException 
     */
    @Test
    public void testReadPatch() throws IOException {
        String input = "{\"select\":{\"fruit_name_equals\":\"apple\"},\"link\":{\"grove\":{\"id\":\"1234\",\"grove_name\":\"apple grove\",\"city\":\"Apple Hill\",\"state\":\"California\"},\"farmers\":[{\"id\":\"1\",\"farmer_name\":\"Jed\"},{\"id\":\"2\",\"farmer_name\":\"Billy\"}]}}";
        FruitPatch patch = mapper.readValue(input, FruitPatch.class);
        log.debug("patch select fruit_name_equals {}", patch.getSelect().fruitNameEquals);
        log.debug("patch link grove {}", patch.getLink().getGrove().groveName);
        List<Farmer> farmers = patch.getLink().getFarmers();
        for(Farmer farmer : farmers) {
            log.debug("patch link farmer {}", farmer.farmerName);
        }
    }
}
