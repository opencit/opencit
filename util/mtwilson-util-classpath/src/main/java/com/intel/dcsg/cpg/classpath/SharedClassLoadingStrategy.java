/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.classpath;

import java.io.File;
import java.util.jar.Manifest;

/**
 * Shared (Semantic versioning). Modules assume that their library dependencies may be interchangeable with newer
 * versions up to a limit. When a module declares library X-1.2 on its class path it may actually be linked with the
 * highest version of library X up to but not including X-2.0, for example X-1.2.1 or X-1.4. This allows the class
 * loader to detect when modules depend on “semantically near” versions of the same library and select the highest
 * version for sharing. This reduces memory usage. Any modules that depend on a specific version of a library and cannot
 * use a semantically near but higher version can declare this explicitly in its MANIFEST.MF file to force the class
 * loader to provide that specific version of the library in the module’s class path. Dependencies of that non-shared
 * library will automatically still be shared unless that library also declares explicit dependency versions. The
 * container must track which modules use each dependency because when the dependencies are shared, all using modules
 * must be unloaded in order for the dependency to be hot-upgraded. Uses one JarFileClassLoader per jar file and one
 * DelegatingClassLoader per module which is configured with that module’s calculated dependencies (semantic versioning
 * with module-specific overrides) so it delegates class loading to existing JarFileClassLoader instances for the
 * appropriate jar files.
 *
 * @author jbuhacoff
 */
public class SharedClassLoadingStrategy {
    // XXX TODO the strategy needs to be initialized with the list of all modules and all libraries so it can
    // figure out what is the set that will be shared for each one, and THEN caller does getClassLoader(module) to get
    // the results. 
    
    public ClassLoader getClassLoader(File jar, Manifest manifest) {
        return null;
    }
}
