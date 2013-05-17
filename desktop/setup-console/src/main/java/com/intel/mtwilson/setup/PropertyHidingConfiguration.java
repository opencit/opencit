/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import org.apache.commons.configuration.Configuration;

/**
 *
 * @author jbuhacoff
 */
public class PropertyHidingConfiguration implements Configuration {

    private Configuration base;
    private HashSet<String> hidden = new HashSet<String>();
    private HashSet<String> nulled = new HashSet<String>();
    private HashMap<String,String> replaced = new HashMap<String,String>();

    public PropertyHidingConfiguration(Configuration base) {
        this.base = base;
    }

    public void hideProperty(String key) {
        hidden.add(key);
        if(nulled.contains(key)) { nulled.remove(key); }
        if(replaced.containsKey(key)) { replaced.remove(key); }
    }

    public boolean isHidden(String key) {
        return hidden.contains(key);
    }
    
    public void nullProperty(String key) {
        nulled.add(key);
        if(hidden.contains(key)) { hidden.remove(key); }
        if(replaced.containsKey(key)) { replaced.remove(key); }
    }
    public boolean isNulled(String key) {
        return nulled.contains(key);
    }

    public void replaceProperty(String key, String value) {
        replaced.put(key, value);
        if(hidden.contains(key)) { hidden.remove(key); }
        if(nulled.contains(key)) { nulled.remove(key); }
    }
    
    public boolean isReplaced(String key) {
        return replaced.containsKey(key);
    }
    
    @Override
    public Configuration subset(String prefix) {
        return base.subset(prefix);
    }

    @Override
    public boolean isEmpty() {
        return base.isEmpty();
    }

    @Override
    public boolean containsKey(String key) {
        return !isHidden(key) && base.containsKey(key);
    }

    @Override
    public void addProperty(String key, Object value) {
        if(isHidden(key)) { return; }
        if(isNulled(key)) { return; }
        base.addProperty(key, value);
    }

    @Override
    public void setProperty(String key, Object value) {
        if(isHidden(key)) { return; }
        if(isNulled(key)) { return; }
        base.setProperty(key, value);
    }

    @Override
    public void clearProperty(String key) {
        if(isHidden(key)) { return; }
        if(isNulled(key)) { return; }
        base.clearProperty(key);
    }

    @Override
    public void clear() {
        base.clear();
    }

    @Override
    public Object getProperty(String key) {
        if(isHidden(key)) { return null; }
        if(isNulled(key)) { return null; }
        return base.getProperty(key);
    }

    @Override
    public Iterator<String> getKeys(String prefix) {
        return base.getKeys(prefix); // will include hidden properties but they won't be accessible
    }

    @Override
    public Iterator<String> getKeys() {
        return base.getKeys(); // will include hidden properties but they won't be accessible
    }

    @Override
    public Properties getProperties(String key) {
        if(isHidden(key)) { return null; }
        if(isNulled(key)) { return null; }
        return base.getProperties(key);
    }

    @Override
    public boolean getBoolean(String key) {
        if(isHidden(key)) { return false; }
        if(isNulled(key)) { return false; }
        return base.getBoolean(key);
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        if (isHidden(key)) {
            return defaultValue;
        }
        if(isNulled(key)) { return false; }
        return base.getBoolean(key, defaultValue);
    }

    @Override
    public Boolean getBoolean(String key, Boolean defaultValue) {
        if (isHidden(key)) {
            return defaultValue;
        }
        if(isNulled(key)) { return null; }
        return base.getBoolean(key, defaultValue);
    }

    @Override
    public byte getByte(String key) {
        if (isHidden(key)) {
            return 0;
        }
        if(isNulled(key)) { return 0; }
        return base.getByte(key);
    }

    @Override
    public byte getByte(String key, byte defaultValue) {
        if (isHidden(key)) {
            return defaultValue;
        }
        if(isNulled(key)) { return 0; }
        return base.getByte(key, defaultValue);
    }

    @Override
    public Byte getByte(String key, Byte defaultValue) {
        if (isHidden(key)) {
            return defaultValue;
        }
        if(isNulled(key)) { return null; }
        return base.getByte(key, defaultValue);
    }

    @Override
    public double getDouble(String key) {
        if (isHidden(key)) {
            return 0;
        }
        if(isNulled(key)) { return 0; }
        return base.getDouble(key);
    }

    @Override
    public double getDouble(String key, double defaultValue) {
        if (isHidden(key)) {
            return defaultValue;
        }
        if(isNulled(key)) { return 0; }
        return base.getDouble(key, defaultValue);
    }

