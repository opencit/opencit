/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson;

import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convenient way to determine the current effective locale based on per-request, per-user, server configuration, and default.
 * @author jbuhacoff
 */
public class MyLocale {
    private static Logger log = LoggerFactory.getLogger(MyLocale.class);
    private Locale defaultLocale;
    
    /**
     * 
     * @param configuredLocaleName if null or empty the system default locale will be used
     */
    public MyLocale(String configuredLocaleName) {
        if( configuredLocaleName == null || configuredLocaleName.isEmpty() ) {
            defaultLocale = Locale.getDefault();
        }
        try {
//            defaultLocale = Locale.forLanguageTag(configuredLocaleName); // only available in Java 7
            throw new UnsupportedOperationException("This feature is not complete");
        }
        catch(Exception e) {
            log.error("Cannot load locale {}: {}", configuredLocaleName, e);
            defaultLocale = Locale.getDefault();
        }
    }
    public Locale getLocale() {
        // how to get per-request?  from thread context??? 
        // how to get per-user? need the principal from thread context ???
        return defaultLocale;
    }
}
