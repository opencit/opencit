/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jmod.cl;

import com.intel.dcsg.cpg.util.Filter;
import com.intel.mtwilson.jmod.Module;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.jar.Manifest;

/**
 * One class loader per module loads its internal implementation and all its dependencies privately. A parent class
 * loader loads all classes that are not in modules such as java.* classes as well as designated public packages.
 * Modules designate internal implementation classes by storing them under a package name such as *.impl.* or
 * *.internal.*. This allows modules to define complex data types with their API which other modules can use. The
 * complex data types can only use what they provide in the public API and java.* classes. The public complex data types
 * may not refer to any classes defined in the internal implementation packages. This is easy to construct by using
 * public interfaces and abstract classes that are implemented by private classes or subclasses. Uses one
 * MultiJarFileClassLoader per module for its dependencies and a ModulePrivateClassLoader for the module jar itself
 * which loads only the internal implementation classes leaving the public classes for a parent class loader; and uses
 * one MultiJarFileClassLoader for the entire container which is a parent class loader that loads only module public
 * APIs from module jars (and does not reference any dependency jars, in order to enforce that modules use only java.*
 * classes or its own provided public classes in its public APIs).
 * 
 * The fencing works like this:
 * By default, all classes except those in the java.*  package are loaded privately
 * If you specify an exclude filter, then all classes except those in java.* and those "accepted" by the exclude filter
 * will be loaded privately.  Excluding means to exclude from the fenced private area.
 * If you specify an include filter, then all classes "accepted" by the filter (except those in java.*) will be
 * loaded privately. Including means to include in the fenced private area.
 * If you specify both an include filter and an exclude filter, then all classes "accepted" by the include filter
 * except those in java.* and those "accepted" by the exclude filter will be loaded privately. So it's a combination
 * of both, and excluding takes precedence (if a class matches criteria for both inclusion and exclusion, it will
 * be excluded).
 *
 * @author jbuhacoff
 */
public class FencedClassLoadingStrategy implements ClassLoadingStrategy{
    private MultiJarFileClassLoader modulePublicApis = new MultiJarFileClassLoader();
    
    public FencedClassLoadingStrategy() {
        StringWildcardFilter exclude = new StringWildcardFilter();
        exclude.add("*.impl.*");
        exclude.add("*.internal.*");
        modulePublicApis.setExcludeFilter(exclude);
    }
    /*
    public void include(String packageNameSpec) {
        include.add(packageNameSpec);
    }
    
    public void exclude(String packageNameSpec) {
        exclude.add(packageNameSpec);
    }*/
    
    
    // XXX  should we ALSO support the other way, wherein a jar has everything implicitly private but
    // it exposes an api so anything in *.api.* is delegated to parent and everything else is private. ?
    // if we do,  we should let each module define how it wants to be fenced off using a header in MANIFEST.MF
    // which we can read here and then decide how to create that module's class loaders.
    @Override
    public ClassLoader getClassLoader(File jar, Manifest manifest, FileResolver resolver) throws IOException {
        // the parent gets only the module jar files and only exposes the non-fenced off areas
        modulePublicApis.add(jar);
        // the dependencies are loaded privately 
        MultiJarFileClassLoader privateDependencies = new MultiJarFileClassLoader(modulePublicApis);
        privateDependencies.add(resolver.resolveClasspath(manifest));
        // the module's first class loader consists of a single jar class loader that loads ONLY the fenced off
        // areas,  it defers anything else to the dependencies and after that to the parent with the module's
        // public APIs that everything can access
        JarFileClassLoader fenced = new JarFileClassLoader(jar, privateDependencies);
        StringWildcardFilter include = new StringWildcardFilter();
        include.add("*.impl.*");
        include.add("*.internal.*");
        fenced.setIncludeFilter(include);
        return fenced;
    }
    

}
