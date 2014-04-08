/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.tasks;

import com.intel.dcsg.cpg.validation.Fault;
import java.util.List;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class PasswordsTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PasswordsTest.class);
    
    @Test
    public void testBenchmark() throws Exception {
        MigrateUsers task = new MigrateUsers();
        task.configure();
    }
    @Test
    public void testFindTables() throws Exception {
        MigrateUsers task = new MigrateUsers();
        task.setIterations(1);
        task.configure();
        List<Fault> faults = task.getConfigurationFaults();
        for(Fault fault : faults) {
            log.error("Configuration fault: {}", fault);
        }
    }
}
