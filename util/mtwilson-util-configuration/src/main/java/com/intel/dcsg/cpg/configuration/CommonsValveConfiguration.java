/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.configuration;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import org.apache.commons.configuration.Configuration;

/**
 * This class allows reading from one Configuration instance (which could be
 * a CompositeConfiguration or any other implementation) and writing all changes 
 * to a second instance. 
 * 
 * All add, set, and clear operations go to the writable configuration.
 * All isEmpty, containsKey, or getKeys operations go to the readable configuration.
 * 
 * You can obtain the enclosed instances of the readable and writable configurations
 * by calling {@code getReadFrom()} and {@code getWriteTo()}
 * 
 * This functionality seems to have been the intent of the "in-memory" option
 * to the Apache CompositeConfiguration, but that implementation still attempts
 * to send all writes to all enclosed configurations. This causes an error,
 * for example, if one of the supposedly-readonly configurations is an
 * EnvironmentConfiguration (which doesn't support writes). 
 * 
 * Example of reading from an EnvironmentConfiguration while writing changes
 * to a PropertiesConfiguration:
 * 
 * <pre>
 * ValveConfiguration vc = new ValveConfiguration();
 * vc.setReadFrom(new EnvironmentConfiguration();
 * vc.setWriteTo(new PropertiesConfiguration();
 * </pre>
 * 
 * Example of including the write-to configuration also in the reading:
 * 
 * <pre>
 * PropertiesConfiguration writable = new PropertiesConfiguration();
 * CompositeConfiguration compositeReadable = new CompositeConfiguration();
 * compositeReadable.addConfiguration(new EnvironmentConfiguration();
 * compositeReadable.addConfiguration(writable);
 * ValveConfiguration vc = new ValveConfiguration();
 * vc.setReadFrom(compositeReadable);
 * vc.setWriteTo(writable);
 * </pre>
 * 
 * NOTE that when you use the same configuration instance in both reading
 * and writing, when you write to it the change will also be available when
 * reading since its the same instance (and this may depend on whether you
 * are reading from it directly or whether it's part of a composite configuration
 * where some of its properties may be hidden by other configurations in the
 * composite). 
 * 
 * Another possibility is to use the ValveConfiguration as a write-protection
 * for an existing configuration instance, passing the valve to some part of
 * the program, and then inspecting the changes that were attempted.
 * If an EnvironmentConfiguration is part of a CompositeConfiguration and
 * errors are being thrown when the program is attempting to clear a property,
 * simply wrap the EnvironmentConfiguration with a ValveConfiguration and
 * use a new PropertiesConfiguration instance as the writable, and this will
 * prevent the errors and also let you see what changes were attempted. 
 * Note that an attempted "clear" operation may not be detectable unless the
 * property does exist in the readable instance (so you can compare) or
 * unless you supply a logging writable instance that produces a changelog
 * so you can see all actions (even repeated clears, etc).
 * 
 * 
 * @author jbuhacoff
 */
public class CommonsValveConfiguration implements Configuration {
    protected Configuration readFrom;
    protected Configuration writeTo;

    public CommonsValveConfiguration() {
    }

    public CommonsValveConfiguration(Configuration readFrom, Configuration writeTo) {
        this.readFrom = readFrom;
        this.writeTo = writeTo;
    }

    public void setReadFrom(Configuration readFrom) {
        this.readFrom = readFrom;
    }

    public void setWriteTo(Configuration writeTo) {
        this.writeTo = writeTo;
    }

    public Configuration getReadFrom() {
        return readFrom;
    }

    public Configuration getWriteTo() {
        return writeTo;
    }
    
    
    
    @Override
    public boolean getBoolean(String key) {
        return readFrom.getBoolean(key);
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        return readFrom.getBoolean(key, defaultValue);
    }
    @Override
    public Boolean getBoolean(String key, Boolean defaultValue) {
        return readFrom.getBoolean(key, defaultValue);
    }

    @Override
    public byte getByte(String key) {
        return readFrom.getByte(key);
    }

    @Override
    public byte getByte(String key, byte defaultValue) {
        return readFrom.getByte(key, defaultValue);
    }
    @Override
    public Byte getByte(String key, Byte defaultValue) {
        return readFrom.getByte(key, defaultValue);
    }

    @Override
    public short getShort(String key) {
        return readFrom.getShort(key);
    }

    @Override
    public short getShort(String key, short defaultValue) {
        return readFrom.getShort(key, defaultValue);
    }
    @Override
    public Short getShort(String key, Short defaultValue) {
        return readFrom.getShort(key, defaultValue);
    }

