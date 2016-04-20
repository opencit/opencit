// NEEDS TO BE INCORPORATED INTO 2.0 AS CPG PACKAGE******************************************************************
/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.diagnostic;

import com.intel.mountwilson.his.helper.ErrorUtil;
import java.io.IOException;
import java.net.UnknownHostException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class ErrorUtilTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ErrorUtilTest.class);

    @Test
    public void testRootCause() {
        try {
            try {
                try {
                    throw new UnknownHostException("localhost");
                }
                catch(IOException e) {
                    throw new IllegalArgumentException("network error", e);
                }
            }
            catch(RuntimeException e) {
                throw new IllegalStateException("error", e);
            }
        }
        catch(Exception e) {
            assertEquals("localhost", ErrorUtil.rootCause(e).getMessage()); // because we set it above in throw new UnknownHostException("localhost");
            assertEquals("localhost", ErrorUtil.findCause(e, UnknownHostException.class).getMessage());
            assertEquals("network error", ErrorUtil.findCause(e, IllegalArgumentException.class).getMessage());
            assertEquals("error", ErrorUtil.findCause(e, IllegalStateException.class).getMessage());            
        }
    }
}
