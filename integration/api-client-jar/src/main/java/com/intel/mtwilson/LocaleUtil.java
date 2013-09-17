/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson;

import java.util.Locale;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Java 7 provides Locale.toLanguageTag and Locale.forLanguageTag
 * 
 * @author jbuhacoff
 */
public class LocaleUtil {
    public static String toLanguageTag(Locale locale) {
        String language = locale.getLanguage();
        String country = locale.getCountry();
        if( language.isEmpty() && country.isEmpty() ) {        
            return locale.toString();
        }
        if( language.isEmpty() || country.isEmpty() ) {
            return String.format("%s%s", language, country);
        }
        return String.format("%s-%s", language, country);
    }
    
    public static Locale forLanguageTag(String localeName) {
        String language = "", country = "";
        String[] parts = localeName.split("-");
        if( parts == null ) { return Locale.getDefault(); }
        if( parts.length > 0 && ArrayUtils.contains(Locale.getISOLanguages(), parts[0]) ) {
            language = parts[0];
            if( parts.length > 1 && ArrayUtils.contains(Locale.getISOCountries(), parts[1]) ) {
                country = parts[1];
            }
        }
        else if( parts.length > 0 && ArrayUtils.contains(Locale.getISOCountries(), parts[0]) ) {
            country = parts[0];
        }
        return new Locale(language, country);
    }
    
}
