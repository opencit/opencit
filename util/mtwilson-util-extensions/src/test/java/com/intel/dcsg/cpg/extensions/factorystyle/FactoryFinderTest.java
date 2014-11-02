/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.extensions.factorystyle;
import com.intel.dcsg.cpg.extensions.FactoryRegistrar;
import com.intel.dcsg.cpg.extensions.ExtensionUtil;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class FactoryFinderTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FactoryFinderTest.class);

    @Test
    public void testEasyScan() {
        ExtensionUtil.scan(new FactoryRegistrar(), AcmeTelephoneFactory.class, VoipFactory.class, LandlineTelephoneFactory.class);
    }
}
