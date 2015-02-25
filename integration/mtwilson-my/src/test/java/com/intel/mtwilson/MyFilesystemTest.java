/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson;

import com.intel.dcsg.cpg.validation.ValidationUtil;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class MyFilesystemTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MyFilesystemTest.class);
    
    
    @Test
    public void testFeatureNameRegex() {
        String[] names = new String[] { "a", "0", "a0", "a.0", ".a", "a.", "a_b", "a.b", "a..b", "a.b.c", "a.b..c", "a/b", "/a" };
        for(String featureId : names) {
            if( ValidationUtil.isValidWithRegex(featureId, "(?:[a-zA-Z](?:\\.[a-zA-Z0-9]|[_-]+[a-zA-Z0-9]|[a-zA-Z0-9])*)") ) {
                log.debug("accepted '{}'", featureId);
            }
            else {
                log.debug("rejected '{}'", featureId);
            }
        }    
    }
    
    @Test
    public void testLocalConfig() throws Exception {
        log.debug("mtwilson home: {}", My.configuration().getMtWilsonHome());
        log.debug("mtwilson conf: {}", My.configuration().getMtWilsonConf());
        log.debug("mtwilson java: {}", My.configuration().getMtWilsonJava());
    }
}
