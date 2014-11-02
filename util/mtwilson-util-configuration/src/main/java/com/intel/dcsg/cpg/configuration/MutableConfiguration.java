/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.configuration;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * In contrast to commons-configuration, a MutableConfiguration is explicitly capable of saving
 * configuration changes. If the MutableConfiguration represents a compound configuration from
 * difference sources, then it specifically tracks from which mutable source the each value is coming from
 * and saves it back to that source, or designates a single mutable source to which all changes will
 * be written. This depends on the specific MutableConfiguration implementation.
 * 
 * XXX TODO need a concurrent implementation like ConcurrentHashMap, but that class is generic and allows
 * only one type of value... so might have to implement the same thing for this, or somehow adapt it so it
 * can use just one type (like maybe make a value type that records the datatype name and the value)
 * @author jbuhacoff
 */
public interface MutableConfiguration extends Configuration {
//    void setNull(String key); // clear (set to null) or remove the value (if the storage is a properties file, this would be setting the key to nothing)
//    void remove(String key); // removes the key completely (if the storage is a properties file, the property would be completely removed from the file)
    void setBoolean(String key, Boolean newValue);
    void setByte(String key, Byte newValue);
    void setShort(String key, Short newValue);
    void setInteger(String key, Integer newValue);
    void setLong(String key, Long newValue);
    void setBigInteger(String key, BigInteger newValue);
    void setFloat(String key, Float newValue);
    void setDouble(String key, Double newValue);
    void setBigDecimal(String key, BigDecimal newValue);
    void setString(String key, String newValue);
    void setObject(String key, Object newValue);
}
