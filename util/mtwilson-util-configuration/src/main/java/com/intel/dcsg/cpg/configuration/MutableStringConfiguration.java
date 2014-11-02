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
public abstract class MutableStringConfiguration extends StringConfiguration implements MutableConfiguration {
    
    public MutableStringConfiguration() {
        super();
    }
    
    abstract protected void set(String key, String value);
    
    @Override
    public void setBoolean(String key, Boolean newValue) {
        if( newValue == null ) {
            set(key, null);
        }
        else {
            set(key, newValue.toString());
        }
    }

    @Override
    public void setByte(String key, Byte newValue) {
        if( newValue == null ) {
            set(key, null);
        }
        else {
            set(key, newValue.toString());
        }
    }

    @Override
    public void setShort(String key, Short newValue) {
        if( newValue == null ) {
            set(key, null);
        }
        else {
            set(key, newValue.toString());
        }
    }

    @Override
    public void setInteger(String key, Integer newValue) {
        if( newValue == null ) {
            set(key, null);
        }
        else {
            set(key, newValue.toString());
        }
    }

    @Override
    public void setLong(String key, Long newValue) {
        if( newValue == null ) {
            set(key, null);
        }
        else {
            set(key, newValue.toString());
        }
    }

    @Override
    public void setBigInteger(String key, BigInteger newValue) {
        if( newValue == null ) {
            set(key, null);
        }
        else {
            set(key, newValue.toString());
        }
    }

    @Override
    public void setFloat(String key, Float newValue) {
        if( newValue == null ) {
            set(key, null);
        }
        else {
            set(key, newValue.toString());
        }
    }

    @Override
    public void setDouble(String key, Double newValue) {
        if( newValue == null ) {
            set(key, null);
        }
        else {
            set(key, newValue.toString());
        }
    }

    @Override
    public void setBigDecimal(String key, BigDecimal newValue) {
        if( newValue == null ) {
            set(key, null);
        }
        else {
            set(key, newValue.toString());
        }
    }

    @Override
    public void setString(String key, String newValue) {
        set(key, newValue);
    }

    @Override
    public void setObject(String key, Object newValue) {
        if( newValue == null ) {
            set(key, null);
        }
        else {
            set(key, objectCodec.encode(newValue));
        }
    }

    
}
