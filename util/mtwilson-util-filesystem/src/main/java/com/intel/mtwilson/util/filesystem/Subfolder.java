/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.filesystem;

import java.io.File;

/**
 * Represents a folder that can be configured via a system property like
 * mtwilson.fs./name/ or an environment variable like MTWILSON_FS_/name/
 * 
 * A default location for the folder is /name/ under the parent folder.
 * If the parent folder is not defined, /name/ is returned as the default
 * which would typically be resolved in the current directory.
 * 
 * This class intentionally does not check the application configuration because
 * it should be usable before the configuration has been loaded. Any process
 * that reads the configuration can just set the appropriate system properties
 * so they can be picked up by instances of this class.
 * 
 * This class should only be used for configurable top-level folders of
 * the application - it should NOT be used for subfolders within a feature
 * or other nested subfolder because, for example, 
 * new Subfolder("hypertext", parentFeature) would check mtwilson.fs.hypertext
 * and if that happens to be set to a top-level application hypertext folder
 * then all features that use the example constructor would end up pointing
 * to some global hypertext folder instead of their own.
 * 
 * In summary, the reason to use this class instead of directly writing
 * {@code root+File.separator+"subfolder"} is that using this class allows
 * the user to change the location of subfolder by setting a system property
 * like mtwilson.fs.subfolder or an environment variable MTWILSON_FS_SUBFOLDER.
 * 
 * If the subfolder location should not be configurable, then do not use this
 * class. It is not appropriate for all situations.
 * 
 * @author jbuhacoff
 */
public class Subfolder extends ApplicationFolder {
    private final String app;
    private final String property;
    private final String environment;
    private String name;
    private Folder parent = null;
    
    /**
     * 
     * @param name like "configuration", "repository", "tmp", "var", etc.
     */
    public Subfolder(String name) {
        this(name,null);
    }
    
    public Subfolder(String name, Folder parent) {
        super();
        app = getApplicationName();
        property = app.toLowerCase()+".fs." + name.toLowerCase(); // like "mtwilson.fs.configuration"
        environment = app.toUpperCase()+"_FS_" + name.toUpperCase(); // like "MTWILSON_FS_CONFIGURATION"
        this.name = name;
        this.parent = parent;
    }
    
    @Override
    public String getPropertyName() {
        return property; // like mtwilson.fs.configuration
    }
    
    @Override
    public String getEnvironmentName() {
        return environment; // like MTWILSON_FS_CONFIGURATION
    }
    
    @Override
    public String getDefaultPath() {
        if( parent == null ) {
            return name;
        }
        return parent.getPath() + File.separator + name;
    }
    
}
