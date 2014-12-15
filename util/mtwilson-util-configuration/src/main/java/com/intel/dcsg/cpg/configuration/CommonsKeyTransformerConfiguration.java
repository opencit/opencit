/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.configuration;

import com.intel.mtwilson.pipe.Transformer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import org.apache.commons.configuration.Configuration;

/**
 * This class is a decorator that can wrap any existing Configuration 
 * instance to transform names of keys before they are passed to that
 * instance. For example if the application uses key names like 
 * java.style.properties it can automatically transform them to 
 * SHELL_STYLE_VARIABLES by using the KeyTransformerConfiguration with
 * the AllCapsNamingStrategy. Another example of a transformation might
 * be to prepend the application name to every key, so that "fruit.color"
 * becomes "myapp.fruit.color"
 * 
 * @author jbuhacoff
 */
public class CommonsKeyTransformerConfiguration implements Configuration {
    protected Configuration configuration;
    protected Transformer<String> transformer;
    
    public CommonsKeyTransformerConfiguration(Transformer<String> transformer, Configuration configuration) {
        this.transformer = transformer;
        this.configuration = configuration;
    }
    
    @Override
    public boolean getBoolean(String key) {
        return configuration.getBoolean(transformer.transform(key));
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        return configuration.getBoolean(transformer.transform(key), defaultValue);
    }
    @Override
    public Boolean getBoolean(String key, Boolean defaultValue) {
        return configuration.getBoolean(transformer.transform(key), defaultValue);
    }

    @Override
    public byte getByte(String key) {
        return configuration.getByte(transformer.transform(key));
    }

    @Override
    public byte getByte(String key, byte defaultValue) {
        return configuration.getByte(transformer.transform(key), defaultValue);
    }
    @Override
    public Byte getByte(String key, Byte defaultValue) {
        return configuration.getByte(transformer.transform(key), defaultValue);
    }

    @Override
    public short getShort(String key) {
        return configuration.getShort(transformer.transform(key));
    }

    @Override
    public short getShort(String key, short defaultValue) {
        return configuration.getShort(transformer.transform(key), defaultValue);
    }
    @Override
    public Short getShort(String key, Short defaultValue) {
        return configuration.getShort(transformer.transform(key), defaultValue);
    }

    @Override
    public int getInt(String key) {
        return configuration.getInt(transformer.transform(key));
    }
    @Override
    public int getInt(String key, int defaultValue) {
        return configuration.getInteger(transformer.transform(key), defaultValue);
    }

    @Override
    public Integer getInteger(String key, Integer defaultValue) {
        return configuration.getInteger(transformer.transform(key), defaultValue);
    }

    @Override
    public long getLong(String key) {
        return configuration.getLong(transformer.transform(key));
    }

    @Override
    public long getLong(String key, long defaultValue) {
        return configuration.getLong(transformer.transform(key), defaultValue);
    }
    @Override
    public Long getLong(String key, Long defaultValue) {
        return configuration.getLong(transformer.transform(key), defaultValue);
    }

    @Override
    public BigInteger getBigInteger(String key) {
        return configuration.getBigInteger(transformer.transform(key));
    }

    @Override
    public BigInteger getBigInteger(String key, BigInteger defaultValue) {
        return configuration.getBigInteger(transformer.transform(key), defaultValue);
    }

    @Override
    public float getFloat(String key) {
        return configuration.getFloat(transformer.transform(key));
    }

    @Override
    public float getFloat(String key, float defaultValue) {
        return configuration.getFloat(transformer.transform(key), defaultValue);
    }
    @Override
    public Float getFloat(String key, Float defaultValue) {
        return configuration.getFloat(transformer.transform(key), defaultValue);
    }

    @Override
    public double getDouble(String key) {
        return configuration.getDouble(transformer.transform(key));
    }
    @Override
    public double getDouble(String key, double defaultValue) {
        return configuration.getDouble(transformer.transform(key), defaultValue);
    }

    @Override
    public Double getDouble(String key, Double defaultValue) {
        return configuration.getDouble(transformer.transform(key), defaultValue);
    }

    @Override
    public BigDecimal getBigDecimal(String key) {
        return configuration.getBigDecimal(transformer.transform(key));
    }

    @Override
    public BigDecimal getBigDecimal(String key, BigDecimal defaultValue) {
        return configuration.getBigDecimal(transformer.transform(key), defaultValue);
    }

    @Override
    public String getString(String key) {
        return configuration.getString(transformer.transform(key));
    }

    @Override
    public String getString(String key, String defaultValue) {
        return configuration.getString(transformer.transform(key), defaultValue);
    }

    @Override
    public Configuration subset(String prefix) {
        return new CommonsKeyTransformerConfiguration(transformer, configuration.subset(transformer.transform(prefix)));
    }

    @Override
    public boolean isEmpty() {
        return configuration.isEmpty();
    }

    @Override
    public boolean containsKey(String key) {
        return configuration.containsKey(transformer.transform(key));
    }

    @Override
    public void addProperty(String key, Object value) {
        configuration.addProperty(transformer.transform(key), value);
    }

    @Override
    public void setProperty(String key, Object value) {
        configuration.setProperty(transformer.transform(key), value);
    }

    @Override
    public void clearProperty(String key) {
        configuration.clearProperty(transformer.transform(key));
    }

    @Override
    public void clear() {
        configuration.clear();
    }

    @Override
    public Object getProperty(String key) {
        return configuration.getProperty(transformer.transform(key));
    }

    @Override
    public Iterator<String> getKeys(String prefix) {
        return configuration.getKeys(transformer.transform(prefix));// there's no reverse transformation available  - this may not work in all cases
    }

    @Override
    public Iterator<String> getKeys() {
        return configuration.getKeys(); // there's no reverse transformation available  - this may not work in all cases
    }

    @Override
    public Properties getProperties(String key) {
        return configuration.getProperties(transformer.transform(key));
    }

    @Override
    public String[] getStringArray(String key) {
        return configuration.getStringArray(transformer.transform(key));
    }

    @Override
    public List<Object> getList(String key) {
        return configuration.getList(transformer.transform(key));
    }

    @Override
    public List<Object> getList(String key, List<Object> defaultValue) {
        return configuration.getList(transformer.transform(key), defaultValue);
    }
    
}
