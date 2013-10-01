/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.api;

import javax.ws.rs.core.MediaType;

/**
 *
 * @since 0.5.2
 * @author jbuhacoff
 */
public class ApiRequest {
    public MediaType contentType;
    public String content;
    
    public ApiRequest(MediaType contentType, String content) {
        this.contentType = contentType;
        this.content = content;
    }
}
