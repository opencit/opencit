/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.patch;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.intel.mtwilson.repository.FilterCriteria;
import com.intel.mtwilson.jaxrs2.NoLinks;
import com.intel.mtwilson.jaxrs2.Patch;
import com.intel.mtwilson.patch.PatchException;
import com.intel.mtwilson.jaxrs2.PatchLink;
import java.util.List;
import java.util.Map;
import org.junit.BeforeClass;
import org.junit.Test;
import com.intel.mtwilson.patch.PatchUtil;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class RelationalPatchTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RelationalPatchTest.class);
    private static ObjectMapper mapper;
    
    @BeforeClass
    public static void createMapper() {
        mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(new PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy());
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY) // jackson 2.0
    public static class Fruit {
        public String id;
        public String fruitName;
        public String fruitColor;
        public String groveId;
        public List<String> farmerIds;

        public List<String> getFarmerIds() {
            return farmerIds;
        }

        public String getFruitColor() {
            return fruitColor;
        }

        public String getFruitName() {
            return fruitName;
        }

        public String getGroveId() {
            return groveId;
        }

        public void setFarmerIds(List<String> farmerIds) {
            this.farmerIds = farmerIds;
        }

        public void setFruitColor(String fruitColor) {
            this.fruitColor = fruitColor;
        }

        public void setFruitName(String fruitName) {
            this.fruitName = fruitName;
        }

        public void setGroveId(String groveId) {
            this.groveId = groveId;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
        
    }
    @JsonInclude(JsonInclude.Include.NON_EMPTY) // jackson 2.0
    public static class FruitFilterCriteria implements FilterCriteria<Fruit> {
        public String id;
        public String fruitNameEquals;
        public String fruitColorEquals;
        public String groveId;
        public String farmerId;
    }
    
    /**
     * 
     * XXX Problem with this approach of deserializing the "replace" block 
     * into an instance of the resource class is that any fields which are
     * missing will be null, and will be indistinguishable from fields that
     * the client sent that are null specifically to clear them -  so we
     * would have to use a separate block for clearing properties, which
     * leads to two conclusions: 1) if the second block is key-value then
     * it's either an instance of the same class again with any non-null 
     * value meaning clear, or it's similar but with all values being true/false
     * in which case we need another class to represent it, or it's a list
     * of fields to clear which means we need to map to properties w/o the
     * automated framework; 2) if we're going to map to prpoerties w/o the
     * automated framework why not just do that with the "replace" blcok 
     * then we can use nulls there to mean clear and the "wire api" can 
     * be simple, while the server api actually stays simpler than if we
     * add more objects.  if patch functionality is completely automatic 
     * anyway this won't affect resource authors. 
     * 
     * example apple resource:
{"id":"1234","fruit_name":"apple","fruit_color":"red","grove_id":"abcd"}
     * example patch document for an apple:
{"select":{"id":"1234"},"replace":{"fruit_color":"green"}}
     * example apple after applying the patch:
{"id":"1234","fruit_name":"apple","fruit_color":"green","grove_id":"abcd"}
     * @throws JsonProcessingException 
     */
    @Test
    public void testBeanPatch() throws JsonProcessingException, PatchException {
        // the initial record
        Fruit apple = new Fruit();
        apple.id = "1234";
        apple.fruitName = "apple";
        apple.fruitColor = "red";
        apple.groveId = "abcd";
        log.debug("apple: {}", mapper.writeValueAsString(apple));

        // a new instance representing a partial update
        Fruit appleUpdate = new Fruit();
        appleUpdate.id = "1234";
        appleUpdate.fruitName = "apple";
        appleUpdate.fruitColor = "green";
        appleUpdate.groveId = "abcd";
        
        // if this patch request is going to be serialized we should note which apple record we want to patch, using the filter criteria
        FruitFilterCriteria appleSearch = new FruitFilterCriteria();
        appleSearch.id = "1234";
        
        // create the patch document
        Map<String,Object> appleReplace = PatchUtil.diff(apple,appleUpdate);
        Patch<Fruit,FruitFilterCriteria,NoLinks<Fruit>> patch = new Patch<Fruit,FruitFilterCriteria,NoLinks<Fruit>>();
        patch.setSelect(appleSearch);
        patch.setReplace(appleReplace);
        
        log.debug("apple patch: {}", mapper.writeValueAsString(patch));
        
        // apply the patch to the document
        apply(patch, apple);
        
        // show the patched document
        log.debug("apple patched: {}", mapper.writeValueAsString(apple));
        
    }
    


    
    private <T,F extends FilterCriteria<T>,L extends PatchLink<T>> T apply(Patch<T,F,L> patch, T o) throws PatchException {
        return PatchUtil.apply(patch.getReplace(), o);
    }

    
    @Test
    public void testCopy() throws Exception {
        // the initial record
        Fruit apple = new Fruit();
        apple.id = "1234";
        apple.fruitName = "apple";
        apple.fruitColor = "red";
        apple.groveId = "abcd";
        log.debug("apple: {}", mapper.writeValueAsString(apple));
        
        Fruit apple2 = new Fruit();
        PatchUtil.copy(apple, apple2);
        log.debug("apple2: {}", mapper.writeValueAsString(apple2));
        
       assertEquals(mapper.writeValueAsString(apple),mapper.writeValueAsString(apple2));
    }
}
