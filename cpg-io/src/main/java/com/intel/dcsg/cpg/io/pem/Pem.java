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
 * A utility class to read and write PEM-format files.
 * 
 * Automatically base64-encodes and base64-decodes the content.  
 * 
 * @author jbuhacoff
 */
public class Pem {
    private static final Logger log = LoggerFactory.getLogger(Pem.class);
    public static final String PEM_NEWLINE = "\r\n";
    private static final String tagStart = "-----BEGIN ([a-zA-Z0-9_ ]+)-----";
    private static final String tagEnd = "-----END ([a-zA-Z0-9_ ]+)-----";
    private static final Pattern contentTagStartPattern = Pattern.compile(tagStart);
    private static final Pattern contentTagEndPattern = Pattern.compile(tagEnd);  
    private static final String headerAttributeNameEmptyPair = "([a-zA-Z0-9_-]+): ?";
    private static final String headerAttributeNameValuePair = "([a-zA-Z0-9_-]+): (?:([^\"].+)|\"([^\"]+)\")"; // value can be plain, or enclosed in double quotes
    private static final Pattern headerAttributeNameEmptyPairPattern = Pattern.compile(headerAttributeNameEmptyPair);
    private static final Pattern headerAttributeNameValuePairPattern = Pattern.compile(headerAttributeNameValuePair);
    private String contentType;
    private HashMap<String,String> headers = new HashMap<String,String>();
    private byte[] content;
    
    protected Pem() { } // used by the static valueOf() method
    
    public Pem(String contentType, byte[] content) {
        this.contentType = contentType;
        this.content = content;
    }
    public Pem(String contentType, byte[] content, Map<String,String> headers) {
        this.contentType = contentType;
        this.content = content;
        if( headers != null ) {
            this.headers.putAll(headers);
        }
    }
    
    public String getTagStart() { return "-----BEGIN "+contentType+"-----"; }
    public String getTagEnd() { return "-----END "+contentType+"-----"; }
    
    public byte[] getContent() { return content; }
    public String getContentType() { return contentType; }
    public Map<String,String> getHeaders() { return headers; }
    
    /**
     * The headers, if present, are sorted alphabetically by header name.  This is done so that if the file needs
     * to be authenticated as-is, a MAC can be reliably produced even after serializing and de-serializing
     * the file multiple times because the headers will always be in the same order. 
     * @return a PEM-like format with start tag, optional headers followed by blank line, base64-encoded content, and end tag
     */
    @Override
    public String toString() {
        String header = "";
        if( !headers.isEmpty() ) {
            Set<String> attrNames = headers.keySet();
            ArrayList<String> sortedAttrNames = new ArrayList<String>(attrNames);
            Collections.sort(sortedAttrNames);
            for(String attrName : sortedAttrNames) {
                header += String.format("%s: %s", attrName, headers.get(attrName)) + PEM_NEWLINE;
            }
            header += PEM_NEWLINE; // blank line separates headers from body
        }
        return getTagStart() + PEM_NEWLINE + 
               header + // newline already included when header is present, or if no headers are present this is an empty string
               chunk(Base64.encodeBase64String(getContent())) + PEM_NEWLINE +
               getTagEnd() + PEM_NEWLINE;        
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
            output += input.substring(cursor, Math.min(cursor+CHUNK_LENGTH, max)) + PEM_NEWLINE;
            cursor += CHUNK_LENGTH;
        } while(cursor < max);
        return output.trim(); // remove trailing newlines
    }
    
    /**
     * This method has a limitation that it only supports *one* PEM-like section per file,
     * and there must not be any other data between the body section of the PEM-like format
     * and the end of the file.
     * If you need to read multiple PEM-like sections per file, or if there is data after the
     * PEM-like sections in the file,  you need to use a better parser to extract those 
     * sections and THEN pass a single section (including its start and end tags) into this
     * method.
     * @param input
     * @return 
     */
    public static Pem valueOf(String input) {
        Pem output = new Pem();
        // first, identify the content type of the input using the tags
        Matcher tagMatcher = contentTagStartPattern.matcher(input);
        if( tagMatcher.find() ) {
            output.contentType = tagMatcher.group(1);
//            log.debug("PEM content type according to tag: {}", output.contentType);
        }
        else {
             throw new IllegalArgumentException("Input is not in PEM format");
        }
        // second, there must be a matching end-tag for the start-tag
        if( !input.contains(output.getTagEnd()) ) { throw new IllegalArgumentException("PEM input with content type '"+output.contentType+"' does not have an end tag"); }
        // third, separate the header from the body
        String content = input.replace(output.getTagStart(), "").replace(output.getTagEnd(), "").replace(PEM_NEWLINE, "\n").trim(); // the trim is to eliminate newlines that may be around the begin/end tokens
        String encodedContent = "";
        String lines[] = content.split("\n");
        for(int i=0; i<lines.length; i++) {
//            log.debug("line: {}", lines[i]);
            if( lines[i].trim().isEmpty() ) {
//                log.debug("encountered empty line");
                // from this point on all the rest of the lines are the base64-encoded encrypted key
                for(int j=i+1; j<lines.length; j++) {
                    encodedContent += lines[j];
                }
//                log.debug("base64 content: {}", encodedContent);
                break;
            }
            else {
//                log.debug("non-empty line in header");
                Matcher m = headerAttributeNameValuePairPattern.matcher(lines[i]);
                if( m.matches() ) {
                    String attributeName = m.group(1);
                    String attributeValue = m.group(2);
//                    log.debug("attr name: {}  value: {}", attributeName, attributeValue);
                    output.headers.put(attributeName, attributeValue);
                }
                else {
                    Matcher m2 = headerAttributeNameEmptyPairPattern.matcher(lines[i]);
                    if( m2.matches() ) {
                        String attributeName = m2.group(1);
//                        log.debug("attr name: {} with empty value", attributeName);
                        output.headers.put(attributeName, null);        // XXX using null to indicate the attribute was in the file but had no value... consider using empty string instead or not registering it at all
                    }
                    else {
                        // the line isn't in header format so assume that it's the start of content
                        // from this point on all the rest of the lines are the base64-encoded encrypted key
                        for(int j=i; j<lines.length; j++) {
                            encodedContent += lines[j];
                        }
//                        log.debug("base64 content: {}", encodedContent);
                        break;                    
                    }
                }
            }
        }
        output.content = Base64.decodeBase64(encodedContent);
        return output;        
    }
    
    public static boolean isPem(String input) {
        // first, identify the content type of the input using the tags
        Matcher startTagMatcher = contentTagStartPattern.matcher(input);
        if( startTagMatcher.find() ) {
            String startContentType = startTagMatcher.group(1);
//            log.debug("PEM content type according to start tag: {}", startContentType);
            Matcher endTagMatcher = contentTagEndPattern.matcher(input);
            if( endTagMatcher.find() ) {
                String endContentType = endTagMatcher.group(1);
//                log.debug("PEM content type according to end tag: {}", endContentType);
                if( startContentType.equals(endContentType) ) {
                    return true;
                }
            }
        }
        return false;
    }
}
