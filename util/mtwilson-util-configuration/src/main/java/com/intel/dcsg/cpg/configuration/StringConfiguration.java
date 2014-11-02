/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.configuration;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 *
 * @author jbuhacoff
 */
public abstract class StringConfiguration implements Configuration {
    protected ObjectPropertyCodec objectCodec;

    public StringConfiguration() {
        this.objectCodec = new XStreamPropertyCodec(); // default; can override with setObjectCodec(...)        
    }
    
    public void setObjectCodec(ObjectPropertyCodec objectCodec) {
        this.objectCodec = objectCodec;
    }

    public ObjectPropertyCodec getObjectCodec() {
        return objectCodec;
    }
    
    /**
     * 
     * @param key
     * @return true only if the configuration contains the key and it's value is not null and not empty
     */
    public boolean contains(String key) {
        String value = get(key);
        return value != null && !value.isEmpty();
    }
    
    abstract protected String get(String key);
    
    @Override
    public Boolean getBoolean(String key) {
        return getBoolean(key, null);
    }

    @Override
    public Boolean getBoolean(String key, Boolean defaultValue) {
        return contains(key) ? Boolean.valueOf(get(key)) : defaultValue;
    }

    @Override
    public Byte getByte(String key) {
        return getByte(key, null);
    }

    @Override
    public Byte getByte(String key, Byte defaultValue) {
        return contains(key) ? Byte.valueOf(get(key)) : defaultValue;
    }

    @Override
    public Short getShort(String key) {
        return getShort(key, null);
    }

    @Override
    public Short getShort(String key, Short defaultValue) {
        return contains(key) ? Short.valueOf(get(key)) : defaultValue;
    }

    @Override
    public Integer getInteger(String key) {
        return getInteger(key, null);
    }

    @Override
    public Integer getInteger(String key, Integer defaultValue) {
        return contains(key) ? Integer.valueOf(get(key)) : defaultValue;
    }

    @Override
    public Long getLong(String key) {
        return getLong(key, null);
    }

    @Override
    public Long getLong(String key, Long defaultValue) {
        return contains(key) ? Long.valueOf(get(key)) : defaultValue;
    }

    @Override
    public BigInteger getBigInteger(String key) {
        return getBigInteger(key, null);
    }

    @Override
    public BigInteger getBigInteger(String key, BigInteger defaultValue) {
        return contains(key) ? new BigInteger(get(key)) : defaultValue;
    }

    @Override
    public Float getFloat(String key) {
        return getFloat(key, null);
    }

    @Override
    public Float getFloat(String key, Float defaultValue) {
        return contains(key) ? Float.valueOf(get(key)) : defaultValue;
    }

    @Override
    public Double getDouble(String key) {
        return getDouble(key, null);
    }

    @Override
    public Double getDouble(String key, Double defaultValue) {
        return contains(key) ? Double.valueOf(get(key)) : defaultValue;
    }

    @Override
    public BigDecimal getBigDecimal(String key) {
        return getBigDecimal(key, null);
    }

    @Override
    public BigDecimal getBigDecimal(String key, BigDecimal defaultValue) {
        return contains(key) ? new BigDecimal(get(key)) : defaultValue;
    }

    @Override
    public String getString(String key) {
        return getString(key, null);
    }

    @Override
    public String getString(String key, String defaultValue) {
//        return contains(key) ? get(key) : defaultValue; // for strings it's valid to have an empty string as a configuration setting (for example an empty password in contrast to not set at all) if it's empty string we return that as the value
        String value = get(key);
        return value != null ? value : defaultValue;
    }

    @Override
    public <T> T getObject(Class<T> objectClass, String key) {
        return getObject(objectClass, key, null);
    }

    @Override
    public <T> T getObject(Class<T> objectClass, String key, T defaultValue) {
        return contains(key) ? (T)objectCodec.decode(get(key)) : defaultValue;
    }

}
