/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package test.jpa;

import com.intel.mtwilson.My;
import com.intel.mtwilson.as.data.MwProcessorMapping;
import java.io.IOException;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class MwProcessorMappingTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MwProcessorMappingTest.class);

    @Test
    public void testFindMwProcessorMapping() throws IOException {
        MwProcessorMapping item = My.jpa().mwProcessorMapping().findByCPUID("D7 06 02");
        log.debug("found platform name {} cpuid {} processor type {}", item.getPlatformName(), item.getProcessorCpuid(), item.getProcessorType());
    }
}
