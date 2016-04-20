/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.fs;

import com.intel.dcsg.cpg.io.AllCapsEnvironmentConfiguration;
import com.intel.dcsg.cpg.io.Platform;
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
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractFilesystem.class);

    private String applicationPath = null;
    private String configurationPath = null;
    private String environmentExtPath = null;
    private BasicFeatureFilesystem bootstrapFilesystem = null;
    private PlatformFilesystem platformFilesystem = null;
    
    /**
     * Used to construct application paths. For example the application
     * home directory is {applicationRoot}/{applicationName} which might
     * be /opt/mtwilson on linux and C:\mtwilson on windows. 
     * @return "mtwilson" or "trustagent" for example
     */
    protected abstract String getApplicationName();
    
    protected PlatformFilesystem getPlatformFilesystem() {
        if( platformFilesystem == null ) {
            if (Platform.isUnix()) {
                platformFilesystem = new UnixFilesystem();
            }
            else if (Platform.isWindows()) {
                platformFilesystem = new WindowsFilesystem();
            }
            else {
                platformFilesystem = new RelativeFilesystem();
            }
        }
        return platformFilesystem;
    }

    /**
     * 
     * @return the application home directory, for example /opt/mtwilson
     */
    @Override
    public String getApplicationPath() {
        if (applicationPath == null) {
            applicationPath = getPlatformFilesystem().getApplicationRoot() + File.separator + getApplicationName();
        }
        return applicationPath;
    }

    /**
     * 
     * @return the application home directory, for example /opt/mtwilson/configuration or /etc/mtwilson
     */
    @Override
    public String getConfigurationPath() {
        if (configurationPath == null) {
            configurationPath = getApplicationPath() + File.separator + "configuration";
        }
        return configurationPath;
    }

    /**
     * 
     * @return the application environment settings directory, for example /opt/mtwilson/env.d
     */
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
