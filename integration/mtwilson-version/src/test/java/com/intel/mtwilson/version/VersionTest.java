/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.version;

import com.intel.mtwilson.Version;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class VersionTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(VersionTest.class);

    @Test
    public void testVersion() {
        log.debug("Version: {}", Version.getInstance().getVersion());
        log.debug("Build: {}", Version.getInstance().getTimestamp());
        log.debug("Branch: {}", Version.getInstance().getBranch());
    }
}
