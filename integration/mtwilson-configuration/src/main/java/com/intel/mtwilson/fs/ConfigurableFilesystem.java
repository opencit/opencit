/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.fs;

import com.intel.dcsg.cpg.io.AllCapsEnvironmentConfiguration;
import com.intel.dcsg.cpg.io.Platform;
import com.intel.dcsg.cpg.validation.ValidationUtil;
import java.io.File;
import java.util.HashMap;
import java.util.Properties;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.commons.configuration.EnvironmentConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;

/**
 *
 * @author jbuhacoff
 */
public class ConfigurableFilesystem extends AbstractFilesystem {
    public final String FILESYSTEM_NAME = "fs.name"; //  to change filesystem root from /opt/mtwilson to /opt/trustagent, run java -Dfs.name=trustagent   or export FS_NAME=trustagent before running java
    public final String FILESYSTEM_ROOT_PATH = "fs.root"; //  to change filesystem root from /opt/mtwilson to /usr/share/mtwilson, run java -Dfs.root=/usr/share 
    public final String FILESYSTEM_APPLICATION_PATH = "fs.home"; //  to change filesystem root from /opt/mtwilson to /opt/trustagent, run java -Dfs.root=/opt/trustagent 
    public final String FILESYSTEM_CONFIGURATION_PATH = "fs.conf"; //  to change configuration folder from /opt/mtwilson/configuration to /opt/trustagent/configuration, just set -Dfs.name (above); but to set it to /etc/mtwilson  run java -Dfs.conf=/etc/mtwilson  or to /etc/trustagent  -Dfs.conf=/etc/trustagent  
    public final String FILESYSTEM_ENVIRONMENT_PATH = "fs.env"; //  to change configuration folder from /opt/mtwilson/env.d to /etc/sysconfig/mtwilson, just set -Dfs.env=/etc/sysconfig/mtwilson 

    private Configuration configuration;
    private PlatformFilesystem platformFilesystem;
    
    public ConfigurableFilesystem() { 
        this(new MapConfiguration(new Properties()));
    }
    /**
     * First priority is the configuration that is passed in.
     * Second is the java system properties specified on command line like -Dfs.name
     * Third is exported environment variables like FS_NAME
     * 
     * @param configuration 
     */
    public ConfigurableFilesystem(Configuration configuration) {
        CompositeConfiguration composite = new CompositeConfiguration();
        composite.addConfiguration(configuration);
        SystemConfiguration system = new SystemConfiguration();
        composite.addConfiguration(system);
        EnvironmentConfiguration env = new EnvironmentConfiguration();
        composite.addConfiguration(env);
        AllCapsEnvironmentConfiguration envAllCaps = new AllCapsEnvironmentConfiguration();
        composite.addConfiguration(envAllCaps);
        this.configuration = composite;
    }

    @Override
    protected String getApplicationName() {
        return configuration.getString(FILESYSTEM_NAME, "mtwilson");
    }

    @Override
    protected PlatformFilesystem getPlatformFilesystem() {
        if( platformFilesystem == null ) {
            String root = configuration.getString(FILESYSTEM_ROOT_PATH);
            if( root != null ) {
                platformFilesystem = new RelativeFilesystem(root);
            }
            else {
                platformFilesystem = super.getPlatformFilesystem();
            }
        }
        return platformFilesystem;
    }
    
    
    
    @Override
    public String getApplicationPath() {
        return configuration.getString(FILESYSTEM_APPLICATION_PATH, super.getApplicationPath());
    }
    
    @Override
    public String getConfigurationPath() {
         return configuration.getString(FILESYSTEM_CONFIGURATION_PATH, super.getConfigurationPath());
    }

    @Override
    public String getEnvironmentExtPath() {
        return configuration.getString(FILESYSTEM_ENVIRONMENT_PATH, super.getEnvironmentExtPath());
    }

    @Override
    public FeatureFilesystem getBootstrapFilesystem() {
//        return new BasicFeatureFilesystem(getApplicationPath());
        HashMap<String,Object> map = new HashMap<>();
        map.put("fs.feature.root", getApplicationPath());
        map.put("fs.feature.java", configuration.getString("fs.java", getApplicationPath() + File.separator + "java"));
        map.put("fs.feature.hypertext", configuration.getString("fs.hypertext", getApplicationPath() + File.separator + "hypertext"));
        map.put("fs.feature.license_d", configuration.getString("fs.license_d", getApplicationPath() + File.separator + "license.d"));
        map.put("fs.feature.sql", configuration.getString("fs.sql", getApplicationPath() + File.separator + "sql"));
        map.put("fs.feature.bin", configuration.getString("fs.bin", getApplicationPath() + File.separator + "bin"));
        map.put("fs.feature.var", configuration.getString("fs.var", getApplicationPath() + File.separator + "var"));
        ConfigurableFeatureFilesystem featureFilesystem = new ConfigurableFeatureFilesystem(new MapConfiguration(map));
        return featureFilesystem;
    }
    
    @Override
    public FeatureFilesystem getFeatureFilesystem(String featureId) {
        if( !ValidationUtil.isValidWithRegex(featureId, FilesystemUtil.FEATURE_ID_REGEX) ) { throw new IllegalArgumentException("Invalid feature id"); } // must start with a letter, then it can have letters, digits, underscores, dots, and hyphens, but not two dots in a row, and must end with a letter or digit
        return new BasicFeatureFilesystem( getApplicationPath() + File.separator + "features" + File.separator + featureId );
    }
    
}
