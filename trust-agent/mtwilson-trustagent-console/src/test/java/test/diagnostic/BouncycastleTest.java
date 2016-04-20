/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package test.diagnostic;

import com.intel.mountwilson.trustagent.Diagnostic;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class BouncycastleTest {
    @Test
    public void testBouncycastlePresent() {
        Diagnostic.main(null);
    }
}
