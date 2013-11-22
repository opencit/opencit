/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jmod;

import com.intel.mtwilson.jmod.cl.MavenResolver;
import com.intel.mtwilson.jmod.cl.MultiJarFileClassLoader;
import com.intel.mtwilson.jmod.cl.JarFileClassLoader;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.Manifest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * For convenience, this junit test assumes that the MavenResolver is already working, and is not under test.
 * Only the class loading system is under test here.
 * 
 * XXX TODO this unit test belongs in the launcher  where it defines the various class loader strategies, so we
 * can test them.   launcher depends on container so we still have access to reflection utils. 
 * 
 * @author jbuhacoff
 */
public class ClassLoaderTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ClassLoaderTest.class);

    /**
     * This one works because it loads VersionPlugin from mtwilson-version-1.2-SNAPSHOT.jar and everything
     * else from the system class loader (which has the test-scope classpath maven built for junit testing)
     * @throws Exception
     */
    @Test
    public void testJarFileClassLoader() throws Exception {
        // first we identify a plugin to load, and list its dependencies (and their locations on disk)
        MavenResolver resolver = new MavenResolver();
        File pluginJarFile = resolver.findJarFile("com.intel.mtwilson.plugins", "mtwilson-version", "1.2-SNAPSHOT");
        JarFileClassLoader cl = new JarFileClassLoader(pluginJarFile);
        Class<?> pluginClass = cl.loadClass("com.intel.mtwilson.plugin.version.VersionPlugin"); // throws ClassNotFoundException
        Object versionPlugin = pluginClass.newInstance();  // throws InstantiationException, IllegalAccessException
        log.debug("Loaded version plugin: {}", versionPlugin.getClass().getName());
    }

    /**
     * This test loads mtwilson-plugin-sample-convention using a single jar classloader that has fenced off
     * all classes matching *.impl.* ... so we should be able to load the SampleSingletonService interface
     * but not th eprivate SampleSingletonServiceImpl class.
     */
    @Test
    public void testFencedJarFileClassLoader() throws Exception {
        MavenResolver resolver = new MavenResolver();
        File pluginJarFile = resolver.findJarFile("com.intel.mtwilson.plugins", "mtwilson-version", "1.2-SNAPSHOT");
        JarFileClassLoader cl = new JarFileClassLoader(pluginJarFile);
        Class<?> pluginClass = cl.loadClass("com.intel.mtwilson.plugin.version.VersionPlugin"); // throws ClassNotFoundException
        Object versionPlugin = pluginClass.newInstance();  // throws InstantiationException, IllegalAccessException
        log.debug("Loaded version plugin: {}", versionPlugin.getClass().getName());
        
    }
    
    /**
     * This one has the complete class path in jars so everything is loaded through the MultiJarFileClassLoader and you
     * can see all the "Loaded class..." debug messages for things outside the mtwilson-version-1.2-SNAPSHOT.jar
     * 
     * @throws Exception
     */
    @Test
    public void testMultiJarFileClassLoader() throws Exception {
        // first we identify a plugin to load, and list its dependencies (and their locations on disk)
        MavenResolver resolver = new MavenResolver();
        File pluginJarFile = resolver.findJarFile("com.intel.mtwilson.plugins", "mtwilson-version", "1.2-SNAPSHOT");
        Manifest manifest = ModuleUtil.readManifest(pluginJarFile); // throws IOException
        Set<File> jars = resolver.resolveClasspath(manifest);
        jars.add(pluginJarFile);
        MultiJarFileClassLoader cl = new MultiJarFileClassLoader(jars.toArray(new File[jars.size()]));
        Class<?> pluginClass = cl.loadClass("com.intel.mtwilson.plugin.version.VersionPlugin"); // throws ClassNotFoundException
        Object versionPlugin = pluginClass.newInstance();  // throws InstantiationException, IllegalAccessException
        log.debug("Loaded version plugin: {}", versionPlugin.getClass().getName());
    }
        
    /**
     * The idea here is to load two jars in separate class loaders, instantiate a class from each, and try to 
     * pass an object from one to the other.  
     * 
     * If mtwilson-plugin-sample-convention defines the dependency on mtwilson-plugin-sample-annotation (MessageA) with
     * "compile" scope the test succeeds.
     * 
     * If mtwilson-plugin-sample-convention defines the dependency on mtwilson-plugin-sample-annotation (MessageA) with
     * "provided" scope the test fails. with no class definition found error on MessageA
     * By the way, the error happens in a very interesting spot: when we call getNoticeMethodForType(componentClass, messageClass),
     * that utility function looks at all methods of componentClass  (the SampleConventionalComponent class) - and 
     * as it's getting the list of methods  (java's built-in java.lang.Class.getDeclaredMethods0 native method) 
     * it throws the NoClassDefFoundError when it's trying to add notice(MessageA) to the list because it has to 
     * resolve MessageA and it can't because it's not on the classpath.  So this is where we come in with the
     * classloader fix (see next test).
     * 
     * @throws Exception 
     */
    @Test(expected=NoClassDefFoundError.class)
    public void testObjectSharingWithJarFileClassLoader() throws Exception {
        MavenResolver resolver = new MavenResolver();
        
        File pluginJarFile1 = resolver.findJarFile("com.intel.mtwilson.plugins", "mtwilson-plugin-sample-annotation", "1.2-SNAPSHOT");
        JarFileClassLoader cl1 = new JarFileClassLoader(pluginJarFile1);
        Class<?> messageClass = cl1.loadClass("com.intel.mtwilson.plugin.sample.MessageA");
        Object messageObject = messageClass.newInstance(); 
        log.debug("Loaded MessageA object: {}", messageObject.getClass().getName());
        
        File pluginJarFile2 = resolver.findJarFile("com.intel.mtwilson.plugins", "mtwilson-plugin-sample-convention", "1.2-SNAPSHOT");
        JarFileClassLoader cl2 = new JarFileClassLoader(pluginJarFile2);
        Class<?> componentClass = cl2.loadClass("com.intel.mtwilson.plugin.sample.SampleConventionalComponent"); 
        Object componentObject = componentClass.newInstance(); 
        log.debug("Loaded component object: {}", componentObject.getClass().getName());
        
        Method noticeMethod = ReflectionUtil.getNoticeMethodForType(componentClass, messageClass); // throws NoClassDefFoundError  when it tries to get methods of componentclass because one of them is notice(MessageA) and MessageA is not foudn by the component's clasloader
        noticeMethod.invoke(componentObject, messageObject);
        fail("should have thrown exception in getNoticeMethodForType, see explanation");
    }
    
    @Test
    public void testObjectSharingWithJarFileClassLoaderWithSimpleParent() throws Exception {
        MavenResolver resolver = new MavenResolver();
        
        File pluginJarFile1 = resolver.findJarFile("com.intel.mtwilson.plugins", "mtwilson-plugin-sample-annotation", "1.2-SNAPSHOT");
        JarFileClassLoader cl1 = new JarFileClassLoader(pluginJarFile1);
        Class<?> messageClass = cl1.loadClass("com.intel.mtwilson.plugin.sample.MessageA"); 
        Object messageObject = messageClass.newInstance(); 
        log.debug("Loaded MessageA object: {}", messageObject.getClass().getName());
        
        File pluginJarFile2 = resolver.findJarFile("com.intel.mtwilson.plugins", "mtwilson-plugin-sample-convention", "1.2-SNAPSHOT");
        JarFileClassLoader cl2 = new JarFileClassLoader(pluginJarFile2, cl1); //  <<<===================== So  just putting classloader 1 here as a parent works because when this one looks for Message A and doesn't find it, it delegates to parent that does know it
        Class<?> componentClass = cl2.loadClass("com.intel.mtwilson.plugin.sample.SampleConventionalComponent"); // throws ClassNotFoundException
        Object componentObject = componentClass.newInstance(); 
        log.debug("Loaded component object: {}", componentObject.getClass().getName());
        
        Method noticeMethod = ReflectionUtil.getNoticeMethodForType(componentClass, messageClass); // no error
        noticeMethod.invoke(componentObject, messageObject); // success: SampleConventionalComponent received MessageA notification: hello world (default message)
    }
    
    // given collection of files, returns space-separated list of names (not full paths)
    private String names(Collection<File> files) {
        return StringUtils.join(nameList(files), " ");
    }
    private List<String> nameList(Collection<File> files) {
        ArrayList<String> names = new ArrayList<String>();
        for(File file : files) {
            names.add(file.getName());
        }
        return names;
    }
    
    @Test
    public void testObjectSharingWithJarFileClassLoaderWithGraph() throws Exception {
        MavenResolver resolver = new MavenResolver();
        
        File pluginJarFile1 = resolver.findJarFile("com.intel.mtwilson.plugins", "mtwilson-plugin-sample-annotation", "1.2-SNAPSHOT");
        Manifest manifest1 = ModuleUtil.readManifest(pluginJarFile1); 
        Set<File> jars1 = resolver.resolveClasspath(manifest1);
        jars1.add(pluginJarFile1);
        log.debug("Classpath1(annotation): {}", names(jars1));
        
        File pluginJarFile2 = resolver.findJarFile("com.intel.mtwilson.plugins", "mtwilson-plugin-sample-convention", "1.2-SNAPSHOT");
        Manifest manifest2 = ModuleUtil.readManifest(pluginJarFile2);
        Set<File> jars2 = resolver.resolveClasspath(manifest2);
        jars2.add(pluginJarFile2);
        log.debug("Classpath2(convention): {}", names(jars2));
        
        // figure out which jars they have in common (baby step... later we need to consider semantic versioning)
        Collection commonJars = CollectionUtils.intersection(jars1, jars2);
        log.debug("Common jars: {}", names(commonJars));
        
        Collection differentJars = CollectionUtils.disjunction(jars1, jars2);
        log.debug("Disjunction jars: {}", names(differentJars));
        
        ////// BOOKMARK  /////  NEXT STEP: WRITE A CLASSLOADER THAT CAN HANDLE WHAT I NEED , DOCUMENTED IN WORD DOC, AND USE IT TO BE ABLE TO PASS MESSAGEA FROM -ANNOTATION TO -CONVENTION, AND ALSO HAVE THEM DEPEND ON ONE LIBRARY X DIFFERENT VERSIONS BUT CONSOLIDATE WITH SEMANTIC VERSIONING, AND ON A LIBRARY Y DIFFERENT SPECIFIED VERSIOSN SO WE'RE FORCED TO LOAD THEM SEPARATELY.... THE RULE PROBABLY HAS TO BE THAT IF YOU HAVE A SPECIFIC VERSION OF SOMETHING YOU'RE RELYING ON, YOU WILL NOT BE ABLE TOSHARE THOSE OBJECTS WITH ANYONE ELSE....  
        
        
        /*
        JarFileClassLoader cl1 = new JarFileClassLoader(pluginJarFile1);
        Class<?> messageClass = cl1.loadClass("com.intel.mtwilson.plugin.sample.MessageA"); 
        Object messageObject = messageClass.newInstance(); 
        log.debug("Loaded MessageA object: {}", messageObject.getClass().getName());
        
        JarFileClassLoader cl2 = new JarFileClassLoader(pluginJarFile2, cl1); //  <<<===================== So  just putting classloader 1 here as a parent works because when this one looks for Message A and doesn't find it, it delegates to parent that does know it
        Class<?> componentClass = cl2.loadClass("com.intel.mtwilson.plugin.sample.SampleConventionalComponent"); // throws ClassNotFoundException
        Object componentObject = componentClass.newInstance(); 
        log.debug("Loaded component object: {}", componentObject.getClass().getName());
        
        Method noticeMethod = ReflectionUtil.getNoticeMethodForType(componentClass, messageClass); // no error
        noticeMethod.invoke(componentObject, messageObject);
        */
    }
    
    
    /**
     * THIS ONE DOESN'T WORK AT ALL
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException 
     */
    /*
    @Test
    public void testDelegatingLoader() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        // first we identify a plugin to load, and list its dependencies (and their locations on disk)
        MavenResolver resolver = new MavenResolver();
        File pluginJarFile = resolver.findJarFile("com.intel.mtwilson.plugins", "mtwilson-version", "1.2-SNAPSHOT");
        Manifest manifest = ModuleUtil.readManifest(pluginJarFile); // throws IOException
        Set<File> jars = resolver.resolveClasspath(manifest);
        // second we create one JarFileClassLoader per dependency
        DelegatingClassLoader delegatingClassLoader = new DelegatingClassLoader();
        HashSet<ClassLoader> classLoaders = new HashSet<ClassLoader>();
        for(File jar : jars) {
            LimitedJarFileClassLoader classLoader = new LimitedJarFileClassLoader(jar, delegatingClassLoader);
            classLoaders.add(classLoader);
        }
        classLoaders.add(new LimitedJarFileClassLoader(pluginJarFile, delegatingClassLoader));
        delegatingClassLoader.children().addAll(classLoaders);
        Class<?> pluginClass = delegatingClassLoader.loadClass("com.intel.mtwilson.plugin.version.VersionPlugin"); // throws ClassNotFoundException
        Object versionPlugin = pluginClass.newInstance();  // throws InstantiationException, IllegalAccessException
        log.debug("Loaded version plugin: {}", versionPlugin.getClass().getName());
    }
    */

    
}
