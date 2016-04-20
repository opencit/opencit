/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.xml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import org.opensaml.xml.parse.ClasspathResolver;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

/**
 *
 * @author jbuhacoff
 */
public class ClasspathResourceResolver extends ClasspathResolver /*implements LSResourceResolver*/ {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ClasspathResourceResolver.class);
    /*
    @Override
    public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
        log.debug("resolveResource type {} type namespaceURI {} publicId {} systemId {} baseURI {}", type, namespaceURI, publicId, systemId, baseURI);
        try {
            URL url = new URL(systemId);
            return new ClasspathResolver.LSInputImpl(publicId, systemId, url.openStream());
        }
        catch(MalformedURLException e) {
            log.error("Invalid URL in systemId: {}: {}", systemId, e.getMessage());
            return null;
        }
        catch(IOException e) {
            log.error("Cannot open input stream for systemId: {}: {}", systemId, e.getMessage());
            return null;
        }
    }
    */
    
    /**
     * Instead of looking for classpath:/path/to/resource.xsd, this resolver
     * accepts the URLs that are used in the original xsd's and uses the
     * filename at the end of the URL to search in the classpath. 
     * @param publicId
     * @param systemId
     * @return 
     */
    @Override
    protected InputStream resolver(String publicId, String systemId) {
        InputStream in = openClasspathInputStream(systemId);
        if( in == null ) {
            in = openClasspathInputStream(publicId);
        }
        return in;
    }
    
    protected InputStream openClasspathInputStream(String href) {
        if( href == null || href.isEmpty() || href.startsWith("-") ) { return null; } // example of startsWith("-") is  -//W3C//DTD XMLSchema 200102//EN
        try {
            if( href.startsWith("http") ) {
                URL url = new URL(href); // url like http://docs.oasis-open.org/security/saml/v2.0/saml-schema-assertion-2.0.xsd
                File file = new File(url.getPath());
                InputStream in = getClass().getResourceAsStream("/"+file.getName());
                log.debug("tried to resolve href {} to {}", href, in);
                return in;
            }
            else {
                InputStream in = getClass().getResourceAsStream("/"+href); // plain filename like saml-schema-assertion-2.0.xsd
                log.debug("tried to resolve href {} to {}", href, in);
//                if( in == null && !href.startsWith("/")) {
//                    in = getClass().getResourceAsStream("/"+href);
//                    log.debug("tried again to resolve href /{} to {}", href, in);
//                }
                return in;
            }
        }
        catch(IOException e) {
            log.error("Cannot open input stream: {}: {}", href, e.getMessage());
            return null;
        }
    }
}
