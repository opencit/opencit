/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.io.pem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class to read and write files that look like HTTP or SMTP messages.
 * 
 * Automatically base64-encodes and base64-decodes the content.  
 * 
 * @author jbuhacoff
 */
public class InternetMessage {
    private static final Logger log = LoggerFactory.getLogger(InternetMessage.class);
    public static final String NEWLINE = "\r\n";
    private static final String headerAttributeNameEmptyPair = "([a-zA-Z0-9_-]+): ?";
    private static final String headerAttributeNameValuePair = "([a-zA-Z0-9_-]+): (?:([^\"].+)|\"([^\"]+)\")"; // value can be plain, or enclosed in double quotes
    private static final Pattern headerAttributeNameEmptyPairPattern = Pattern.compile(headerAttributeNameEmptyPair);
    private static final Pattern headerAttributeNameValuePairPattern = Pattern.compile(headerAttributeNameValuePair);
    private HashMap<String,String> headers = new HashMap<String,String>();
    private String content;
    
    protected InternetMessage() { } // used by the static valueOf() method
    
    public InternetMessage(String content) {
        this.content = content;
    }
    public InternetMessage(String content, Map<String,String> headers) {
        this.content = content;
        if( headers != null ) {
            this.headers.putAll(headers);
        }
    }
    
    public String getContent() { return content; }
    public Map<String,String> getHeaders() { return headers; }
    
    private static enum ContentState {
        HEADER, BODY;
    }
    

    /**
     * Parses the content between the start and end tags, looking for
     * optional headers, empty line (when optional headers are present),
     * and the rest is the body
     * @param input
     * @return 
     */
    public static InternetMessage valueOf(String input) {
        // split up the input into lines
        String lines[] = input.split("\n");
//        log.info("parseContent(lines {}, start {}, count {})", new Object[] { lines.length, start, count });
        StringBuilder body = new StringBuilder();
        InternetMessage message = new InternetMessage();
        ContentState s = ContentState.HEADER;
        String lastHeader = null;
        for(int i=0; i<lines.length; i++) {
//            log.debug("Content line: {}", lines[i]);
            switch(s) {
                case HEADER:
                    if( lines[i].trim().isEmpty() ) {
                        s = ContentState.BODY;
                    }
                    else {
                        Matcher m = headerAttributeNameValuePairPattern.matcher(lines[i]);
                        if( m.matches() ) {
                            String attributeName = m.group(1);
                            String attributeValue = m.group(2);
//                            log.debug("attr name: {}  value: {}", attributeName, attributeValue);
                            message.headers.put(attributeName, attributeValue); 
                            lastHeader = attributeName;
                        }
                        else {
                            Matcher m2 = headerAttributeNameEmptyPairPattern.matcher(lines[i]);
                            if( m2.matches() ) {
                                String attributeName = m2.group(1);
//                                log.debug("attr name: {} with empty value", attributeName);
                                message.headers.put(attributeName, null);        // XXX using null to indicate the attribute was in the file but had no value... consider using empty string instead or not registering it at all
                                lastHeader = attributeName;
                            }
                            else {
                                if( message.headers.isEmpty() ) {
//                                    log.debug("Found start of body with no headers");
                                    s = ContentState.BODY;
                                    body.append(lines[i]);
                                }
                                else { // lastHeader guaranteed to be non-null since it is set each time we add a header... so if headers is non-empty, lastHeader is set.
//                                    log.error("Appending unexpected line format in header: {}", lines[i]); // XXX or should we append it to the last header line? (with the assumption that it got wrapped...)
                                    String attributeValue = message.headers.get(lastHeader);
                                    if( attributeValue == null ) { attributeValue = ""; }
                                    attributeValue = attributeValue.concat(lines[i]); // appends this line "as is" to the last header value... so leading spaces count. for example if the value is base64 data, you want to make sure there are no leading spaces on the extra lines,  unless your base64 parser ignores spaces anyway.
                                    message.headers.put(lastHeader, attributeValue);
                                }
                            }
                        }
                    }
                    break;
                case BODY:
                    body.append(lines[i]);
                    break;
            }
        }
        message.content = body.toString();
        return message;
    }    
    /**
     * The headers, if present, are sorted alphabetically by header name.  This is done so that if the file needs
     * to be authenticated as-is, a MAC can be reliably produced even after serializing and de-serializing
     * the file multiple times because the headers will always be in the same order. 
     * @return an HTTP-like format with optional headers followed by blank line and optionally-encoded content
     */
    @Override
    public String toString() {
        String header = "";
        if( !headers.isEmpty() ) {
            Set<String> attrNames = headers.keySet();
            ArrayList<String> sortedAttrNames = new ArrayList<String>(attrNames);
            Collections.sort(sortedAttrNames);
            for(String attrName : sortedAttrNames) {
                header += String.format("%s: %s", attrName, headers.get(attrName)) + NEWLINE;
            }
            header += NEWLINE; // blank line separates headers from body
        }
        return  
               header + // newline already included when header is present, or if no headers are present this is an empty string
//               chunk(Base64.encodeBase64String(getContent())) + NEWLINE;
               content + NEWLINE;
    }

    /**
     * This method is used to chunk the single-line output of Base64.encodeBase64String
     * 
     * XXX TODO sumbmit a feature request to Apache Commons to add Base64.encodeBase64StringChunked defined as chunk(Base64.encodeBase64String)
     * 
     * @param input any non-null string
     * @return the same string with carriage return + newlines inserted every 76 characters
     */
    private String chunk(String input) {
        final int CHUNK_LENGTH = 76;
        int cursor = 0;
        int max = input.length();
        String output = "";
        do {
            output += input.substring(cursor, Math.min(cursor+CHUNK_LENGTH, max)) + NEWLINE;
            cursor += CHUNK_LENGTH;
        } while(cursor < max);
        return output.trim(); // remove trailing newlines
    }
    
    /*
    public static boolean isInternetMessage(String input) {
        try {
            InternetMessage msg = valueOf(input);
            if( msg.content != null && msg.content.length() > 0 && msg.headers != null ) { return true; }
            return false;
        }
        catch(Exception e) {
            log.debug("Cannot parse message", e);
            return false;
        }
    }
    */
}
