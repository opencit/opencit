/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.classpath;

import java.io.File;
import java.io.IOException;
import java.util.jar.Manifest;

/**
 * One class loader per module loads all its dependencies privately. 
 * A parent class loader loads all classes that are not in modules such as java.* classes. 
 * Modules can have different versions of dependencies without conflict. 
 * There is a limitation on object sharing - module APIs can rely only on java.* classes to 
 * transfer information to other modules, no custom complex types in public APIs. This limit is easy to 
 * work around by either translating all complex types to Map for APIs or serializing all complex types to 
 * XML or JSON when writing and de-serializing when reading. Uses one MultiJarFileClassLoader per module.
 * 
 * 
 * @author jbuhacoff
 */
public class IsolatedClassLoadingStrategy implements ClassLoadingStrategy {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IsolatedClassLoadingStrategy.class);

    /**
     * Returns a MultiJarFileClassLoader for the module's entire classpath (including the module jar itself)
     * 
     * If you call this multiple times for the same module/manifest you will get a new instance of the classloader
     * each time.
     * 
     * @param module
     * @return
     * @throws IOException 
     */
    @Override
    public ClassLoader getClassLoader(File jar, Manifest manifest, FileResolver resolver) throws IOException {
        MultiJarFileClassLoader cl = new MultiJarFileClassLoader();
        cl.add(jar);
        cl.add(resolver.resolveClasspath(manifest));
        return cl;
    }

    
}
