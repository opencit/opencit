/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.patch;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy;
import com.intel.mtwilson.jersey.FilterCriteria;
import com.intel.mtwilson.jersey.NoLinks;
import com.intel.mtwilson.jersey.Patch;
import com.intel.mtwilson.jersey.PatchException;
import com.intel.mtwilson.jersey.PatchLink;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.beanutils.PropertyUtils;
import org.junit.BeforeClass;
import org.junit.Test;

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
        Map<String,Object> appleReplace = diff(apple,appleUpdate);
        Patch<Fruit,FruitFilterCriteria,NoLinks<Fruit>> patch = new Patch<Fruit,FruitFilterCriteria,NoLinks<Fruit>>();
        patch.setSelect(appleSearch);
        patch.setReplace(appleReplace);
        
        log.debug("apple patch: {}", mapper.writeValueAsString(patch));
        
        // apply the patch to the document
        apply(patch, apple);
        
        // show the patched document
        log.debug("apple patched: {}", mapper.writeValueAsString(apple));
        
    }
    

    /**
     * Returns a "replace" map showing which attributes changes from
     * o1 to o2
     * 
     * This method assumes the objects are flat -- it does not support
     * objects having arrays, lists, etc.  maybe a future version will.
     * so currently any object taht is present will replace the previous
     * value completely, which means changes to arrays or maps require the
     * full arra/map to be sent
     */
    private <T> Map<String,Object> diff(T o1, T o2) throws PatchException {
        try {
            LowerCaseWithUnderscoresStrategy namingStrategy = new LowerCaseWithUnderscoresStrategy();
            Map<String,Object> result = new HashMap<String,Object>();
            Map<String,Object> replaceAttrs = PropertyUtils.describe(o1);// throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
            for(Map.Entry<String,Object> attr : replaceAttrs.entrySet()) {
                String translatedKey = namingStrategy.translate(attr.getKey());
                // there are attributes we skip, like "class" from getClass() 
                if( attr.getKey().equals("class") ) { continue; }
                Object a1 = PropertyUtils.getSimpleProperty(o1, attr.getKey());
                Object a2 = PropertyUtils.getSimpleProperty(o2, attr.getKey());
                if( a1 == null && a2 == null ) { continue; }
                else if( a1 != null && a2 == null ) { result.put(translatedKey, null); }
                else if( a1 == null && a2 != null ) { result.put(translatedKey, a2); }
                else if( a1 != null && a2 != null && !a1.equals(a2)) { result.put(translatedKey, a2); }
            }
            return result;
        }
        catch(Exception e) {
            throw new PatchException(e);
        }
    }
    
    private <T> T apply(Patch patch, T o) throws PatchException {
        try {
            ReverseLowerCaseWithUnderscoresStrategy reverseNamingStrategy = new ReverseLowerCaseWithUnderscoresStrategy(o);        

            Map<String,Object> replaceAttrs = patch.getReplace();
            for(Map.Entry<String,Object> attr : replaceAttrs.entrySet()) {
                // there are attributes we skip, like "class" from getClass() 
                if( attr.getKey().equals("class") ) { continue; }
                log.debug("patch replace attr {} value {}", attr.getKey(), attr.getValue());
                // find the corresponding property in the object (reverse of naming strategy)
                String key = reverseNamingStrategy.translate(attr.getKey());
                PropertyUtils.setSimpleProperty(o, key, attr.getValue());
            }
            return o; // can be ignored by caller since we modify the argument
        }
        catch(Exception e) {
            throw new PatchException(e);
        }
    }
    
    /**
     * Reverse of the LowerCaseWithUnderscoresStrategy; due to the rules of 
     * the strategy the target object is required in order to map translated
     * keys to their corresponding attributes in the object.
     * 
     * For example:
     * fruit_name -> fruitName
     * 
     * Fruit fruit = new Fruit();
     * fruit.setFruitName("apple");
     * ReverseLowerCaseWithUnderscoresStrategy reverse = new ReverseLowerCaseWithUnderscoresStrategy(fruit);
     * String attrName = reverse.translate("fruit_name");  // fruitName
     * 
     */
    public static class ReverseLowerCaseWithUnderscoresStrategy {
        private static final LowerCaseWithUnderscoresStrategy namingStrategy = new LowerCaseWithUnderscoresStrategy();
        private HashMap<String,String> map = new HashMap<String,String>();
        public ReverseLowerCaseWithUnderscoresStrategy(Object target) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
            Map<String,Object> attrs = PropertyUtils.describe(target);// throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
            for(String key : attrs.keySet()) {
                map.put(namingStrategy.translate(key), key);
            }
        }
        public String translate(String key) {
            return map.get(key);
        }
    }
}
