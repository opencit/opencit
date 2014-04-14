/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jersey.patch;

import com.fasterxml.jackson.databind.PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy;
import com.intel.mtwilson.jersey.PatchException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
            ReverseLowerCaseWithUnderscoresStrategy reverseNamingStrategy = new ReverseLowerCaseWithUnderscoresStrategy(target);        
            for(Map.Entry<String,Object> attr : replaceAttrs.entrySet()) {
                // there are attributes we skip, like "class" from getClass() 
                if( attr.getKey().equals("class") ) { continue; }
                // find the corresponding property in the object (reverse of naming strategy)
                String key = reverseNamingStrategy.translate(attr.getKey());
                log.debug("patch replace attr {} -> {} value {}", attr.getKey(), key, attr.getValue());
                if( targetAttrs.containsKey(key) ) {
                    PropertyUtils.setSimpleProperty(target, key, attr.getValue());
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
            ReverseLowerCaseWithUnderscoresStrategy reverseNamingStrategy = new ReverseLowerCaseWithUnderscoresStrategy(target);        
            for(Map.Entry<String,Object> attr : replaceAttrs.entrySet()) {
                // there are attributes we skip, like "class" from getClass() 
                if( attr.getKey().equals("class") ) { continue; }
                log.debug("patch replace attr {} value {}", attr.getKey(), attr.getValue());
                // find the corresponding property in the object (reverse of naming strategy)
                String key = reverseNamingStrategy.translate(attr.getKey());
                PropertyUtils.setSimpleProperty(target, key, attr.getValue());
            }
            return target; // can be ignored by caller since we modify the argument
        }
        catch(IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new PatchException(e);
        }
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
    public static <T> Map<String,Object> diff(T o1, T o2) throws PatchException {
        try {
            LowerCaseWithUnderscoresStrategy namingStrategy = new LowerCaseWithUnderscoresStrategy();
            Map<String,Object> result = new HashMap<>();
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
        catch(IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new PatchException(e);
        }
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
    public static <T> Map<String,Object> propertyMap(T source) throws PatchException {
        try {
            LowerCaseWithUnderscoresStrategy namingStrategy = new LowerCaseWithUnderscoresStrategy();
            Map<String,Object> result = new HashMap<>();
            Map<String,Object> sourceAttrs = PropertyUtils.describe(source);// throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
            for(Map.Entry<String,Object> attr : sourceAttrs.entrySet()) {
                String translatedKey = namingStrategy.translate(attr.getKey());
                // there are attributes we skip, like "class" from getClass() 
                if( attr.getKey().equals("class") ) { continue; }
                Object a1 = PropertyUtils.getSimpleProperty(source, attr.getKey());
                result.put(translatedKey, a1);
            }
            return result;
        }
        catch(IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new PatchException(e);
        }
    }
    
    public static Map<String,Object> removeNullValues(Map<String,Object> map) {
        HashSet<String> toRemove = new HashSet<>();
        for(Map.Entry<String,Object> attr : map.entrySet()) {
            if( attr.getValue() == null ) {
                toRemove.add(attr.getKey());
            }
        }
        for(String key : toRemove) {
            map.remove(key);
        }
        return map;
    }
    
    /**
     * Copies all non-null source properties to the target. 
     * 
     * @param source
     * @param target
     * @throws PatchException 
     */
    public static void patch(Object source, Object target) throws PatchException {
        Map<String,Object> properties = propertyMap(source);
        removeNullValues(properties);
        apply(properties, target);
    }
    
    /**
     * Copies all null and non-null source properties to the target.
     * 
     * @param source
     * @param target
     * @throws PatchException 
     */
    public static void copy(Object source, Object target) throws PatchException {
        Map<String,Object> properties = propertyMap(source);
        apply(properties, target);
    }
}
