/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.configuration;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * XXX draft, intended to abstract a single value that might appear in a MutableConfiguration object, so 
 * that all the serialization logic would be in implementations of this interface
 *
 * TODO what about byte[] and lists and sets of things?  those are really useful to extract directly from
 * configuration without adding logic for that at every turn
 * 
 * @author jbuhacoff
 */
public interface MutableValue extends Value {
    void setBoolean(Boolean value);
    void setByte(Byte value);
    void setShort(Short value);
    void setInteger(Integer value);
    void setLong(Long value);
    void setBigInteger(BigInteger value);
    void setFloat(Float value);
    void setDouble(Double value);
    void setBigDecimal(BigDecimal value);
    void setString(String value);    
    void setObject(Object value);
}
