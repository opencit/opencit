/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.rfc822;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.mail.internet.ContentType;
import javax.mail.internet.HeaderTokenizer;
import javax.mail.internet.ParameterList;
import javax.mail.internet.ParseException;

/**
 *
 * @author jbuhacoff
 */
public class Rfc822Header {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Rfc822Header.class);
    
    public static class ParameterizedHeaderValue {
        private String value;
        private Map<String,String> parameters;
        
        protected ParameterizedHeaderValue(String value, Map<String,String> parameters) {
            this.value = value;
            this.parameters = parameters;
        }
        
        public String getValue() {
            return value;
        }

        public Map<String, String> getParameters() {
            return parameters;
        }
        
        
    }
    
    public static ParameterizedHeaderValue parseHeaderValue(String headerValue) throws IOException {
        String value;
        HashMap<String, String> parameterMap = new HashMap<String, String>();
        try {
            HeaderTokenizer h = new HeaderTokenizer(headerValue, HeaderTokenizer.RFC822);
            HeaderTokenizer.Token tk = h.next();
            value = tk.getValue();
//            log.debug("token type {} value {}", tk.getType(), tk.getValue());  //  for example:  token type -1 value text/plain
            String parameterText = h.getRemainder();
//            log.debug("parameters {}", parameters);
            ParameterList plist = new ParameterList(parameterText);
//            ContentType header = new ContentType(headerValue);
//            ParameterList plist = header.getParameterList();
            Enumeration<String> pnames = plist.getNames();
            while (pnames.hasMoreElements()) {
                String pname = pnames.nextElement();
                parameterMap.put(pname, plist.get(pname));
            }
        } catch (ParseException e) {
            throw new IOException("Cannot parse header parameters", e);
        }
        return new ParameterizedHeaderValue(value, parameterMap);
        
    }
    
    public static Map<String, String> getHeaderParameters(String headerValue) throws IOException {
        ParameterizedHeaderValue parameterizedHeaderValue = parseHeaderValue(headerValue);
        return parameterizedHeaderValue.getParameters();
    }
    
}
