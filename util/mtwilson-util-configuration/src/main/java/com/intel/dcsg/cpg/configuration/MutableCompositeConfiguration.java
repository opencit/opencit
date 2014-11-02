/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.configuration;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Extends the CompositeConfiguration by declaring a single MutableConfiguration
 * called "target" that will store all changes.
 * 
 * Tentative - the target itself is also automatically the first source in the list so
 * if you write a setting to target that will be the one that is always
 * returned. This may be changed later to not happen automatically and leave it
 * up to the caller to insert the mutable configuration target into any position
 * in the sources or not to use the target as a source at all.
 * 
 * @author jbuhacoff
 */
public class MutableCompositeConfiguration extends CompositeConfiguration implements MutableConfiguration {
    protected MutableConfiguration target;
    
    public MutableCompositeConfiguration(MutableConfiguration target, List<Configuration> sources) {
        this.sources = new ArrayList<Configuration>();
        this.sources.add(target);
        this.sources.addAll(sources);
        this.target = target;
    }
    public MutableCompositeConfiguration(MutableConfiguration target, Configuration... sources) {
        this.sources = new ArrayList<Configuration>();
        this.sources.add(target);
        this.sources.addAll(Arrays.asList(sources));
        this.target = target;
    }

    @Override
    public void setBoolean(String key, Boolean newValue) {
        target.setBoolean(key, newValue);
    }

    @Override
    public void setByte(String key, Byte newValue) {
        target.setByte(key, newValue);
    }

    @Override
    public void setShort(String key, Short newValue) {
        target.setShort(key, newValue);
    }

    @Override
    public void setInteger(String key, Integer newValue) {
        target.setInteger(key, newValue);
    }

    @Override
    public void setLong(String key, Long newValue) {
        target.setLong(key, newValue);
    }

    @Override
    public void setBigInteger(String key, BigInteger newValue) {
        target.setBigInteger(key, newValue);
    }

    @Override
    public void setFloat(String key, Float newValue) {
        target.setFloat(key, newValue);
    }

    @Override
    public void setDouble(String key, Double newValue) {
        target.setDouble(key, newValue);
    }

    @Override
    public void setBigDecimal(String key, BigDecimal newValue) {
        target.setBigDecimal(key, newValue);
    }

    @Override
    public void setString(String key, String newValue) {
        target.setString(key, newValue);
    }

    @Override
    public void setObject(String key, Object newValue) {
        target.setObject(key, newValue);
    }
    
}
