/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.fs;

import com.intel.dcsg.cpg.io.Platform;
import com.intel.dcsg.cpg.validation.ValidationUtil;
import java.io.File;
import java.util.HashMap;
import java.util.Properties;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;

/**
 *
 * @author jbuhacoff
 */
public class ConfigurableFilesystem implements ApplicationFilesystem {

    private ApplicationFilesystem defaultFilesystem;
    private Configuration configuration;
    public ConfigurableFilesystem() { 
        this(new MapConfiguration(new Properties()));
    }
    public ConfigurableFilesystem(Configuration configuration) {
        this.configuration = configuration;
        this.defaultFilesystem = pickDefaultFilesystem();
    }
    
    private ApplicationFilesystem pickDefaultFilesystem() {
        if (Platform.isUnix()) {
            return new UnixFilesystem();
        }
        if (Platform.isWindows()) {
            return new WindowsFilesystem();
        }
        return new RelativeFilesystem();
    }
    
    public ApplicationFilesystem getDefaultFilesystem() {
        return defaultFilesystem;
    }
    public void setDefaultFilesystem(ApplicationFilesystem defaultFilesystem) {
        this.defaultFilesystem = defaultFilesystem;
    }
    
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
    public Configuration getConfiguration( ){ return configuration; }
    
    @Override
    public String getConfigurationPath() {
        return configuration.getString("mtwilson.fs.configuration", defaultFilesystem.getConfigurationPath());
    }

    @Override
    public String getApplicationPath() {
        return configuration.getString("mtwilson.fs.application", defaultFilesystem.getApplicationPath());
    }

    @Override
    public String getEnvironmentExtPath() {
        return configuration.getString("mtwilson.fs.env", defaultFilesystem.getEnvironmentExtPath());
    }

    @Override
    public FeatureFilesystem getBootstrapFilesystem() {
//        return new BasicFeatureFilesystem(getApplicationPath());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("mtwilson.fs.feature.root", getApplicationPath());
        map.put("mtwilson.fs.feature.java", configuration.getString("mtwilson.fs.java"));
        map.put("mtwilson.fs.feature.hypertext", configuration.getString("mtwilson.fs.hypertext"));
        map.put("mtwilson.fs.feature.license_d", configuration.getString("mtwilson.fs.license_d"));
        map.put("mtwilson.fs.feature.sql", configuration.getString("mtwilson.fs.sql"));
        map.put("mtwilson.fs.feature.bin", configuration.getString("mtwilson.fs.bin"));
        map.put("mtwilson.fs.feature.var", configuration.getString("mtwilson.fs.var"));
        ConfigurableFeatureFilesystem featureFilesystem = new ConfigurableFeatureFilesystem(new MapConfiguration(map));
        return featureFilesystem;
    }
    
    @Override
    public FeatureFilesystem getFeatureFilesystem(String featureId) {
        if( !ValidationUtil.isValidWithRegex(featureId, FilesystemUtil.FEATURE_ID_REGEX) ) { throw new IllegalArgumentException("Invalid feature id"); } // must start with a letter, then it can have letters, digits, underscores, dots, and hyphens, but not two dots in a row, and must end with a letter or digit
        return new BasicFeatureFilesystem( getApplicationPath() + File.separator + "features" + File.separator + featureId );
    }
    
}
