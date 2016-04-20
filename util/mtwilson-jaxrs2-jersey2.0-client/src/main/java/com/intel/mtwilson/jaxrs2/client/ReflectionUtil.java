/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jaxrs2.client;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.QueryParam;

/**
 *
 * @author jbuhacoff
 */
public class ReflectionUtil {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ReflectionUtil.class);
    
    public static boolean isQueryParamMethod(Method method) {
        boolean notStatic = !Modifier.isStatic(method.getModifiers());
        boolean conventional = method.getName().startsWith("get") || method.getName().startsWith("is");
        boolean noArgs = method.getParameterTypes().length == 0;
        boolean annotated = method.isAnnotationPresent(QueryParam.class);
        return notStatic && conventional && noArgs && annotated;
    }
    public static boolean isQueryParamField(Field field) {
        boolean annotated = field.isAnnotationPresent(QueryParam.class);
        return annotated;
    }
    public static String getPropertyName(Method method) {
        if( method.getReturnType().isAssignableFrom(boolean.class) && method.getName().startsWith("is") ) {
            String name = method.getName().substring(2);
            return name.substring(0, 1).toUpperCase() + name.substring(1);
        }
        if(  method.getName().startsWith("get") ) {
            String name = method.getName().substring(3);
            return name.substring(0, 1).toUpperCase() + name.substring(1);
        }
        return method.getName();
    }
    public static String getQueryParamName(Method method) {
        if( isQueryParamMethod(method) ) {
            QueryParam annotation = method.getAnnotation(QueryParam.class);
            if( annotation.value() != null ) {
                return annotation.value();
            }
            return getPropertyName(method);
        }
        return null;
    }
    public static String getQueryParamName(Field field) {
        if( isQueryParamField(field) ) {
            QueryParam annotation = field.getAnnotation(QueryParam.class);
            if( annotation.value() != null ) {
                return annotation.value();
            }
            return field.getName();
        }
        return null;
    }    
    
    /**
     * First checks getter methods and then fields directly.  If a @QueryParam
     * annotation is present on both the getter and field for the same 
     * property, only the getter will be used.
     * 
     * @param bean
     * @return a map of all @QueryParam names defined in the bean; some values may be null if they were not set
     */
    public static Map<String,Object> getQueryParams(Object bean) throws IllegalAccessException, InvocationTargetException {
        HashMap<String,Object> properties = new HashMap<String,Object>();
        // first check getters, then check fields
        Method[] methods = bean.getClass().getMethods();
        for(int i=0; i<methods.length; i++) {
            String propertyName = getQueryParamName(methods[i]);
            if( propertyName == null ) { continue; }
            Object value = methods[i].invoke(bean); // throws IllegalAccessException, InvocationTargetException
            properties.put(propertyName, value);
        }
        Field[] fields = bean.getClass().getFields();
        for(int i=0; i<fields.length; i++) {
            String propertyName = getQueryParamName(fields[i]);
            if( propertyName == null ) { continue; }
            if( properties.containsKey(propertyName) ) { continue; }
            Object value = fields[i].get(bean); // throws IllegalAccessException
            properties.put(propertyName, value);
        }
        return properties;
    }
}
