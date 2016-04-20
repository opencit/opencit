/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.authc.host;

/**
 *
 * @author jbuhacoff
 */
public interface HostFilter {
    boolean accept(String address);    
}
