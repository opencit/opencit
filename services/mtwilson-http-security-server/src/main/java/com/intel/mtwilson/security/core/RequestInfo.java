/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.security.core;

import java.util.Date;

/**
 * @since 1.2
 * @author jbuhacoff
 */
public class RequestInfo {
    public String instance;
    public Date received;
    public String source;
    public String content;
    public byte[] md5Hash;
}
