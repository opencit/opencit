/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.classpath;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

/**
 *
 * @author jbuhacoff
 */
public class JarUtil {

    /**
     * XXX TODO: Probably can speed this up by checking entry.getSize() and creating a byte[] buffer large enough for
     * that size, then reading all bytes at once with read(buffer,0,size)
     *
     * @param entry
     * @return
     * @throws IOException
     */
    public static byte[] readJarEntry(JarFile jarFile, JarEntry jarEntry) throws IOException {
        try(InputStream in = jarFile.getInputStream(jarEntry)) { // throws IOException
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int next = in.read();
        while (next != -1) {
            out.write(next);
            next = in.read();
        }
        return out.toByteArray();
        }
    }

    /**
     * 
     * @param jarFile must be in an "open" state (it's automatically opened when new create the instance, but if you call close() on it then it will be closed)
     * @param className fully qualified class name which is "package name" dot "class name", for example com.intel.mtwilson.MyClassName
     * @return
     * @throws IOException 
     */
    public static byte[] readClass(JarFile jarFile, String className) throws IOException {
        JarEntry jarEntry = jarFile.getJarEntry(className.replace(".", "/") + ".class"); // throws IllegalStateException if jar is closed
        if (jarEntry == null) {
            return null;
        }
        byte[] data = JarUtil.readJarEntry(jarFile, jarEntry); // throws IOException
        return data;
    }

    public static Manifest readManifest(File jar) throws IOException {
        try(FileInputStream fileStream = new FileInputStream(jar);
            JarInputStream jarStream = new JarInputStream(fileStream)) {
            Manifest mf = jarStream.getManifest();
            return mf;
        }
    }
}
