/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.fs;

import com.intel.dcsg.cpg.io.AllCapsEnvironmentConfiguration;
import com.intel.dcsg.cpg.validation.ValidationUtil;
import java.io.File;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.EnvironmentConfiguration;
import org.apache.commons.configuration.SystemConfiguration;

/**
 *
 * @author jbuhacoff
 */
public abstract class AbstractFilesystem implements ApplicationFilesystem {

    private CompositeConfiguration configuration;
    private String applicationPath = null;
    private String configurationPath = null;
    private String environmentExtPath = null;
    private BasicFeatureFilesystem bootstrapFilesystem = null;

    /**
     * When a variable is needed like mtwilson.home, first the JVM properties
     * are checked, then the environment variables, then the environment
     * variables with an all caps translation of the name, for example
     * MTWILSON_HOME
     */
    public AbstractFilesystem() {
        CompositeConfiguration composite = new CompositeConfiguration();
        SystemConfiguration system = new SystemConfiguration();
        composite.addConfiguration(system);
        EnvironmentConfiguration env = new EnvironmentConfiguration();
        composite.addConfiguration(env);
        AllCapsEnvironmentConfiguration envAllCaps = new AllCapsEnvironmentConfiguration();
        composite.addConfiguration(envAllCaps);
        configuration = composite;
    }

    protected abstract String getDefaultConfigurationPath();

    protected abstract String getDefaultApplicationPath();

    // MTWILSON_HOME
    @Override
    public String getApplicationPath() {
        if (applicationPath == null) {
            applicationPath = configuration.getString("mtwilson.home", getDefaultApplicationPath());
        }
        return applicationPath;
    }

    // MTWILSON_CONF
    @Override
    public String getConfigurationPath() {
        if (configurationPath == null) {
            configurationPath = configuration.getString("mtwilson.conf", getDefaultConfigurationPath());
        }
        return configurationPath;
    }

    @Override
    public String getEnvironmentExtPath() {
        if (environmentExtPath == null) {
            environmentExtPath = getApplicationPath() + File.separator + "env.d";
        }
        return environmentExtPath;
    }
/*
    @Override
    public String getBinPath() {
        if (binPath == null) {
            binPath = getApplicationPath() + File.separator + "bin";
        }
        return binPath;
    }

    @Override
    public String getJavaPath() {
        if (javaPath == null) {
            javaPath = getApplicationPath() + File.separator + "java";
        }
        return javaPath;
    }
*/
/*
    @Override
    public String getJavaExtPath() {
        if (javaExtPath == null) {
            javaExtPath = getApplicationPath() + File.separator + "java.d";
        }
        return javaExtPath;
    }
    @Override
    public String getUtilPath() {
        if (utilPath == null) {
            utilPath = getApplicationPath() + File.separator + "util.d";
        }
        return utilPath;
    }*/
/*
    @Override
    public String getLicensePath() {
        if (licensePath == null) {
            licensePath = getApplicationPath() + File.separator + "license.d";
        }
        return licensePath;
    }
    */
    
    @Override
    public FeatureFilesystem getBootstrapFilesystem() {
        if( bootstrapFilesystem == null ) {
            bootstrapFilesystem = new BasicFeatureFilesystem(getApplicationPath());
        }
        return bootstrapFilesystem;
    }
    
    @Override
    public FeatureFilesystem getFeatureFilesystem(String featureId) {
        if( !ValidationUtil.isValidWithRegex(featureId, FilesystemUtil.FEATURE_ID_REGEX) ) { throw new IllegalArgumentException("Invalid feature id"); } // must start with a letter, then it can have letters, digits, underscores, dots, and hyphens, but not two dots in a row, and must end with a letter or digit
        return new BasicFeatureFilesystem( getApplicationPath() + File.separator + "features" + File.separator + featureId );
    }
}
