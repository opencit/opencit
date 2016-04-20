/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.myconfig;

import com.intel.mtwilson.My;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Properties;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class ShowMyConfig {
    private Logger log = LoggerFactory.getLogger(getClass());
    
    @Test
    public void testShowMyConfig() throws IOException {
        Properties p = My.configuration().getProperties();
        StringWriter writer = new StringWriter();
        p.store(writer, "Combined configuration");
        log.debug("Configuration: {}", writer.toString());
    }
}
