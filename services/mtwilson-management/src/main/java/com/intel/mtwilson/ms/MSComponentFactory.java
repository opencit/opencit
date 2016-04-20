/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.ms;

import com.intel.mtwilson.ms.business.HostBO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class MSComponentFactory {
    private static Logger log = LoggerFactory.getLogger(MSComponentFactory.class);
    
    public static HostBO getHostBO() {
        HostBO bean = new HostBO();
        return bean;
    }

}
