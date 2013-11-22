/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jmod.cl;

import com.intel.dcsg.cpg.util.Filter;
import java.util.Collection;
import java.util.HashSet;

/**
 *
 * Handles 4 kinds of matches:
 *
 * equal to: "foo.bar.Xyz"
 *
 * starts with: "foo.bar.*" would match any class in foo.bar or sub-packages
 *
 * ends with: "*Impl" would match "foo.bar.XyzImpl"
 *
 * contains: "*.impl.*" would match anything in the foo.bar.impl package or its sub-packages.
 *
 * The wildcards must be either the first or last character of the matching string or they will not be recognized.
 *
 * @author jbuhacoff
 */
public class StringWildcardFilter implements Filter<String> {

    private HashSet<String> contains = new HashSet<String>();
    private HashSet<String> startsWith = new HashSet<String>();
    private HashSet<String> endsWith = new HashSet<String>();
    private HashSet<String> equalTo = new HashSet<String>();

    /**
     * You must call add(String) or addAll(Collection) to add criteria before using the filter or
     * else it will reject everything.
     * 
     */
    public StringWildcardFilter() {
    }
    public StringWildcardFilter(Collection<String> matchPatterns) {
        addAll(matchPatterns);
    }
    
    public final void add(String pattern) {
        if (pattern.startsWith("*") && pattern.endsWith("*")) {
            contains.add(pattern.substring(1, pattern.length() - 1)); // strip off the wildcards
        } else if (pattern.startsWith("*")) {
            startsWith.add(pattern.substring(1));
        } else if (pattern.endsWith("*")) {
            endsWith.add(pattern.substring(0, pattern.length() - 1));
        } else {
            equalTo.add(pattern);
        }        
    }
    
    public final void addAll(Collection<String> patterns) {
        for (String pattern : patterns) {
            add(pattern);
        }        
    }

    @Override
    public boolean accept(String item) {
        for (String pattern : equalTo) {
            if (pattern.equals(item)) {
                return true;
            }
        }
        for (String pattern : startsWith) {
            if (pattern.startsWith(item)) {
                return true;
            }
        }
        for (String pattern : endsWith) {
            if (pattern.endsWith(item)) {
                return true;
            }
        }
        for (String pattern : contains) {
            if (pattern.contains(item)) {
                return true;
            }
        }
        return false;
    }
}
