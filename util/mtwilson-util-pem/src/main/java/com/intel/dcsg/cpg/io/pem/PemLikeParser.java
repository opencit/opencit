/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.io.pem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is intended to extract all PEM-like blocks from a file
 * that may have multiple PEM-like blocks embedded within other content -
 * that is, there may be content before and/or after each PEM-like block.
 * 
 * Unlike the original Pem class,  this one does NOT automatically base64-decode
 * the body... because the body could be plaintext, quoted-printable, base64,
 * or some other encoding.  
 * 
 * Note: treatment of carriage returns is currently not very well defined... so beware
 * 
 * @since 0.1
 * @author jbuhacoff
 */
public class PemLikeParser {
    private static final Logger log = LoggerFactory.getLogger(PemLikeParser.class);
    public static final String PEM_NEWLINE = "\r\n";
    private static final String tagStart = "-----BEGIN ([a-zA-Z0-9_ ]+)-----";
    private static final String tagEnd = "-----END ([a-zA-Z0-9_ ]+)-----";
    private static final Pattern contentTagStartPattern = Pattern.compile(tagStart);
    private static final Pattern contentTagEndPattern = Pattern.compile(tagEnd);  
    private static final String headerAttributeNameEmptyPair = "([a-zA-Z0-9_-]+): ?";
    private static final String headerAttributeNameValuePair = "([a-zA-Z0-9_-]+): (?:([^\"].+)|\"([^\"]+)\")"; // value can be plain, or enclosed in double quotes
    private static final Pattern headerAttributeNameEmptyPairPattern = Pattern.compile(headerAttributeNameEmptyPair);
    private static final Pattern headerAttributeNameValuePairPattern = Pattern.compile(headerAttributeNameValuePair);
    
    private static enum EnvelopeState {
        FIND_START_TAG, FIND_END_TAG;
    }
    
    private static enum ContentState {
        HEADER, BODY;
    }
    
    public static List<Pem> parse(String input) {
        ArrayList<Pem> list = new ArrayList<>();
        EnvelopeState s = EnvelopeState.FIND_START_TAG;
        int contentStart = -1;
        // normalize newlines
        String normalized = input.replaceAll(PEM_NEWLINE, "\n");
        // split up the input into lines
        String lines[] = normalized.split("\n");
        StartTagFinder startFinder = new StartTagFinder();
        EndTagFinder endFinder = new EndTagFinder();
        for(int i=0; i<lines.length; i++) {
            switch(s) {
                case FIND_START_TAG:
                    startFinder.readLine(lines[i]);
                    if( startFinder.isDone() ) {
                        contentStart = i+1; // current line is start tag, so next line is first line of content
                        s = EnvelopeState.FIND_END_TAG;
                        endFinder.init(startFinder.getStartTag());
                    }
                    break;
                case FIND_END_TAG:
                    endFinder.readLine(lines[i]);
                    if( endFinder.isDone() ) {
                        ContentInfo info = parseContent(lines, contentStart, endFinder.getCount());
                        list.add(new Pem(endFinder.getStartTag(), Base64.decodeBase64(info.body.toString()), info.headers));
                        s = EnvelopeState.FIND_START_TAG;
                        startFinder.init();
                    }
            }
        }
        return list;
    }
    
    /**
     * Parses the content between the start and end tags, looking for
     * optional headers, empty line (when optional headers are present),
     * and the rest is the body
     * @param input
     * @return 
     */
    private static ContentInfo parseContent(String[] lines, int start, int count) {
//        log.info("parseContent(lines {}, start {}, count {})", new Object[] { lines.length, start, count });
        ContentInfo info = new ContentInfo();
        ContentState s = ContentState.HEADER;
        String lastHeader = null;
        int end = start+count;
        for(int i=start; i<end; i++) {
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
                            info.headers.put(attributeName, attributeValue); 
                            lastHeader = attributeName;
                        }
                        else {
                            Matcher m2 = headerAttributeNameEmptyPairPattern.matcher(lines[i]);
                            if( m2.matches() ) {
                                String attributeName = m2.group(1);
//                                log.debug("attr name: {} with empty value", attributeName);
                                info.headers.put(attributeName, null);        // XXX using null to indicate the attribute was in the file but had no value... consider using empty string instead or not registering it at all
                                lastHeader = attributeName;
                            }
                            else {
                                if( info.headers.isEmpty() ) {
//                                    log.debug("Found start of body with no headers");
                                    s = ContentState.BODY;
                                    info.body.append(lines[i]);
                                }
                                else { // lastHeader guaranteed to be non-null since it is set each time we add a header... so if headers is non-empty, lastHeader is set.
//                                    log.error("Appending unexpected line format in header: {}", lines[i]); // XXX or should we append it to the last header line? (with the assumption that it got wrapped...)
                                    String attributeValue = info.headers.get(lastHeader);
                                    if( attributeValue == null ) { attributeValue = ""; }
                                    attributeValue = attributeValue.concat(lines[i]); // appends this line "as is" to the last header value... so leading spaces count. for example if the value is base64 data, you want to make sure there are no leading spaces on the extra lines,  unless your base64 parser ignores spaces anyway.
                                    info.headers.put(lastHeader, attributeValue);
                                }
                            }
                        }
                    }
                    break;
                case BODY:
                    info.body.append(lines[i]);
                    break;
            }
        }
        return info;
    }
    
    private static class ContentInfo {
        StringBuilder body = new StringBuilder();
        LinkedHashMap<String,String> headers = new LinkedHashMap<>(); // linked hash map  preserves insertion order
    }
    
    
    /**
     * Ignores lines until it finds a start tag.
     * When it finds a start tag, isDone() will return true and you
     * can get the start tag using getStartTag().
     * After it finds a start tag, if you want to use it again 
     * call init() first.
     */
    private static class StartTagFinder {
        private String startTag = null;
        public String getStartTag() { return startTag; }
        public boolean isDone() { return startTag != null; }
        public void init() { startTag = null; }
        public void readLine(String input) {
            Matcher tagMatcher = contentTagStartPattern.matcher(input);
            if( tagMatcher.find() ) {
                startTag = tagMatcher.group(1);
//                log.debug("Found start tag: {}", startTag);
            }
        }
    }

    /**
     * Counts lines until it finds an end tag corresponding to given
     * start tag. You must call init(startTag) before using it.
     * When it finds the end tag, isDone() will return true and you
     * can get the end tag using getEndTag(), as well as the number of
     * lines seen using getCount().   
     * 
     * This assumes that you are using
     * this in a loop wherein you already have the lines in memory
     * so it doesn't make sense to store a copy of them here.
     * 
     * After it finds an end tag, if you want to use it again 
     * call init(startTag) before using it again.
     */
    private static class EndTagFinder {
        private String startTag = null; // provided by init(startTag)
        private String endTag = null; // set when we find the end tag
        private int count = 0; // number of lines of content between the start and end tags
        public String getStartTag() { return startTag; }
        public String getEndTag() { return endTag; }
        public int getCount() { return count; }
        public boolean isDone() { return endTag != null; }
        public void init(String startTag) { this.startTag = startTag; this.endTag = null; this.count = 0; }
        public void readLine(String input) {            
            Matcher tagMatcher = contentTagEndPattern.matcher(input);
            if( tagMatcher.find() ) {
                endTag = tagMatcher.group(1);
//                log.debug("Found end tag: {}", endTag);
                if( startTag != null && !startTag.equals(endTag) ) {
//                    log.debug("Start tag {} does not match end tag {}, so ignoring end tag", startTag, endTag);
                    endTag = null;
                }
            }
            else {
                count++;
            }
        }
    }


}
