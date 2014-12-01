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
public class CommonsConfigurationAdapter implements Configuration {
    protected org.apache.commons.configuration.Configuration cc;
    protected ObjectPropertyCodec objectCodec;
    
    public CommonsConfigurationAdapter(org.apache.commons.configuration.Configuration cc) {
        if( cc == null ) { throw new NullPointerException(); }
        this.cc = cc;
        this.objectCodec = new XStreamPropertyCodec(); // default; can override with setObjectCodec(...)
    }
    
    public void setObjectCodec(ObjectPropertyCodec objectCodec) {
        this.objectCodec = objectCodec;
    }

    public ObjectPropertyCodec getObjectCodec() {
        return objectCodec;
    }
    
    @Override
    public Boolean getBoolean(String key) {
        return cc.getBoolean(key);
    }

    @Override
    public Boolean getBoolean(String key, Boolean defaultValue) {
        return cc.getBoolean(key, defaultValue);
    }

    @Override
    public Byte getByte(String key) {
        return cc.getByte(key);
    }

    @Override
    public Byte getByte(String key, Byte defaultValue) {
        return cc.getByte(key, defaultValue);
    }

    @Override
    public Short getShort(String key) {
        return cc.getShort(key);
    }

    @Override
    public Short getShort(String key, Short defaultValue) {
        return cc.getShort(key, defaultValue);
    }

    @Override
    public Integer getInteger(String key) {
        return cc.getInt(key);
    }

    @Override
    public Integer getInteger(String key, Integer defaultValue) {
        return cc.getInteger(key, defaultValue);
    }

    @Override
    public Long getLong(String key) {
        return cc.getLong(key);
    }

    @Override
    public Long getLong(String key, Long defaultValue) {
        return cc.getLong(key, defaultValue);
    }

    @Override
    public BigInteger getBigInteger(String key) {
        return cc.getBigInteger(key);
    }

    @Override
    public BigInteger getBigInteger(String key, BigInteger defaultValue) {
        return cc.getBigInteger(key, defaultValue);
    }

    @Override
    public Float getFloat(String key) {
        return cc.getFloat(key);
    }

    @Override
    public Float getFloat(String key, Float defaultValue) {
        return cc.getFloat(key, defaultValue);
    }

    @Override
    public Double getDouble(String key) {
        return cc.getDouble(key);
    }

    @Override
    public Double getDouble(String key, Double defaultValue) {
        return cc.getDouble(key, defaultValue);
    }

    @Override
    public BigDecimal getBigDecimal(String key) {
        return cc.getBigDecimal(key);
    }

    @Override
    public BigDecimal getBigDecimal(String key, BigDecimal defaultValue) {
        return cc.getBigDecimal(key, defaultValue);
    }

    @Override
    public String getString(String key) {
        return cc.getString(key);
    }

    @Override
    public String getString(String key, String defaultValue) {
        return cc.getString(key, defaultValue);
    }

    @Override
    public <T> T getObject(Class<T> objectClass, String key) {
        return (T)objectCodec.decode(cc.getString(key));
    }
    
    @Override
    public <T> T getObject(Class<T> objectClass, String key, T defaultValue) {
        return cc.containsKey(key) && !cc.getString(key).isEmpty() ? getObject(objectClass, key) : defaultValue;
    }
    
}
