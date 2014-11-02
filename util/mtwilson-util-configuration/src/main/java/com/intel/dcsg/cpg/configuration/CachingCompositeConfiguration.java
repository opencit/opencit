/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.configuration;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

/**
 * This class is tentative
 * 
 * A composite configuration that automatically copies non-null values 
 * from the composite sources
 * into the mutable configuration target as they are accessed.
 * 
 * @author jbuhacoff
 */
public class CachingCompositeConfiguration extends MutableCompositeConfiguration {
    public CachingCompositeConfiguration(MutableConfiguration target, List<Configuration> sources) {
        super(target, sources);
    }
    public CachingCompositeConfiguration(MutableConfiguration target, Configuration... sources) {
        super(target, sources);
    }

    @Override
    public Boolean getBoolean(String key, Boolean defaultValue) {
        Boolean value = super.getBoolean(key, defaultValue);
        if( value != null ) {
            target.setBoolean(key, value);
        }
        return value;
    }

    @Override
    public BigDecimal getBigDecimal(String key, BigDecimal defaultValue) {
        BigDecimal value = super.getBigDecimal(key, defaultValue);
        if( value != null ) {
            target.setBigDecimal(key, value);
        }
        return value;
    }

    @Override
    public BigInteger getBigInteger(String key, BigInteger defaultValue) {
        BigInteger value = super.getBigInteger(key, defaultValue);
        if( value != null ) {
            target.setBigInteger(key, value);
        }
        return value;
    }

    @Override
    public Byte getByte(String key, Byte defaultValue) {
        Byte value = super.getByte(key, defaultValue);
        if( value != null ) {
            target.setByte(key, value);
        }
        return value;
    }

    @Override
    public Double getDouble(String key, Double defaultValue) {
        Double value = super.getDouble(key, defaultValue);
        if( value != null ) {
            target.setDouble(key, value);
        }
        return value;
    }

    @Override
    public Float getFloat(String key, Float defaultValue) {
        Float value = super.getFloat(key, defaultValue);
        if( value != null ) {
            target.setFloat(key, value);
        }
        return value;
    }

    @Override
    public Integer getInteger(String key, Integer defaultValue) {
        Integer value = super.getInteger(key, defaultValue);
        if( value != null ) {
            target.setInteger(key, value);
        }
        return value;
    }

    @Override
    public Long getLong(String key, Long defaultValue) {
        Long value = super.getLong(key, defaultValue);
        if( value != null ) {
            target.setLong(key, value);
        }
        return value;
    }

    @Override
    public <T> T getObject(Class<T> objectClass, String key, T defaultValue) {
        T value = super.getObject(objectClass, key, defaultValue);
        if( value != null ) {
            target.setObject(key, value);
        }
        return value;
    }

    @Override
    public Short getShort(String key, Short defaultValue) {
        Short value = super.getShort(key, defaultValue);
        if( value != null ) {
            target.setShort(key, value);
        }
        return value;
    }

    @Override
    public String getString(String key, String defaultValue) {
        String value = super.getString(key, defaultValue);
        if( value != null ) {
            target.setString(key, value);
        }
        return value;
    }

    
    
}
