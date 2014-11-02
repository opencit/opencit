/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.configuration;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * XXX draft, intended to abstract a single value that might appear in a Configuration object, so that
 * all the deserialization logic would be in implementations of this interface
 * 
 * TODO what about byte[] and lists and sets of things?  those are really useful to extract directly from
 * configuration without adding logic for that at every turn
 * 
 * @author jbuhacoff
 */
public interface Value {
    Boolean getBoolean();
    Byte getByte();
    Short getShort();
    Integer getInteger();
    Long getLong();
    BigInteger getBigInteger();
    Float getFloat();
    Double getDouble();
    BigDecimal getBigDecimal();
    String getString();
    <T> T getObject(Class<T> objectClass);
}
