/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.launcher;

import com.intel.dcsg.cpg.extensions.AnnotationRegistrar;
import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.dcsg.cpg.extensions.ImplementationRegistrar;
import com.intel.dcsg.cpg.extensions.Registrar;
import com.intel.mtwilson.launcher.ext.annotations.Background;
import com.intel.mtwilson.launcher.ws.ext.RPC;
import com.intel.mtwilson.launcher.ws.ext.V1;
import com.intel.mtwilson.launcher.ws.ext.V2;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class ExtensionCacheLauncherTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExtensionCacheLauncherTest.class);

    @Test
    public void testExtensionLauncher() {
       ExtensionCacheLauncher launcher = new ExtensionCacheLauncher();
       launcher.setRegistrars(new Registrar[] { new ImplementationRegistrar(), new AnnotationRegistrar(V2.class),  new AnnotationRegistrar(RPC.class) });
       launcher.run(); // loads and scans the jars
       List<Object> v2s = Extensions.findAllAnnotated(V2.class);
       for(Object v2 : v2s) {
           log.debug("V2 extension {} classloader {}", v2.getClass().getName(), v2.getClass().getClassLoader().getClass().getName());
       }
    }
    
    @Test
    public void testGenerateExtensionCache() {
        // first use the directory launcher to scan and register all available extensions
        ExtensionDirectoryLauncher launcher = new ExtensionDirectoryLauncher();
        launcher.setRegistrars(new Registrar[] { new ImplementationRegistrar(), new AnnotationRegistrar(V2.class), new AnnotationRegistrar(V1.class), new AnnotationRegistrar(RPC.class), new AnnotationRegistrar(Background.class) });
        launcher.run(); // loads and scans the jars
        // second make a list of loaded classes and write it to the extensions.cache file
        Map<String,List<Class<?>>> map = Extensions.getWhiteboard();
        Collection<List<Class<?>>> collection = map.values();
        HashSet<Class<?>> set = new HashSet<>();
        for(List<Class<?>> list : collection) {
            set.addAll(list);
        }
        ArrayList<String> list = new ArrayList<>();
        for(Class<?> clazz : set) {
            log.debug("Caching extension: {}", clazz.getName());
            list.add(clazz.getName());
        }
        String text = StringUtils.join(list, "\n");
        log.debug("text:\n{}", text);
        
    }
}
