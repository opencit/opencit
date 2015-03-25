/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.io.pem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
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
 * 
 * General behavior for a structure that serializes to a PEM-like format with a
 * banner, headers, and content.
 *
 * This is an example of a PEM format file of an RSA private key. Notice it has
 * http/smtp-like headers, an empty line, and then base64-encoded-chunked
 * content.
 * 
 * <pre>
 * -----BEGIN RSA PRIVATE KEY-----
 * Proc-Type: 4,ENCRYPTED
 * DEK-Info: DES-EDE3-CBC,F2D4E6438DBD4EA8
 *
 * LjKQ2r1Yt9foxbHdLKZeClqZuzN7PoEmy+b+dKq9qibaH4pRcwATuWt4/Jzl6y85
 * NHM6CM4bOV1MHkyD01tFsT4kJ0GwRPg4tKAiTNjE4Yrz9V3rESiQKridtXMOToEp
 * Mj2nSvVKRSNEeG33GNIYUeMfSSc3oTmZVOlHNp9f8LEYWNmIjfzlHExvgJaPrixX
 * QiPGJ6K05kV5FJWRPET9vI+kyouAm6DBcyAhmR80NYRvaBbXGM/MxBgQ7koFVaI5
 * zoJ/NBdEIMdHNUh0h11GQCXAQXOSL6Fx2hRdcicm6j1CPd3AFrTt9EATmd4Hj+D4
 * 91jDYXElALfdSbiO0A9Mz6USUepTXwlfVV/cbBpLRz5Rqnyg2EwI2tZRU+E+Cusb
 * /b6hcuWyzva895YMUCSyDaLgSsIqRWmXxQV1W2bAgRbs8jD8VF+G9w==
 * -----END RSA PRIVATE KEY-----
 * </pre>
 *
 * See also:
 * http://etutorials.org/Programming/secure+programming/Chapter+7.+Public+Key+Cryptography/7.17+Representing+Keys+and+Certificates+in+Plaintext+PEM+Encoding/
 *
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
    private String banner;
    private Map<String,String> headers; 
    private byte[] content;
    
// used by the static valueOf() method
    protected Pem() {
        this.banner = null;
        this.content = null;
        this.headers =  new LinkedHashMap<>(); // linked hash map preserves insertion order when iterating
    } 

    /**
     * 
     * @param banner for example "PUBLIC KEY" or "CERTIFICATE"
     * @param content binary content, for example publicKey.getEncoded() or certificate.getEncoded(); it will be automatically base64-encoded by Pem.toString()
     */
    public Pem(String banner, byte[] content) {
        this.banner = banner;
        this.content = content;
        this.headers =  new LinkedHashMap<>(); // linked hash map preserves insertion order when iterating
    }
    
    /**
     * 
     * @param banner for the banner, for example "PUBLIC KEY" or "CERTIFICATE"
     * @param content binary content, for example publicKey.getEncoded() or certificate.getEncoded(); it will be automatically base64-encoded by Pem.toString()
     * @param headers name-value pairs to appear between the begin banner and the base64-encoded content; will be copied so original map will not be modified
     */
    public Pem(String banner, byte[] content, Map<String,String> headers) {
        this.banner = banner;
        this.content = content;
        this.headers = headers;
    }
    
    /** 
     * Copy constructor, changes to the new instance content or headers 
     * will not be reflected in
     * the original instance
     * @param original 
     */
    public Pem(Pem original) {
        this.banner = original.banner;
        this.content = new byte[original.content.length];
        System.arraycopy(original.content, 0, this.content, 0, original.content.length);
        this.headers =  new LinkedHashMap<>(); // linked hash map preserves insertion order when iterating
        this.headers.putAll(original.headers);
    }
    
    public String getTagStart() { return "-----BEGIN "+banner+"-----"; }
    public String getTagEnd() { return "-----END "+banner+"-----"; }
    
    public byte[] getContent() { return content; }
    public String getBanner() { return banner; }
    public Map<String,String> getHeaders() { return headers; }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
    
    // convenience methods, instead of  getHeaders().get(name) write the more concise getHeader(name)
    
    public String getHeader(String headerName) { return headers.get(headerName); }
    public void setHeader(String headerName, String headerValue) { headers.put(headerName, headerValue); }
    public void removeHeader(String headerName) { headers.remove(headerName); }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public void setBanner(String contentType) {
        this.banner = contentType;
    }
    
    
    /**
     * The headers, if present, are sorted alphabetically by header name.  This is done so that if the file needs
     * to be authenticated as-is, a MAC can be reliably produced even after serializing and de-serializing
     * the file multiple times because the headers will always be in the same order. 
     * @return a PEM-like format with start tag, optional headers followed by blank line, base64-encoded content, and end tag
     */
    @Override
    public String toString() {
        StringBuilder header = new StringBuilder();
        if( !headers.isEmpty() ) {
            Set<String> attrNames = headers.keySet();
            ArrayList<String> sortedAttrNames = new ArrayList<>(attrNames);
            Collections.sort(sortedAttrNames);
            for(String attrName : sortedAttrNames) {
                if( headers.get(attrName) != null ) {
                    header.append(String.format("%s: %s", attrName, headers.get(attrName)));
                    header.append(PEM_NEWLINE);
                }
            }
            header.append(PEM_NEWLINE); // blank line separates headers from body
        }
        return getTagStart() + PEM_NEWLINE + 
               header.toString() + // newline already included when header is present, or if no headers are present this is an empty string
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
        StringBuilder output = new StringBuilder();
        do {
            output.append(input.substring(cursor, Math.min(cursor+CHUNK_LENGTH, max)));
            output.append(PEM_NEWLINE);
            cursor += CHUNK_LENGTH;
        } while(cursor < max);
        return output.toString().trim(); // remove trailing newlines
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
            output.banner = tagMatcher.group(1);
//            log.debug("PEM content type according to tag: {}", output.contentType);
        }
        else {
             throw new IllegalArgumentException("Input is not in PEM format");
        }
        // second, there must be a matching end-tag for the start-tag
        if( !input.contains(output.getTagEnd()) ) { throw new IllegalArgumentException("PEM input with content type '"+output.banner+"' does not have an end tag"); }
        // third, separate the header from the body
        String content = input.replace(output.getTagStart(), "").replace(output.getTagEnd(), "").replace(PEM_NEWLINE, "\n").trim(); // the trim is to eliminate newlines that may be around the begin/end tokens
        StringBuilder encodedContent = new StringBuilder();
        String lines[] = content.split("\n");
        for(int i=0; i<lines.length; i++) {
//            log.debug("line: {}", lines[i]);
            if( lines[i].trim().isEmpty() ) {
//                log.debug("encountered empty line");
                // from this point on all the rest of the lines are the base64-encoded encrypted key
                for(int j=i+1; j<lines.length; j++) {
                    encodedContent.append(lines[j]);
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
                        output.headers.put(attributeName, "");
                    }
                    else {
                        // the line isn't in header format so assume that it's the start of content
                        // from this point on all the rest of the lines are the base64-encoded encrypted key
                        for(int j=i; j<lines.length; j++) {
                            encodedContent.append(lines[j]);
                        }
//                        log.debug("base64 content: {}", encodedContent);
                        break;                    
                    }
                }
            }
        }
        output.content = Base64.decodeBase64(encodedContent.toString());
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
