/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jackson;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.beanutils.PropertyUtils;

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
public class ReverseLowerCaseWithUnderscoresStrategy {
    private static final PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy namingStrategy = new PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy();
    private HashMap<String, String> map = new HashMap<String, String>();

    public ReverseLowerCaseWithUnderscoresStrategy(Object target) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Map<String, Object> attrs = PropertyUtils.describe(target); // throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
        for (String key : attrs.keySet()) {
            map.put(namingStrategy.translate(key), key);
        }
    }

    public String translate(String key) {
        return map.get(key);
    }
    
}
