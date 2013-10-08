/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.jackson;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.net.URL;
import java.util.HashMap;

/**
 *
 * @author jbuhacoff
 */
public class ResponseBase {
    
    @JsonIgnore
    private long id;
    private HashMap<String,URL> links = new HashMap<String,URL>();

    public ResponseBase() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public HashMap<String, URL> getLinks() {
        return links;
    }

    
    public void setLinks(HashMap<String, URL> links) {
        this.links = links;
    }
    
    
}
