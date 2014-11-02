/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.classpath;

import com.intel.dcsg.cpg.classpath.MavenResolver;
import com.intel.dcsg.cpg.classpath.MultiJarFileClassLoader;
import com.intel.dcsg.cpg.classpath.JarFileClassLoader;
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
        Manifest manifest = JarUtil.readManifest(pluginJarFile); // throws IOException
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
     * Sample output:
2013-12-27 14:19:35,726 DEBUG [main] c.i.d.c.c.ClassLoaderTest [ClassLoaderTest.java:120] Loaded component object: com.intel.mtwilson.plugin.sample.SampleConventionalComponent
2013-12-27 14:19:35,726 DEBUG [main] c.i.d.c.c.JarFileClassLoader [JarFileClassLoader.java:55] Cannot find class com.intel.mtwilson.plugin.sample.MessageA
(followed by NoClassDefFoundError)
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
        
        // XXX TODO  below code is commented out because ReflectionUtil is in cpg-module (this class was originally in that package)
        
        Method noticeMethod = getNoticeMethodForType(componentClass, messageClass); // throws NoClassDefFoundError  when it tries to get methods of componentclass because one of them is notice(MessageA) and MessageA is not foudn by the component's clasloader
        noticeMethod.invoke(componentObject, messageObject);
        fail("should have thrown exception in getNoticeMethodForType, see explanation");
    }
    
    
    /**
     * 
     * Sample output:
2013-12-27 14:21:06,066 DEBUG [main] c.i.m.p.s.SampleConventionalComponent [SampleConventionalComponent.java:53] SampleConventionalComponent received MessageA notification: hello world (default message)
     *
     * @throws Exception 
     */
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
        
        Method noticeMethod = getNoticeMethodForType(componentClass, messageClass); // no error
        noticeMethod.invoke(componentObject, messageObject); // success: SampleConventionalComponent received MessageA notification: hello world (default message)
        
    }
    
    @Test
    public void testObjectSharingWithJarFileClassLoaderWithGraph() throws Exception {
        MavenResolver resolver = new MavenResolver();
        
        File pluginJarFile1 = resolver.findJarFile("com.intel.mtwilson.plugins", "mtwilson-plugin-sample-annotation", "1.2-SNAPSHOT");
        Manifest manifest1 = JarUtil.readManifest(pluginJarFile1); 
        Set<File> jars1 = resolver.resolveClasspath(manifest1);
        jars1.add(pluginJarFile1);
        log.debug("Classpath1(annotation): {}", names(jars1));
        
        File pluginJarFile2 = resolver.findJarFile("com.intel.mtwilson.plugins", "mtwilson-plugin-sample-convention", "1.2-SNAPSHOT");
        Manifest manifest2 = JarUtil.readManifest(pluginJarFile2);
        Set<File> jars2 = resolver.resolveClasspath(manifest2);
        jars2.add(pluginJarFile2);
        log.debug("Classpath2(convention): {}", names(jars2));
        
        // figure out which jars they have in common (baby step... later we need to consider semantic versioning)
        Collection commonJars = CollectionUtils.intersection(jars1, jars2);
        log.debug("Common jars: {}", names(commonJars));
        
        Collection differentJars = CollectionUtils.disjunction(jars1, jars2);
        log.debug("Disjunction jars: {}", names(differentJars));
        
        
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
    
    
    // these two methods copied from cpg-module ReflectionUtil but modified not to use the module annotations for our testing purposes here
    private Method getNoticeMethodForType(Class<?> clazz, Class<?> arg) {
        Method[] methods =  clazz.getDeclaredMethods();                
        // look for a method matching the convention
        for(Method method : methods ) {
            if( isNoticeMethod(method) && method.getParameterTypes()[0].isAssignableFrom(arg) ) {
                return method;
            }
        }
        return null;
        
    }
    private boolean isNoticeMethod(Method method) {
        boolean conventional = method.getName().equals("notice");
        boolean oneArg = method.getParameterTypes().length == 1;
        boolean notPrimitive = oneArg && !method.getParameterTypes()[0].isPrimitive();
        boolean noReturn = method.getReturnType().getName().equals("void");
        return conventional && oneArg && notPrimitive && noReturn;
    }
    
}
