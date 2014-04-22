/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.fs;

//import java.io.File;
import java.util.Properties;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;

/**
 *
 * @author jbuhacoff
 */
public class ConfigurableFeatureFilesystem extends BasicFeatureFilesystem implements FeatureFilesystem {
    private Configuration config = null;
    
    public ConfigurableFeatureFilesystem() {
        this(new MapConfiguration(new Properties()));
    }
    public ConfigurableFeatureFilesystem(Properties properties) {
        this(new MapConfiguration(properties));
    }
    public ConfigurableFeatureFilesystem(Configuration config) {
        this.config = config;
        super.setRootPath(config.getString("fs.feature.root"));
    }
    
    /*
    @Override
    public void setRootPath(String root) {
        super.setRootPath(root);
    }
    @Override
    public String getRootPath() { return config.getString("fs.feature.root", super.getRootPath()); }
    * */
    
    @Override
    public String getBinPath() {
        return config.getString("fs.feature.bin", super.getBinPath()); // root + File.separator + "bin"
    }
    @Override
    public String getHypertextPath() {
        return config.getString("fs.feature.hypertext", super.getHypertextPath()); // root + File.separator + "hypertext"
    }
    @Override
    public String getJavaPath() {
        return config.getString("fs.feature.java", super.getJavaPath()); // root + File.separator + "java"
    }
    @Override
    public String getLicensePath() {
        return config.getString("fs.feature.license_d", super.getLicensePath()); // root + File.separator + "license.d"
    }
    @Override
    public String getLinuxUtilPath() {
        return config.getString("fs.feature.linux_util", super.getLinuxUtilPath()); // root + File.separator + "linux-util"
    }
    @Override
    public String getSqlPath() {
        return config.getString("fs.feature.sql", super.getSqlPath()); // root + File.separator + "sql"
    }
    
    @Override
    public String getVarPath() {
        return config.getString("fs.feature.var", super.getVarPath()); // root + File.separator + "var"
    }

}
