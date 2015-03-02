/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jaxrs2;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 
 * @author jbuhacoff
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
public class Link {
    
    /**
     * The relation between the current object and the linked object.
     * Required: must set this property when creating a Link.
     */
    protected String rel;
    
    /**
     * The URL of the linked object.
     * Required: must set this property when creating a Link.
     */
    protected String href;
    
    /**
     * The media type of the linked object; this is just a hint, and
     * if the object is accessed from an HTTP server then whatever media
     * type is specified in the response overrides this
     */
    protected String type;
    
    /**
     * Optional: set this property to indicate that the linked object
     * accepts the POST method.
     * The value of this property should be
     * the content type that can be sent with a POST to the linked object.
     * Multiple content types can be separated by commas, for example:
     * <pre>
     * application/json, application/xml
     * </pre>
     */
    protected String acceptPost;
    
    /**
     * Optional: set this property to indicate the Etag of the linked 
     * object, typically the MD5 digest of the linked object. This allows
     * the client to determine if it already has a current copy of the
     * linked object and avoid an unnecessary download. Etags are 
     * typically used only with GET requests. An Etag should NOT be 
     * provided for any secret objects.
     * 
     */
    protected String etag;

    public Link() {
    }

    public Link(String rel, String href, String acceptPost, String etag) {
        this.rel = rel;
        this.href = href;
        this.acceptPost = acceptPost;
        this.etag = etag;
    }
    
    public Link(String rel, String href) {
        this.rel = rel;
        this.href = href;
    }

    public String getRel() {
        return rel;
    }

    public String getHref() {
        return href;
    }

    public String getAcceptPost() {
        return acceptPost;
    }

    public String getEtag() {
        return etag;
    }

    public String getType() {
        return type;
    }
    
    /**
     * Example:
     * <pre>
     * ArrayList<Link> links = new ArrayList<>();
     * links.add(Link.build().href("http://example.com").rel("author"));
     * </pre>
     * 
     * @return 
     */
    public static LinkBuilder build() { return new LinkBuilder(); }

    public static class LinkBuilder extends Link {
        public LinkBuilder rel(String rel) { this.rel = rel; return this; }
        public LinkBuilder href(String href) { this.href = href; return this; }
        public LinkBuilder type(String type) { this.type = type; return this; }
        public LinkBuilder acceptPost(String acceptPost) { this.acceptPost = acceptPost; return this; }
        public LinkBuilder etag(String etag) { this.etag = etag; return this; }
    }
}
