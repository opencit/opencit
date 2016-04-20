/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.as;

import com.intel.mountwilson.as.common.ASConfig;
import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.as.business.HostBO;
import com.intel.mtwilson.as.business.ReportsBO;
import com.intel.mtwilson.as.business.trust.HostTrustBO;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jbuhacoff
 */
public class ASComponentFactory {
    private static Logger log = LoggerFactory.getLogger(ASComponentFactory.class);
    
    public static HostBO getHostBO() {
        HostBO bean = new HostBO();
        return bean;
    }

    public static HostTrustBO getHostTrustBO() {
        HostTrustBO bean = new HostTrustBO();
        HostBO hostBO = getHostBO();
        bean.setHostBO(hostBO);
        return bean;
    }
    
     public static ReportsBO getReportsBO() {
        ReportsBO bean = new ReportsBO();
        return bean;
    }

}
