/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.configuration.jaxrs;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.mtwilson.jaxrs2.Document;
import java.util.ArrayList;

/**
 *
 * @author jbuhacoff
 */
public class ConfigurationDocument extends Document {
    
    @JsonDeserialize(as=ArrayList.class, contentAs=Setting.class)
    private ArrayList<Setting> settings;

    public ArrayList<Setting> getSettings() {
        return settings;
    }

    public void setSettings(ArrayList<Setting> settings) {
        this.settings = settings;
    }
    
    /**
     * Copies settings from provided configuration to this instance.
     * Does NOT clear existing settings, and does NOT check for duplicates.
     * 
     * @param source 
     */
    public void copyFrom(Configuration source) {
        if( settings == null ) { settings = new ArrayList<>(); }
        for (String key : source.keys()) {
            String value = source.get(key);
            this.settings.add(new Setting(key, value));
        }
    }
    
    /**
     * Copies settings from this instance to provided configuration.
     * Does NOT clear existing settings, but if any existing settings have
     * the same name they will be replaced.
     * If a setting appears multiple times, the last one will take effect.
     * @param target 
     */
    public void copyTo(Configuration target) {
        if( settings == null ) { return; }
        for(Setting setting : settings) {
            target.set(setting.getName(), setting.getValue());
        }
    }
    
}
