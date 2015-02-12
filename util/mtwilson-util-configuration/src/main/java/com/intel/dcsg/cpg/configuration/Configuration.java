/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.configuration;

import java.util.Set;

/**
 * Provides a simple read-only interface to configuration properties. 
 * 
 * The {@code getString} methods return null if a key is not present or if
 * it's present but has a null value. 
 * 
 * Null values are treated as missing properties. 
 * 
 * To check if a key is present, call {@code keys().contains()} - this does
 * not guarantee that the key has a value as this may be implementation dependent
 * but implementations SHOULD NOT advertise keys that have null values.
 * 
 * Configuration settings are all strings. The programmer may use a decorator
 * to automatically parse strings into numbers, ranges, URLs, comma-separated
 * lists, etc. For large objects such as keystores it is recommended to 
 * store them in a data repository and only put file location, password, 
 * key handles, etc. in the configuration.
 * 
 * In desktop applications the user's preferences may generally be changed
 * from anywhere in the user interface where it's most convenient. In
 * server applications the configuration is generally not changed on the fly
 * and when it is it typically requires special privileges. The isEditable
 * method is provided to allow applications to determine whether a specific
 * Configuration instance allows editing.
 * 
 * @author jbuhacoff
 */
public interface Configuration {
    /**
     * The return value is a {@code Set} which may not be modifiable. 
     * Implementations may enforce this by throwing UnsupportedOperationException
     * on any of the state-changing methods of the set.
     * 
     * Configuration keys with null values may be omitted from the returned set.
     * 
     * @return the set of keys available in the configuration
     */
    Set<String> keys();
    
    /**
     * Get the value of a configuration key, or null if the key is not set.
     * 
     * @param key
     * @return 
     */
    String get(String key);
    
    /**
     * Get the value of a configuration key, or the specified defaultValue
     * if the configuration key is not set. 
     * 
     * A null value for a configuration key is the same as not being set.
     * 
     * @param key
     * @param defaultValue
     * @return 
     */
    String get(String key, String defaultValue);
    
    /**
     * Set a configuration key to the specified value.
     * 
     * To remove a configuration key, set its value to null.
     * 
     * Implementations may throw UnsupportedOperationException if 
     * {@code isEditable} returns false.
     * 
     * @param key
     * @param value 
     */
    void set(String key, String value);
    
    /**
     * 
     * @return true if the implementation supports {@code set}
     */
    boolean isEditable();
//    String get(Property property);
    /*
    @Deprecated
    Boolean getBoolean(String key);
    @Deprecated
    Boolean getBoolean(String key, Boolean defaultValue);
    @Deprecated
    Byte getByte(String key);
    @Deprecated
    Byte getByte(String key, Byte defaultValue);
    @Deprecated
    Short getShort(String key);
    @Deprecated
    Short getShort(String key, Short defaultValue);
    @Deprecated
    Integer getInteger(String key);
    @Deprecated
    Integer getInteger(String key, Integer defaultValue);
    @Deprecated
    Long getLong(String key);
    @Deprecated
    Long getLong(String key, Long defaultValue);
    @Deprecated
    BigInteger getBigInteger(String key);
    @Deprecated
    BigInteger getBigInteger(String key, BigInteger defaultValue);
    @Deprecated
    Float getFloat(String key);
    @Deprecated
    Float getFloat(String key, Float defaultValue);
    @Deprecated
    Double getDouble(String key);
    @Deprecated
    Double getDouble(String key, Double defaultValue);
    @Deprecated
    BigDecimal getBigDecimal(String key);
    @Deprecated
    BigDecimal getBigDecimal(String key, BigDecimal defaultValue);
    @Deprecated
    String getString(String key);
//    @Deprecated
//    String getString(String key, String defaultValue);
    @Deprecated
    <T> T getObject(Class<T> objectClass, String key);
    @Deprecated
    <T> T getObject(Class<T> objectClass, String key, T defaultValue);
    */
}
