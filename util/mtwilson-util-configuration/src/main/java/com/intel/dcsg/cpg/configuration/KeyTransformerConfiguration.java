/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.configuration;

import com.intel.mtwilson.pipe.Transformer;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * This class is a decorator that can wrap any existing Configuration 
 * instance to tranform names of keys before they are passed to that
 * instance. For example if the application uses key names like 
 * java.style.properties it can automatically transform them to 
 * SHELL_STYLE_VARIABLES by using the KeyTransformerConfiguration with
 * the AllCapsNamingStrategy. Another example of a transformation might
 * be to prepend the application name to every key, so that "fruit.color"
 * becomes "myapp.fruit.color"
 * 
 * @author jbuhacoff
 */
public class KeyTransformerConfiguration implements Configuration {
    protected Configuration configuration;
    protected Transformer<String> transformer;
    
    public KeyTransformerConfiguration(Transformer<String> transformer, Configuration configuration) {
        this.transformer = transformer;
        this.configuration = configuration;
    }
    
    @Override
    public Boolean getBoolean(String key) {
        return configuration.getBoolean(transformer.transform(key));
    }

    @Override
    public Boolean getBoolean(String key, Boolean defaultValue) {
        return configuration.getBoolean(transformer.transform(key), defaultValue);
    }

    @Override
    public Byte getByte(String key) {
        return configuration.getByte(transformer.transform(key));
    }

    @Override
    public Byte getByte(String key, Byte defaultValue) {
        return configuration.getByte(transformer.transform(key), defaultValue);
    }

    @Override
    public Short getShort(String key) {
        return configuration.getShort(transformer.transform(key));
    }

    @Override
    public Short getShort(String key, Short defaultValue) {
        return configuration.getShort(transformer.transform(key), defaultValue);
    }

    @Override
    public Integer getInteger(String key) {
        return configuration.getInteger(transformer.transform(key));
    }

    @Override
    public Integer getInteger(String key, Integer defaultValue) {
        return configuration.getInteger(transformer.transform(key), defaultValue);
    }

    @Override
    public Long getLong(String key) {
        return configuration.getLong(transformer.transform(key));
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
    public Float getFloat(String key) {
        return configuration.getFloat(transformer.transform(key));
    }

    @Override
    public Float getFloat(String key, Float defaultValue) {
        return configuration.getFloat(transformer.transform(key), defaultValue);
    }

    @Override
    public Double getDouble(String key) {
        return configuration.getDouble(transformer.transform(key));
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
    public <T> T getObject(Class<T> objectClass, String key) {
        return configuration.getObject(objectClass, transformer.transform(key));
    }

    @Override
    public <T> T getObject(Class<T> objectClass, String key, T defaultValue) {
        return configuration.getObject(objectClass, transformer.transform(key), defaultValue);
    }
    
}
