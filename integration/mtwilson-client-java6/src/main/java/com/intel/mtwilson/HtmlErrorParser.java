/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Currently supports only Glassfish.
 * 
 * @author jbuhacoff
 */
public class HtmlErrorParser {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private String html;
    private String serverName;
    private String rootCause;
    
    public HtmlErrorParser(String html) {
        this.html = html;
        this.serverName = findServerName();
        log.debug("Server name is {}", serverName);
        this.rootCause = StringEscapeUtils.unescapeHtml(findRootCause());
        log.debug("Root cause is {}", rootCause);
    }
    
    private String findServerName() {
        if( html != null && html.contains("<h3>GlassFish Server") ) { //  Open Source Edition 3.1.2.2</h3>
            return "GlassFish";
        }
        return null;
    }
    
    public String getServerName() { return serverName; }
    
    private String findRootCause() {
        if( serverName != null && serverName.equals("GlassFish") ) {
            Pattern pRootCause = Pattern.compile(".*<b>root cause</b>"+RegexUtil.WHITESPACE_CHAR_CLASS+"*<pre>(.+)</pre>.*", Pattern.MULTILINE);
            Matcher mRootCause = pRootCause.matcher(html);
            if( mRootCause.matches() ) {
                return mRootCause.group(1);
            }
            Pattern pException = Pattern.compile(".*<b>exception</b>"+RegexUtil.WHITESPACE_CHAR_CLASS+"*<pre>(.+)</pre>.*", Pattern.MULTILINE);
            Matcher mException = pException.matcher(html);
            if( mException.matches() ) {
                return mException.group(1);
            }
        }
        log.debug("Didn't find root cause. HTML: "+html);
        return null;
    }
    
    public String getRootCause() { return rootCause; }
}
