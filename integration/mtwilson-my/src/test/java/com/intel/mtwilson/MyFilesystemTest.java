/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson;

import com.intel.dcsg.cpg.validation.ValidationUtil;
import com.intel.mtwilson.fs.ApplicationFilesystem;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class MyFilesystemTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MyFilesystemTest.class);
    
    /**
when MTWILSON_CONF is set to C:\Intel\MtWilson\conf:
2014-02-07 22:42:54,995 DEBUG [main] c.i.m.MyFilesystem [MyFilesystem.java:78] value from commons config: C:\Intel\MtWilson\conf
2014-02-07 22:42:55,007 DEBUG [main] c.i.m.MyFilesystem [MyFilesystem.java:62] getenv(allcaps) MTWILSON_CONF = C:\Intel\MtWilson\conf
2014-02-07 22:42:55,008 DEBUG [main] c.i.m.MyFilesystem [MyFilesystem.java:79] value from testegetenv: C:\Intel\MtWilson\conf
2014-02-07 22:42:55,008 DEBUG [main] c.i.m.MyFilesystemTest [MyFilesystemTest.java:20] conf path: C:\Intel\MtWilson\conf

when MTWILSON_CONF and MTWILSON_HOME is not set:
2014-02-07 22:51:43,215 DEBUG [main] c.i.m.MyFilesystem [MyFilesystem.java:78] value from commons config: C:\mtwilson\conf
2014-02-07 22:51:43,220 DEBUG [main] c.i.m.MyFilesystem [MyFilesystem.java:62] getenv(allcaps) MTWILSON_CONF = null
2014-02-07 22:51:43,220 DEBUG [main] c.i.m.MyFilesystem [MyFilesystem.java:79] value from testegetenv: null
2014-02-07 22:51:43,220 DEBUG [main] c.i.m.MyFilesystemTest [MyFilesystemTest.java:28] conf path: C:\mtwilson\conf

* 
     */
    @Test
    public void testDefaultPaths() {
        ApplicationFilesystem fs = MyFilesystem.getApplicationFilesystem();
        log.debug("app fs class: {}", fs.getClass().getName());
        log.debug("conf path: {}", fs.getConfigurationPath());
        log.debug("bin path: {}", fs.getBootstrapFilesystem().getBinPath());
        log.debug("var path: {}", fs.getBootstrapFilesystem().getVarPath());
    }
    
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
