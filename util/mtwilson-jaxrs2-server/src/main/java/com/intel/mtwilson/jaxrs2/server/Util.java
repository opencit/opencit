/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jaxrs2.server;

import com.intel.dcsg.cpg.i18n.LocaleUtil;
import com.intel.mtwilson.i18n.BundleName;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author jbuhacoff
 */
public class Util {

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
    
    /**
     * Tries to satisfy the client's acceptable language list in the order
     * provided by the client. The server's list is not ordered. If a match
     * cannot be determined the server's default locale is chosen.
     * 
     * It would be ideal if the web server could negotiate this automatically,
     * as some web servers do for static resources. Automatic negotiation would
     * require a way for the application to advertise which locales it has
     * available for dynamically-generated responses. If such a facility 
     * exists or comes into existence it should replace this utility function.
     * 
     * @param acceptableLanguages list provided by client in Accept-Language header
     * @param availableLocales configured or detected on server
     * @return the best available locale on the server for the client's request, or the server's default locale
     */
    public static Locale getAcceptableLocale(List<Locale> acceptableLanguages, String[] availableLocales) {
//        ArrayList<Locale> list = new ArrayList<Locale>();
//        Enumeration<Locale> locales = request.getLocales(); // in priority order based on the accept language header in the request; if request doesn't specify then it contains the server's default locale (java-provided, not mtwilson-configured)
//        while( locales.hasMoreElements() ) {
//            list.add(locales.nextElement());
//        }
        if( acceptableLanguages == null || acceptableLanguages.isEmpty() ) {
//            list.add(Locale.getDefault()); // should never happen since the enumeration includes the platform default at the end
            return Locale.getDefault();
        }
        else {
            HashSet<String> available = new HashSet<>(Arrays.asList(availableLocales));
            Locale locale;
            int i;
            int max = acceptableLanguages.size();
            // 1. look for exact match
            for(i=0; i<max; i++) {
                locale = acceptableLanguages.get(i);
                if( available.contains(LocaleUtil.toLanguageTag(locale)) ) {
                    return locale;
                }
            }
            // 2. look for just language code without region
            for(i=0; i<max; i++) {
                locale = acceptableLanguages.get(i);
                if( available.contains(locale.getLanguage()) ) {
                    return locale;
                }
            }
            // 3. return default locale
            return Locale.getDefault();
        }

    }
        
}
