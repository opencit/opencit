/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jmod.cl;

//import com.intel.mtwilson.jmod.Module;
import java.io.File;
import java.io.IOException;
import java.util.jar.Manifest;

/**
 *
 * @author jbuhacoff
 */
public interface ClassLoadingStrategy {
    ClassLoader getClassLoader(File jar, Manifest manifest, FileResolver resolver) throws IOException;
}
