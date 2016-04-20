/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.patch;

import com.fasterxml.jackson.databind.PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.beanutils.PropertyUtils;

/**
 *
 * @author jbuhacoff
 */
public class PatchUtil {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PatchUtil.class);
    
    /**
     * If replaceAttrs contains a key that is not found in the target, it will
     * be ignored. 
     * @param <T>
     * @param replaceAttrs a map containing key-value pairs that should be set on the target object
     * @param target the object which should be updated with the provided key-value pairs in replaceAttrs
     * @return target as a convenience for chaining method calls but can be ignored since the target parameter is modified
     * @throws PatchException 
     */
    public static <T> T apply(Map<String,Object> replaceAttrs, T target) throws PatchException {
        try {
            Map<String,Object> targetAttrs = PropertyUtils.describe(target);// throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
//            ReverseLowerCaseWithUnderscoresStrategy reverseNamingStrategy = new ReverseLowerCaseWithUnderscoresStrategy(target);        
            for(Map.Entry<String,Object> attr : replaceAttrs.entrySet()) {
                // there are attributes we skip, like "class" from getClass() 
                if( attr.getKey().equals("class") ) { continue; }
                // find the corresponding property in the object (reverse of naming strategy)
//                String key = reverseNamingStrategy.translate(attr.getKey());
//                log.debug("patch replace attr {} -> {} value {}", attr.getKey(), key, attr.getValue());
                log.debug("patch replace attr {} value {}", attr.getKey(), attr.getValue());
                if( targetAttrs.containsKey(attr.getKey()) ) {
                    PropertyUtils.setSimpleProperty(target, attr.getKey(), attr.getValue());
                }
            }
            return target; // can be ignored by caller since we modify the argument
        }
        catch(IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new PatchException(e);
        }
    }

    /**
     * All keys in replaceAttrs are assumed to exist in the target so if one
     * is missing an exception will be thrown.
     * 
     * @param <T>
     * @param replaceAttrs a map containing key-value pairs that should be set on the target object
     * @param target the object which should be updated with the provided key-value pairs in replaceAttrs
     * @return target as a convenience for chaining method calls but can be ignored since the target parameter is modified
     * @throws PatchException 
     */
    public static <T> T applyAll(Map<String,Object> replaceAttrs, T target) throws PatchException {
        try {
//            ReverseLowerCaseWithUnderscoresStrategy reverseNamingStrategy = new ReverseLowerCaseWithUnderscoresStrategy(target);        
            for(Map.Entry<String,Object> attr : replaceAttrs.entrySet()) {
                // there are attributes we skip, like "class" from getClass() 
                if( attr.getKey().equals("class") ) { continue; }
                log.debug("patch replace attr {} value {}", attr.getKey(), attr.getValue());
                // find the corresponding property in the object (reverse of naming strategy)
//                String key = reverseNamingStrategy.translate(attr.getKey());
                PropertyUtils.setSimpleProperty(target, attr.getKey(), attr.getValue());
            }
            return target; // can be ignored by caller since we modify the argument
        }
        catch(IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new PatchException(e);
        }
    }
    
    /**
     * Returns a "replace" map showing which attributes change from
     * o1 to o2 such that creating a clone of o1 and applying the diff
     * map would result in o2. The value of using diff and apply 
     * instead of just copy is if you want to see what are the differences
     * and possibly let the user approve or reject individual changes.
     * If you're not using the diff in an intermediate step you can 
     * just use copy(source,target) instead and it will copy all
     * properties from the source to the target (even if they are null), or
     * use merge(source,target) to copy all non-null properties from the
     * source to the target.
     * 
     * This method assumes the objects are flat -- it does not support
     * objects having arrays, lists, etc.  maybe a future version will.
     * so currently any object taht is present will replace the previous
     * value completely, which means changes to arrays or maps require the
     * full arra/map to be sent
     */
    public static <T> Map<String,Object> diff(T o1, T o2) throws PatchException {
        try {
            Map<String,Object> result = new HashMap<>();
            Map<String,Object> replaceAttrs = PropertyUtils.describe(o1);// throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
            for(Map.Entry<String,Object> attr : replaceAttrs.entrySet()) {
                // there are attributes we skip, like "class" from getClass() 
                if( attr.getKey().equals("class") ) { continue; }
                Object a1 = PropertyUtils.getSimpleProperty(o1, attr.getKey());
                Object a2 = PropertyUtils.getSimpleProperty(o2, attr.getKey());
                if( a1 == null && a2 == null ) { continue; }
                else if( a1 != null && a2 == null ) { result.put(attr.getKey(), null); }
                else if( a1 == null && a2 != null ) { result.put(attr.getKey(), a2); }
                else if( a1 != null && a2 != null && !a1.equals(a2)) { result.put(attr.getKey(), a2); }
            }
            return result;
        }
        catch(IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new PatchException(e);
        }
    }
    
    /**
     * Creates a new map instance containing the same key-value pairs as the
     * input instance but with the keys renamed using the lowercase with underscores
     * naming strategy
     * @param input
     * @return 
     */
    public static Map<String,Object> toLowercaseWithUnderscores(Map<String,Object> input) {
        LowerCaseWithUnderscoresStrategy namingStrategy = new LowerCaseWithUnderscoresStrategy();
        HashMap<String,Object> result = new HashMap<>();
        for(Map.Entry<String,Object> kv : input.entrySet()) {
            String translatedKey = namingStrategy.translate(kv.getKey());
            result.put(translatedKey, kv.getValue());
        }
        return result;
    }

    /**
     * Returns a "replace" map showing which attributes changes from
     * o1 to o2
     * 
     * This method assumes the objects are flat -- it does not support
     * objects having arrays, lists, etc.  maybe a future version will.
     * so currently any object that is present will replace the previous
     * value completely, which means changes to arrays or maps require the
     * full array/map to be sent
     * 
     * This function wraps PropertyUtils.describe and excludes the "class"
     * attribute which ends up in the described object.
     * 
     */
    public static <T> Map<String,Object> toMap(T source) throws PatchException {
        try {
            Map<String,Object> result = new HashMap<>();
            Map<String,Object> sourceAttrs = PropertyUtils.describe(source);// throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
            for(Map.Entry<String,Object> attr : sourceAttrs.entrySet()) {
                // there are attributes we skip, like "class" from getClass() 
                if( attr.getKey().equals("class") ) { continue; }
                Object value = PropertyUtils.getSimpleProperty(source, attr.getKey());
                result.put(attr.getKey(), value);
            }
            return result;
        }
        catch(IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new PatchException(e);
        }
    }
    
    /**
     * Returns a set of key names that have null values in the map
     * @param map
     * @return 
     */
    public static <T> Set<String> nullValueKeySet(Map<String,T> map) {
        HashSet<String> nullValueKeys = new HashSet<>();
        for(Map.Entry<String,T> attr : map.entrySet()) {
            if( attr.getValue() == null ) {
                nullValueKeys.add(attr.getKey());
            }
        }
        return nullValueKeys;
    }
    
    /**
     * Modifies the input map by removing keys that have null values
     * @param map 
     */
    public static <T> void removeNullValues(Map<String,T> map) {
        Set<String> toRemove = nullValueKeySet(map);
        for(String key : toRemove) {
            map.remove(key);
        }
    }
    
    /**
     * Copies all non-null source properties to the target. 
     * Source and target need not be of the same type; only properties
     * that are available in the target would be copied from the source.
     * 
     * @param source
     * @param target
     * @throws PatchException 
     */
    public static void merge(Object source, Object target) throws PatchException {
        Map<String,Object> properties = toMap(source);
        removeNullValues(properties);
        apply(properties, target);
    }
    
    /**
     * Copies all null and non-null source properties to the target.
     * Source and target need not be of the same type; only properties
     * that are available in the target would be copied from the source.
     * 
     * @param source
     * @param target
     * @throws PatchException 
     */
    public static void copy(Object source, Object target) throws PatchException {
        Map<String,Object> properties = toMap(source);
        apply(properties, target);
    }
}
