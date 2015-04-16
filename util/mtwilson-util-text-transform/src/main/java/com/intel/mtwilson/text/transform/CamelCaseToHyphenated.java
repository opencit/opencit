/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.text.transform;

import com.intel.mtwilson.pipe.Transformer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Transforms camel case "HelloWorld" to all caps "HELLO_WORLD"
 * 
 * @author jbuhacoff
 */
public class CamelCaseToHyphenated implements Transformer<String> {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CamelCaseToHyphenated.class);

    private static Pattern abbreviationEnd = Pattern.compile("([a-z])([A-Z])([A-Z]+)$"); // TODO: use the character classes to support all unicode camelcase
    private static Pattern abbreviation = Pattern.compile("([A-Z])([A-Z]+?)([A-Z][a-z])"); // TODO: use the character classes to support all unicode camelcase
    private static Pattern camelCase = Pattern.compile("([a-z])([A-Z])"); // TODO: use the character classes to support all unicode camelcase
    
    /**
     * The transformation of camelCase to hyphenated:
     * All transitions lowerUpper are separated by hyphen
     * 
     * Examples:
     * HelloWorld becomes hello-world
     * helloWorld stays hello-world
     * Hello-World becomes hello-world
     * hello-world becomes hello-world
     * 
     * 
     * @param propertyName
     * @return all-uppercase version of property name, dots converted to underscores, and camelCase words separated by underscore
     */
    public String toHyphenated(String propertyName) {
        String result = propertyName;
        // replace 
        Matcher e = abbreviationEnd.matcher(result);
        if( e.find() && e.groupCount() == 3 ) {
            log.debug("found abbreviationEnd, before: {}", result);
            result = e.replaceFirst(e.group(1)+"-"+e.group(2)+e.group(3).toLowerCase()); // turns "fooBAR" to "foo-Bar"
            log.debug("found abbreviationEnd, after: {}", result);
        }
        Matcher a = abbreviation.matcher(result);
        while( a.find() && a.groupCount() == 3 ) {
            log.debug("found abbreviation, before: {}", result);
//            log.debug("groups: {}", a.groupCount());
            result = a.replaceFirst(a.group(1)+a.group(2).toLowerCase()+"-"+a.group(3)); // turns "FOOBar" to "Foo-Bar" (they will be lowercased later)
            log.debug("found abbreviation, after: {}", result);
            a = camelCase.matcher(result);
        }
        Matcher c = camelCase.matcher(result);
        while( c.find() && c.groupCount() == 2 ) {
            log.debug("found camelCase, before: {}", result);
            result = c.replaceFirst(c.group(1)+"-"+c.group(2)); // turns "fooBar" to "foo-Bar"
            log.debug("found camelCase, after: {}", result);
            c = camelCase.matcher(result);
        }
        return result.toLowerCase();
    }

    @Override
    public String transform(String input) {
        return toHyphenated(input);
    }
    
}
