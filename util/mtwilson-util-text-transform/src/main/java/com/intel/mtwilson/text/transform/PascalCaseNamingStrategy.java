/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.text.transform;

import com.intel.mtwilson.pipe.Transformer;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 * Transforms hyphenated "hello-world" to pascal case "HelloWorld"
 * 
 * @author jbuhacoff
 */
public class PascalCaseNamingStrategy implements Transformer<String> {
    
    private final Map<String,String> map;

    public PascalCaseNamingStrategy() {
        map = null;
    }

    public PascalCaseNamingStrategy(Map<String,String> conversionMap) {
        map = conversionMap;
    }
    
    /**
     * The transformation of command-name to CommandName:
     * First letter is uppercased
     * Every letter after a hyphen is uppercased
     * Hyphens are removed 
     * @param propertyName
     * @return all-uppercase version of property name, dots converted to underscores, and camelCase words separated by underscore
     */
    public String toPascalCase(String commandName) {
        StringBuilder pascalCaseWords = new StringBuilder();
        String parts[] = commandName.split("-");
        if( parts == null || parts.length == 0 ) { return null; }
        for(int i=0; i<parts.length; i++) {
            if( map != null && map.containsKey(parts[i]) ) {
                pascalCaseWords.append(map.get(parts[i]));
            }
            else {
                pascalCaseWords.append(StringUtils.capitalize(parts[i]));
            }
        }
        return pascalCaseWords.toString();
    }

    @Override
    public String transform(String input) {
        return toPascalCase(input);
    }
    
}
