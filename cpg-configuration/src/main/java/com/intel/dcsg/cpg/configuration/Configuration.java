/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.configuration;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * In contrast to commons-configuration, this Configuration interface is read-only. Methods that need
 * to modify the configuration passed to them must accept a MutableConfiguration to make this fact clear.
 * @author jbuhacoff
 */
public interface Configuration {
    Boolean getBoolean(String key);
    Boolean getBoolean(String key, Boolean defaultValue);
    Byte getByte(String key);
    Byte getByte(String key, Byte defaultValue);
    Short getShort(String key);
    Short getShort(String key, Short defaultValue);
    Integer getInteger(String key);
    Integer getInteger(String key, Integer defaultValue);
    Long getLong(String key);
    Long getLong(String key, Long defaultValue);
    BigInteger getBigInteger(String key);
    BigInteger getBigInteger(String key, BigInteger defaultValue);
    Float getFloat(String key);
    Float getFloat(String key, Float defaultValue);
    Double getDouble(String key);
    Double getDouble(String key, Double defaultValue);
    BigDecimal getBigDecimal(String key);
    BigDecimal getBigDecimal(String key, BigDecimal defaultValue);
    String getString(String key);
    String getString(String key, String defaultValue);
}
