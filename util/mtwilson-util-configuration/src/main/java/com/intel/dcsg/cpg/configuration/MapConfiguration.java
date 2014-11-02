/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.configuration;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author jbuhacoff
 */
public class MapConfiguration implements MutableConfiguration {
    private Map<String,Object> map;
    
    public MapConfiguration() {
        map = new HashMap<String,Object>();
    }
    public MapConfiguration(Map<String,Object> map) {
        this.map = map;
    }
    
    public Map<String,Object> getMap() { return map; }
    
    @Override
    public void setBoolean(String key, Boolean newValue) {
        map.put(key, newValue);
    }

    @Override
    public void setByte(String key, Byte newValue) {
        map.put(key, newValue);
    }

    @Override
    public void setShort(String key, Short newValue) {
        map.put(key, newValue);
    }

    @Override
    public void setInteger(String key, Integer newValue) {
        map.put(key, newValue);
    }

    @Override
    public void setLong(String key, Long newValue) {
        map.put(key, newValue);
    }

    @Override
    public void setBigInteger(String key, BigInteger newValue) {
        map.put(key, newValue);
    }

    @Override
    public void setFloat(String key, Float newValue) {
        map.put(key, newValue);
    }

    @Override
    public void setDouble(String key, Double newValue) {
        map.put(key, newValue);
    }

    @Override
    public void setBigDecimal(String key, BigDecimal newValue) {
        map.put(key, newValue);
    }

    @Override
    public void setString(String key, String newValue) {
        map.put(key, newValue);
    }

    @Override
    public void setObject(String key, Object newValue) {
        map.put(key, newValue);
    }

    @Override
    public Boolean getBoolean(String key) {
        return getBoolean(key, null);
    }

    @Override
    public Boolean getBoolean(String key, Boolean defaultValue) {
        return map.containsKey(key) ? (Boolean)map.get(key) : defaultValue;
    }

    @Override
    public Byte getByte(String key) {
        return getByte(key, null);
    }

    @Override
    public Byte getByte(String key, Byte defaultValue) {
        return map.containsKey(key) ? (Byte)map.get(key) : defaultValue;
    }

    @Override
    public Short getShort(String key) {
        return getShort(key, null);
    }

    @Override
    public Short getShort(String key, Short defaultValue) {
        return map.containsKey(key) ? (Short)map.get(key) : defaultValue;
    }

    @Override
    public Integer getInteger(String key) {
        return getInteger(key, null);
    }

    @Override
    public Integer getInteger(String key, Integer defaultValue) {
        return map.containsKey(key) ? (Integer)map.get(key) : defaultValue;
    }

    @Override
    public Long getLong(String key) {
        return getLong(key, null);
    }

    @Override
    public Long getLong(String key, Long defaultValue) {
        return map.containsKey(key) ? (Long)map.get(key) : defaultValue;
    }

    @Override
    public BigInteger getBigInteger(String key) {
        return getBigInteger(key, null);
    }

    @Override
    public BigInteger getBigInteger(String key, BigInteger defaultValue) {
        return map.containsKey(key) ? (BigInteger)map.get(key) : defaultValue;
    }

    @Override
    public Float getFloat(String key) {
        return getFloat(key, null);
    }

    @Override
    public Float getFloat(String key, Float defaultValue) {
        return map.containsKey(key) ? (Float)map.get(key) : defaultValue;
    }

    @Override
    public Double getDouble(String key) {
        return getDouble(key, null);
    }

    @Override
    public Double getDouble(String key, Double defaultValue) {
        return map.containsKey(key) ? (Double)map.get(key) : defaultValue;
    }

    @Override
    public BigDecimal getBigDecimal(String key) {
        return getBigDecimal(key, null);
    }

    @Override
    public BigDecimal getBigDecimal(String key, BigDecimal defaultValue) {
        return map.containsKey(key) ? (BigDecimal)map.get(key) : defaultValue;
    }

    @Override
    public String getString(String key) {
        return getString(key, null);
    }

    @Override
    public String getString(String key, String defaultValue) {
        return map.containsKey(key) ? (String)map.get(key) : defaultValue;
    }

    @Override
    public <T> T getObject(Class<T> objectClass, String key) {
        return getObject(objectClass, key, null);
    }

    @Override
    public <T> T getObject(Class<T> objectClass, String key, T defaultValue) {
        return map.containsKey(key) ? (T)map.get(key) : defaultValue;
    }
    
}
