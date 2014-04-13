/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jersey.patch;

import com.fasterxml.jackson.databind.PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy;
import com.intel.mtwilson.jersey.PatchException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.beanutils.PropertyUtils;

/**
 *
 * @author jbuhacoff
 */
public class PatchUtil {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PatchUtil.class);
    
    public static <T> T apply(Map<String,Object> replaceAttrs, T o) throws PatchException {
        try {
            ReverseLowerCaseWithUnderscoresStrategy reverseNamingStrategy = new ReverseLowerCaseWithUnderscoresStrategy(o);        
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
            Map<String,Object> result = new HashMap<String,Object>();
            Map<String,Object> replaceAttrs = PropertyUtils.describe(o1);// throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
            for(Map.Entry<String,Object> attr : replaceAttrs.entrySet()) {
                String translatedKey = namingStrategy.translate(attr.getKey());
                log.debug("BlockRPC: Attribute key value pair is {}-{}", attr.getKey(), attr.getValue());
                log.debug("BlockRPC: Attribute key and translated key are {} - {}", attr.getKey(), translatedKey);
                // there are attributes we skip, like "class" from getClass() 
                if( attr.getKey().equals("class") ) { continue; }
                Object a1 = PropertyUtils.getSimpleProperty(o1, attr.getKey());
                Object a2 = PropertyUtils.getSimpleProperty(o2, attr.getKey());
                if( a1 == null && a2 == null ) { continue; }
                else if( a1 != null && a2 == null ) { result.put(translatedKey, a1); }
                else if( a1 == null && a2 != null ) { result.put(translatedKey, a2); }
                else if( a1 != null && a2 != null && !a1.equals(a2)) { result.put(translatedKey, a2); }
            }
            return result;
        }
        catch(Exception e) {
            throw new PatchException(e);
        }
    }    
}
