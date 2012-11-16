/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.backport.org.opensaml.xml;

/**
 *
 * @author jbuhacoff
 */
public class XMLConfigurator {
    /*
    public static Object createClassInstance(XMLConfigurator self, org.w3c.dom.Element configuration) throws org.opensaml.xml.ConfigurationException {
        String className = configuration.getAttributeNS(null, "className");
        className = org.opensaml.xml.DatatypeHelper.safeTrimOrNullString(className);
        if( className == null ) {
            return null;
        }
        try {
            ClassLoader classLoader = self.getClass().getClassLoader();
            if( classLoader == null ) {
                classLoader = ClassLoader.getSystemClassLoader();
            }
            Class clazz = classLoader.loadClass(className);
            java.lang.reflect.Constructor constructor = clazz.getConstructor();
            return constructor.newInstance();
        }
        catch(Exception e) {
            throw new org.opensaml.xml.ConfigurationException("[backport] Can not create instance of "+className, e);
        }
    }
    * 
    */
}
