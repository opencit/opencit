/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.configuration;

import com.intel.mtwilson.pipe.Transformer;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 *
 * @author jbuhacoff
 */
public class MutableKeyTransformerConfiguration extends KeyTransformerConfiguration implements MutableConfiguration {
    protected MutableConfiguration mutableConfiguration;

    public MutableKeyTransformerConfiguration(Transformer<String> transformer, MutableConfiguration configuration) {
        super(transformer, configuration);
        this.mutableConfiguration = configuration;
    }
    
    @Override
    public void setBoolean(String key, Boolean newValue) {
        mutableConfiguration.setBoolean(transformer.transform(key), newValue);
    }

    @Override
    public void setByte(String key, Byte newValue) {
        mutableConfiguration.setByte(transformer.transform(key), newValue);
    }

    @Override
    public void setShort(String key, Short newValue) {
        mutableConfiguration.setShort(transformer.transform(key), newValue);
    }

    @Override
    public void setInteger(String key, Integer newValue) {
        mutableConfiguration.setInteger(transformer.transform(key), newValue);
    }

    @Override
    public void setLong(String key, Long newValue) {
        mutableConfiguration.setLong(transformer.transform(key), newValue);
    }

    @Override
    public void setBigInteger(String key, BigInteger newValue) {
        mutableConfiguration.setBigInteger(transformer.transform(key), newValue);
    }

    @Override
    public void setFloat(String key, Float newValue) {
        mutableConfiguration.setFloat(transformer.transform(key), newValue);
    }

    @Override
    public void setDouble(String key, Double newValue) {
        mutableConfiguration.setDouble(transformer.transform(key), newValue);
    }

    @Override
    public void setBigDecimal(String key, BigDecimal newValue) {
        mutableConfiguration.setBigDecimal(transformer.transform(key), newValue);
    }

    @Override
    public void setString(String key, String newValue) {
        mutableConfiguration.setString(transformer.transform(key), newValue);
    }

    @Override
    public void setObject(String key, Object newValue) {
        mutableConfiguration.setObject(transformer.transform(key), newValue);
    }
    
}