    @Override
    public Double getDouble(String key, Double defaultValue) {
        if (isHidden(key)) {
            return defaultValue;
        }
        if(isNulled(key)) { return null; }
        return base.getDouble(key, defaultValue);
    }

    @Override
    public float getFloat(String key) {
        if (isHidden(key)) {
            return 0;
        }
        if(isNulled(key)) { return 0; }
        return base.getFloat(key);
    }

    @Override
    public float getFloat(String key, float defaultValue) {
        if (isHidden(key)) {
            return defaultValue;
        }
        if(isNulled(key)) { return 0; }
        return base.getFloat(key, defaultValue);
    }

    @Override
    public Float getFloat(String key, Float defaultValue) {
        if (isHidden(key)) {
            return defaultValue;
        }
        if(isNulled(key)) { return null; }
        return base.getFloat(key, defaultValue);
    }

    @Override
    public int getInt(String key) {
        if (isHidden(key)) {
            return 0;
        }
        if(isNulled(key)) { return 0; }
        return base.getInt(key);
    }

    @Override
    public int getInt(String key, int defaultValue) {
        if (isHidden(key)) {
            return defaultValue;
        }
        if(isNulled(key)) { return 0; }
        return base.getInt(key, defaultValue);
    }

    @Override
    public Integer getInteger(String key, Integer defaultValue) {
        if (isHidden(key)) {
            return defaultValue;
        }
        if(isNulled(key)) { return null; }
        return base.getInteger(key, defaultValue);
    }

    @Override
    public long getLong(String key) {
        if (isHidden(key)) {
            return 0;
        }
        if(isNulled(key)) { return 0; }
        return base.getLong(key);
    }

    @Override
    public long getLong(String key, long defaultValue) {
        if (isHidden(key)) {
            return defaultValue;
        }
        if(isNulled(key)) { return 0; }
        return base.getLong(key, defaultValue);
    }

    @Override
    public Long getLong(String key, Long defaultValue) {
        if (isHidden(key)) {
            return defaultValue;
        }
        if(isNulled(key)) { return null; }
        return base.getLong(key, defaultValue);
    }

    @Override
    public short getShort(String key) {
        if (isHidden(key)) {
            return 0;
        }
        if(isNulled(key)) { return 0; }
        return base.getShort(key);
    }

    @Override
    public short getShort(String key, short defaultValue) {
        if (isHidden(key)) {
            return defaultValue;
        }
        if(isNulled(key)) { return 0; }
        return base.getShort(key, defaultValue);
    }

    @Override
    public Short getShort(String key, Short defaultValue) {
        if (isHidden(key)) {
            return defaultValue;
        }
        if(isNulled(key)) { return null; }
        return base.getShort(key, defaultValue);
    }

    @Override
    public BigDecimal getBigDecimal(String key) {
        if (isHidden(key)) {
            return null;
        }
        if(isNulled(key)) { return null; }
        return base.getBigDecimal(key);
    }

    @Override
    public BigDecimal getBigDecimal(String key, BigDecimal defaultValue) {
        if (isHidden(key)) {
            return defaultValue;
        }
        if(isNulled(key)) { return null; }
        return base.getBigDecimal(key, defaultValue);
    }

    @Override
    public BigInteger getBigInteger(String key) {
        if (isHidden(key)) {
            return null;
        }
        if(isNulled(key)) { return null; }
        return base.getBigInteger(key);
    }

    @Override
    public BigInteger getBigInteger(String key, BigInteger defaultValue) {
        if (isHidden(key)) {
            return defaultValue;
        }
        if(isNulled(key)) { return null; }
        return base.getBigInteger(key, defaultValue);
    }

    @Override
    public String getString(String key) {
        if (isHidden(key)) {
            return null;
        }
        if(isNulled(key)) { return null; }
        if(isReplaced(key)) { return replaced.get(key); }
        return base.getString(key);
    }

    @Override
    public String getString(String key, String defaultValue) {
        if (isHidden(key)) {
            return defaultValue;
        }
        if(isNulled(key)) { return null; }
        if(isReplaced(key)) { return replaced.get(key); }
        return base.getString(key, defaultValue);
    }

    @Override
    public String[] getStringArray(String key) {
        if (isHidden(key)) {
            return null;
        }
        if(isNulled(key)) { return null; }
        return base.getStringArray(key);
    }

    @Override
    public List<Object> getList(String key) {
        if (isHidden(key)) {
            return null;
        }
        if(isNulled(key)) { return null; }
        return base.getList(key);
    }

    @Override
    public List<Object> getList(String key, List<Object> defaultValue) {
        if (isHidden(key)) {
            return defaultValue;
        }
        if(isNulled(key)) { return null; }
        return base.getList(key, defaultValue);
    }
}