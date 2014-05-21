/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jaxrs2.provider;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Date;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;

/**
 * You don't need this param converter if you use Iso8601Date objects in
 * the transfer objects directly. This converter exists only to convert plain 
 * java.util.Date objects using the Iso8601Date formatter/parser.
 * 
 * @author jbuhacoff
 */
@Provider
public class DateParamConverterProvider implements ParamConverterProvider {

    @Override
    public <T> ParamConverter<T> getConverter(Class<T> type, Type genericType, Annotation[] annotations) {
        if( Date.class.equals(type)) {
            return (ParamConverter<T>)new DateParamConverter();
        }
        return null;
    }
    
}
