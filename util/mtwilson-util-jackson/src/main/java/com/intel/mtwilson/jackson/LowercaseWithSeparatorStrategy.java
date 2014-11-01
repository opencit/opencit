/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jackson;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;

/**
 * Abstraction of LowercaseWithUnderscoresStrategy to allow a different separator,
 * for example a hyphen for xml-elements-like-this.
 * 
 * @author jbuhacoff
 */
public abstract class LowercaseWithSeparatorStrategy extends PropertyNamingStrategy.PropertyNamingStrategyBase {
    private char separator = '-';
    
    protected void setSeparator(char separator) {
        this.separator = separator;
    }
    
    @Override
    public String translate(String input) {
        if (input == null) {
            return input; // garbage in, garbage out
        }
        int length = input.length();
        StringBuilder result = new StringBuilder(length * 2);
        int resultLength = 0;
        boolean wasPrevTranslated = false;
        for (int i = 0; i < length; i++) {
            char c = input.charAt(i);
            if (i > 0 || c != separator) // skip first starting underscore
            {
                if (Character.isUpperCase(c)) {
                    if (!wasPrevTranslated && resultLength > 0 && result.charAt(resultLength - 1) != separator) {
                        result.append(separator);
                        resultLength++;
                    }
                    c = Character.toLowerCase(c);
                    wasPrevTranslated = true;
                } else {
                    wasPrevTranslated = false;
                }
                result.append(c);
                resultLength++;
            }
        }
        return resultLength > 0 ? result.toString() : input;
    }
}
