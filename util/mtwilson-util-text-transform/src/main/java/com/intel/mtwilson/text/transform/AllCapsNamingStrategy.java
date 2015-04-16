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
public class AllCapsNamingStrategy implements Transformer<String> {
    private static Pattern camelCase = Pattern.compile("[a-z][A-Z]"); // TODO: use the character classes to support all unicode camelcase
    
    /**
     * The transformation of dot.camelCase to ALL_CAPS:
     * All letters are uppercased.
     * Dots are converted to underscores.
     * CamelCase is converted to SEPARATE_WORDS.
     * 
     * Examples:
     * mtwilson.api.ssl.requireTrustedCertificate becomes MTWILSON_API_SSL_REQUIRE_TRUSTED_CERTIFICATE
     * MTWILSON_API_SSL_REQUIRE_TRUSTED_CERTIFICATE stays MTWILSON_API_SSL_REQUIRE_TRUSTED_CERTIFICATE
     * mtwilson.api.ssl.verifyHostname becomes MTWILSON_API_SSL_VERIFY_HOSTNAME
     * mtwilson.api.SSL_verifyHostname becomes MTWILSON_API_SSL_VERIFY_HOSTNAME
     * 
     * 
     * @param propertyName
     * @return all-uppercase version of property name, dots converted to underscores, and camelCase words separated by underscore
     */
    public String toAllCaps(String propertyName) {
        StringBuilder underscoreWords = new StringBuilder();
        Matcher m = camelCase.matcher(propertyName);
        int cur = 0;
        while( m.find() ) {
            int end = m.end(); // one after the uppercase character
            underscoreWords.append(propertyName.substring(cur, end-1)).append('_');
            cur = end-1;
        }
        underscoreWords.append(propertyName.substring(cur));
        String allCaps = underscoreWords.toString().toUpperCase().replace(".", "_");
        return allCaps;
    }

    @Override
    public String transform(String input) {
        return toAllCaps(input);
    }
    
}
