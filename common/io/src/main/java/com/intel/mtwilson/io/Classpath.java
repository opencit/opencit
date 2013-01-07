/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.io;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 *
 * @author jbuhacoff
 */
public class Classpath {

    /**
     * XXX don't need to use this. it can be easily accomplished in one or two lines:
            Enumeration<URL> urls = getClass().getClassLoader().getResources("com/intel/mtwilson/database/"+databaseVendor);
            List<URL> list = Collections.list(urls);

     * Pass in the relative filenames to search for. Here is an example:
     * 
        ArrayList<URL> list = new ArrayList<URL>();
        list.addAll(Classpath.listResources(cl, String.format("META-INF/persistence-%s.xml", persistenceUnitName)));
        list.addAll(Classpath.listResources(cl, String.format("META-INF/%s/persistence.xml", persistenceUnitName)));
        list.addAll(Classpath.listResources(cl, String.format("META-INF/persistence.xml", persistenceUnitName)));
     * 
     * @param cl
     * @param name
     * @return
     * @throws IOException 
     */
    public static List<URL> listResources(ClassLoader cl, String name) throws IOException {
        ArrayList<URL> list = new ArrayList<URL>();
        Enumeration<URL> urls = cl.getResources(name); // IOException
        while (urls.hasMoreElements()) {
            list.add(urls.nextElement());
        }
//        PersistenceManager.log.info("Found {} resources for {}", new String[]{String.valueOf(list.size()), name});
        return list;
    }
    
}
