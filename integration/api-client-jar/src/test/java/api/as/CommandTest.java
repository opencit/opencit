/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package api.as;

import org.junit.Test;
import com.intel.mtwilson.ApiCommand;

/**
 *
 * @author jbuhacoff
 */
public class CommandTest {
//    @Test
    public void testGetLocation() throws Exception {
        ApiCommand.main(new String[] { "GetHostLocation", "10.1.71.103" });
    }
}
