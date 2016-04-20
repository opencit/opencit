/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.launcher;

import com.intel.dcsg.cpg.performance.AlarmClock;
import com.intel.dcsg.cpg.module.Container;
import com.intel.dcsg.cpg.module.Module;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

/**
 * The Maven pom.xm for this module specifies a few modules to copy into the "target" folder during the build; we try to
 * load classes from those modules in order to make the test repeatable on different developer machines.
 *
 * @author jbuhacoff
 */
public class LauncherTest {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LauncherTest.class);

    /**
     * see also Launcher for code same as the first part of this test function
     * 
     * See mtwilson-launcher Main class for same thing for production.
     * 
     * @throws Exception 
     */
    @Test
    public void testActivateModulesInDirectory() throws Exception {
        ModuleDirectoryLauncher launcher = new ModuleDirectoryLauncher();
        launcher.launch();

        Container container = launcher.getContainer();

        // now list the registered modules
        log.debug("There are {} registered modules", container.getModules().size());
        for (Module module : container.getModules()) {
            log.debug("Module: {};active={}", module.getImplementationTitle() + "-" + module.getImplementationVersion(), (module.isActive() ? "yes" : "no"));
        }
        
//        launcher.startEventLoop();

        // now deactivate them
        log.debug("Deactivating modules");
        for (Module module : container.getModules()) {
            if (module.isActive()) {
                log.debug("Deactivating module: {}", module.getImplementationTitle() + "-" + module.getImplementationVersion());
                container.deactivate(module);
            } else {
                log.debug("Module {} was not active", module.getImplementationTitle() + "-" + module.getImplementationVersion());
            }
        }


    }
    
    @Test
    public void testEventLoop() {
        ModuleDirectoryLauncher launcher = new ModuleDirectoryLauncher();
        
        LaunchThread r = new LaunchThread(launcher);
        
        log.debug("Starting...");
        Thread thread = new Thread(r);
        thread.start();
        
        log.debug("Waiting 5 seconds...");
        AlarmClock clock = new AlarmClock(5,TimeUnit.SECONDS);
        clock.sleep();
        log.debug("Stopping...");
        launcher.stopEventLoop();
        log.debug("Done");
    }

    public static class LaunchThread implements Runnable {
        private ModuleDirectoryLauncher launcher;
        public LaunchThread(ModuleDirectoryLauncher launcher) {
            this.launcher = launcher;
        }
        public void run() {
            launcher.startEventLoop();
        }
    }

}
