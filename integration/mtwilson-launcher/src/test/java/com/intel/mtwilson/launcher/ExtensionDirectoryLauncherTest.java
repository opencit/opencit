/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.launcher;

import com.intel.dcsg.cpg.extensions.AnnotationRegistrar;
import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.dcsg.cpg.extensions.ImplementationRegistrar;
import com.intel.dcsg.cpg.extensions.Registrar;
import com.intel.mtwilson.launcher.ws.ext.V2;
import java.io.File;
import java.util.List;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class ExtensionDirectoryLauncherTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExtensionDirectoryLauncherTest.class);

    @Test
    public void testExtensionLauncher() {
       ExtensionDirectoryLauncher launcher = new ExtensionDirectoryLauncher();
       File[] applicationJars = launcher.getApplicationJars();
       for(int i=0; i<applicationJars.length; i++) {
           log.debug("App Jar: {}", applicationJars[i].getAbsolutePath());
       }
       File[] applicationExtensionJars = launcher.getApplicationExtensionJars();
       for(int i=0; i<applicationExtensionJars.length; i++) {
           log.debug("App Ext Jar: {}", applicationExtensionJars[i].getAbsolutePath());
       }
       launcher.setRegistrars(new Registrar[] { new ImplementationRegistrar(), new AnnotationRegistrar(V2.class) });
       launcher.run(); // loads and scans the jars
       List<Object> v2s = Extensions.findAllAnnotated(V2.class);
       for(Object v2 : v2s) {
           log.debug("V2 extension {} classloader {}", v2.getClass().getName(), v2.getClass().getClassLoader().getClass().getName());
       }
    }
}
