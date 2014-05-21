/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jaxrs2.server;

import com.intel.mtwilson.i18n.BundleName;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author jbuhacoff
 */
public class Util {

    // XXX TODO  jersey already has this but doesn't appear to be available from the context of an Exception Mapper 
    // so just basic implementation right now for json, xml, yaml.  use first one we know about.
    public static MediaType getAcceptableMediaType( List<MediaType> acceptableMediaTypes) {
        if( acceptableMediaTypes.isEmpty() ) { return MediaType.WILDCARD_TYPE; }
        for(MediaType type : acceptableMediaTypes) {
            if( type.isCompatible(MediaType.APPLICATION_JSON_TYPE) ) {
                return MediaType.APPLICATION_JSON_TYPE;
            }
            if( type.isCompatible(MediaType.APPLICATION_XML_TYPE) ) {
                return MediaType.APPLICATION_XML_TYPE;
            }
            if( type.isCompatible(MediaType.TEXT_PLAIN_TYPE)) {
                return MediaType.TEXT_PLAIN_TYPE;
            }
        }
        return acceptableMediaTypes.get(0);
    }
    
    public static Locale getAcceptableLocale(List<Locale> acceptableLanguages) {
//        ArrayList<Locale> list = new ArrayList<Locale>();
        Locale locale = null;
//        Enumeration<Locale> locales = request.getLocales(); // in priority order based on the accept language header in the request; if request doesn't specify then it contains the server's default locale (java-provided, not mtwilson-configured)
//        while( locales.hasMoreElements() ) {
//            list.add(locales.nextElement());
//        }
        if( acceptableLanguages.isEmpty() ) {
//            list.add(Locale.getDefault()); // should never happen since the enumeration includes the platform default at the end
            locale = Locale.getDefault();
        }
        else if( acceptableLanguages.size() == 1 ) {
            // simple case: either user specified just one language or no language at all
            locale = acceptableLanguages.get(0);
        }
        else if( acceptableLanguages.size() > 1 ) {
            // check each locale to see if we have a bundle for it
            int i;
            int max = acceptableLanguages.size() - 1; // no need to evaluate the last locale, it will be our default
            for(i=0; i<max && locale==null; i++) {
                locale = acceptableLanguages.get(i);
                ResourceBundle bundle = ResourceBundle.getBundle(BundleName.MTWILSON_STRINGS.bundle(), locale);
                if( bundle.getLocale().equals(Locale.getDefault())) {
                    locale = null; // reset for next iteration
                }
            }
            if( locale == null ) {
                locale = acceptableLanguages.get(i); // last element
            }
        }
        return locale;
    }
        
}
