/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.configuration;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A read-only configuration that is comprised of one or more configuration
 * sources which are checked in order for each requested property.
 * 
 * The configuration sources are copied from the constructor arguments
 * so if you change the list or array of sources after creating the
 * CompositeConfiguration, it will not be affected.
 * 
 * @author jbuhacoff
 */
public class CompositeConfiguration implements Configuration {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CompositeConfiguration.class);
    protected List<Configuration> sources;
    
    public CompositeConfiguration(List<Configuration> sources) {
        this.sources = new ArrayList<>();
        this.sources.addAll(sources);
    }
    public CompositeConfiguration(Configuration... sources) {
        this.sources = new ArrayList<>();
        this.sources.addAll(Arrays.asList(sources));
    }
    
    // would be useful to have a contains(key) method to facilitate this
    // so we don't have to consider null vs empty for strings in different
    // implementations
    public Configuration source(String key) {
        for(Configuration source : sources) {
            String value = source.getString(key);
            if( value != null ) {
                return source;
            }
        }
        return null;
    }
    
    @Override
    public Boolean getBoolean(String key) {
        return getBoolean(key, null);
    }

    @Override
    public Boolean getBoolean(String key, Boolean defaultValue) {
        for(Configuration source : sources) {
            Boolean value = source.getBoolean(key);
            if( value != null ) {
                return value;
            }
        }
        return defaultValue;
    }

    @Override
    public Byte getByte(String key) {
        return getByte(key, null);
    }

    @Override
    public Byte getByte(String key, Byte defaultValue) {
        for(Configuration source : sources) {
            Byte value = source.getByte(key);
            if( value != null ) {
                return value;
            }
        }
        return defaultValue;
    }

    @Override
    public Short getShort(String key) {
        return getShort(key, null);
    }

    @Override
    public Short getShort(String key, Short defaultValue) {
        for(Configuration source : sources) {
            Short value = source.getShort(key);
            if( value != null ) {
                return value;
            }
        }
        return defaultValue;
    }

    @Override
    public Integer getInteger(String key) {
        return getInteger(key, null);
    }

    @Override
    public Integer getInteger(String key, Integer defaultValue) {
        for(Configuration source : sources) {
            Integer value = source.getInteger(key);
            if( value != null ) {
                return value;
            }
        }
        return defaultValue;
    }

    @Override
    public Long getLong(String key) {
        return getLong(key, null);
    }

    @Override
    public Long getLong(String key, Long defaultValue) {
        for(Configuration source : sources) {
            Long value = source.getLong(key);
            if( value != null ) {
                return value;
            }
        }
        return defaultValue;
    }

    @Override
    public BigInteger getBigInteger(String key) {
        return getBigInteger(key, null);
    }

    @Override
    public BigInteger getBigInteger(String key, BigInteger defaultValue) {
        for(Configuration source : sources) {
            BigInteger value = source.getBigInteger(key);
            if( value != null ) {
                return value;
            }
        }
        return defaultValue;
    }

    @Override
    public Float getFloat(String key) {
        return getFloat(key, null);
    }

    @Override
    public Float getFloat(String key, Float defaultValue) {
        for(Configuration source : sources) {
            Float value = source.getFloat(key);
            if( value != null ) {
                return value;
            }
        }
        return defaultValue;
    }

    @Override
    public Double getDouble(String key) {
        return getDouble(key, null);
    }

    @Override
    public Double getDouble(String key, Double defaultValue) {
        for(Configuration source : sources) {
            Double value = source.getDouble(key);
            if( value != null ) {
                return value;
            }
        }
        return defaultValue;
    }

    @Override
    public BigDecimal getBigDecimal(String key) {
        return getBigDecimal(key, null);
    }

    @Override
    public BigDecimal getBigDecimal(String key, BigDecimal defaultValue) {
        for(Configuration source : sources) {
            BigDecimal value = source.getBigDecimal(key);
            if( value != null ) {
                return value;
            }
        }
        return defaultValue;
    }

    @Override
    public String getString(String key) {
        return getString(key, null);
    }

    @Override
    public String getString(String key, String defaultValue) {
        for(Configuration source : sources) {
            log.debug("getString {} checking {}", key, source.getClass().getName());
            String value = source.getString(key);
            if( value != null ) {
                return value;
            }
        }
        return defaultValue;
    }

    @Override
    public <T> T getObject(Class<T> objectClass, String key) {
        return getObject(objectClass, key, null);
    }

    @Override
    public <T> T getObject(Class<T> objectClass, String key, T defaultValue) {
        for(Configuration source : sources) {
            T value = source.getObject(objectClass, key);
            if( value != null ) {
                return value;
            }
        }
        return defaultValue;
    }
    
}
