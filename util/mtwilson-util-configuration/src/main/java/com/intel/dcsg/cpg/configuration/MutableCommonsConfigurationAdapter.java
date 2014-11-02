/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.configuration;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * The commons-configuration dependency is optional so if you use this class you need to include commons-configuration
 * in your classpath or declare it as a dependency.
 * 
 * @author jbuhacoff
 */
public class MutableCommonsConfigurationAdapter extends CommonsConfigurationAdapter implements MutableConfiguration {
    public MutableCommonsConfigurationAdapter(org.apache.commons.configuration.Configuration cc) {
        super(cc);
    }
    
    @Override
    public void setBoolean(String key, Boolean newValue) {
        cc.setProperty(key, newValue);
    }

    @Override
    public void setByte(String key, Byte newValue) {
        cc.setProperty(key, newValue);
    }

    @Override
    public void setShort(String key, Short newValue) {
        cc.setProperty(key, newValue);
    }

    @Override
    public void setInteger(String key, Integer newValue) {
        cc.setProperty(key, newValue);
    }

    @Override
    public void setLong(String key, Long newValue) {
        cc.setProperty(key, newValue);
    }

    @Override
    public void setBigInteger(String key, BigInteger newValue) {
        cc.setProperty(key, newValue);
    }

    @Override
    public void setFloat(String key, Float newValue) {
        cc.setProperty(key, newValue);
    }

    @Override
    public void setDouble(String key, Double newValue) {
        cc.setProperty(key, newValue);
    }

    @Override
    public void setBigDecimal(String key, BigDecimal newValue) {
        cc.setProperty(key, newValue);
    }

    @Override
    public void setString(String key, String newValue) {
        cc.setProperty(key, newValue);
    }

    @Override
    public void setObject(String key, Object newValue) {
        if( newValue == null ) { cc.clearProperty(key); return; }
        cc.setProperty(key, objectCodec.encode(newValue));
    }
    

}