    @Override
    public int getInt(String key) {
        return readFrom.getInt(key);
    }
    @Override
    public int getInt(String key, int defaultValue) {
        return readFrom.getInteger(key, defaultValue);
    }

    @Override
    public Integer getInteger(String key, Integer defaultValue) {
        return readFrom.getInteger(key, defaultValue);
    }

    @Override
    public long getLong(String key) {
        return readFrom.getLong(key);
    }

    @Override
    public long getLong(String key, long defaultValue) {
        return readFrom.getLong(key, defaultValue);
    }
    @Override
    public Long getLong(String key, Long defaultValue) {
        return readFrom.getLong(key, defaultValue);
    }

    @Override
    public BigInteger getBigInteger(String key) {
        return readFrom.getBigInteger(key);
    }

    @Override
    public BigInteger getBigInteger(String key, BigInteger defaultValue) {
        return readFrom.getBigInteger(key, defaultValue);
    }

    @Override
    public float getFloat(String key) {
        return readFrom.getFloat(key);
    }

    @Override
    public float getFloat(String key, float defaultValue) {
        return readFrom.getFloat(key, defaultValue);
    }
    @Override
    public Float getFloat(String key, Float defaultValue) {
        return readFrom.getFloat(key, defaultValue);
    }

    @Override
    public double getDouble(String key) {
        return readFrom.getDouble(key);
    }
    @Override
    public double getDouble(String key, double defaultValue) {
        return readFrom.getDouble(key, defaultValue);
    }

    @Override
    public Double getDouble(String key, Double defaultValue) {
        return readFrom.getDouble(key, defaultValue);
    }

    @Override
    public BigDecimal getBigDecimal(String key) {
        return readFrom.getBigDecimal(key);
    }

    @Override
    public BigDecimal getBigDecimal(String key, BigDecimal defaultValue) {
        return readFrom.getBigDecimal(key, defaultValue);
    }

    @Override
    public String getString(String key) {
        return readFrom.getString(key);
    }

    @Override
    public String getString(String key, String defaultValue) {
        return readFrom.getString(key, defaultValue);
    }

    /**
     * Returns a new ValveConfiguration instance configured 
     * with a subset of the
     * readable and a subset of the writable enclosed
     * configurations.
     * 
     * @param prefix
     * @return 
     */
    @Override
    public Configuration subset(String prefix) {
        return new CommonsValveConfiguration(readFrom.subset(prefix), writeTo.subset(prefix));
    }

    /**
     * If you need to check the writable configuration's isEmpty(),
     * or you want to explicitly get isEmpty() from the readable
     * configuration, call {@code getWriteTo().isEmpty()} or
     * {@code getReadFrom().isEmpty()}
     * @return result of isEmpty() on the readable configuration
     */
    @Override
    public boolean isEmpty() {
        return readFrom.isEmpty();
    }

    /**
     * If you need to check the writable configuration's containsKey(),
     * or you want to explicitly get containsKey() from the readable
     * configuration, call {@code getWriteTo().containsKey()} or
     * {@code getReadFrom().containsKey()}
     * @return result of containsKey() on the readable configuration
     */
    @Override
    public boolean containsKey(String key) {
        return readFrom.containsKey(key);
    }

    @Override
    public void addProperty(String key, Object value) {
        writeTo.setProperty(key, value);
    }

    @Override
    public void setProperty(String key, Object value) {
        writeTo.setProperty(key, value);
    }

    @Override
    public void clearProperty(String key) {
        writeTo.clearProperty(key);
    }

    @Override
    public void clear() {
        writeTo.clear();
    }

    @Override
    public Object getProperty(String key) {
        return readFrom.getProperty(key);
    }

    @Override
    public Iterator<String> getKeys(String prefix) {
        return readFrom.getKeys(prefix);// there's no reverse transformation available  - this may not work in all cases
    }

    @Override
    public Iterator<String> getKeys() {
        return readFrom.getKeys(); // there's no reverse transformation available  - this may not work in all cases
    }

    @Override
    public Properties getProperties(String key) {
        return readFrom.getProperties(key);
    }

    @Override
    public String[] getStringArray(String key) {
        return readFrom.getStringArray(key);
    }

    @Override
    public List<Object> getList(String key) {
        return readFrom.getList(key);
    }

    @Override
    public List<Object> getList(String key, List<Object> defaultValue) {
        return readFrom.getList(key, defaultValue);
    }
    
}
