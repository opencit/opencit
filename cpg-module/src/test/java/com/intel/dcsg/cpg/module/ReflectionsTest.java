/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.module;

import com.intel.dcsg.cpg.module.JarComponentIterator;
import com.intel.dcsg.cpg.classpath.MavenResolver;
import com.intel.dcsg.cpg.classpath.JarFileClassLoader;
import com.intel.dcsg.cpg.module.annotations.Activate;
import com.intel.dcsg.cpg.module.annotations.Deactivate;
import com.intel.dcsg.cpg.module.annotations.Notice;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.junit.Test;
import org.junit.BeforeClass;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;
import static org.reflections.ReflectionUtils.*;

/**
 * Unit tests for using the Reflections library.  Also uses MavenResolver for conveniently accessing
 * a jar file to scan (the mtwilson-version plugin in local repository)
 * 
 * @author jbuhacoff
 */
public class ReflectionsTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ReflectionsTest.class);

    private static File jarfile;
    
    @BeforeClass
    public static void init() {
        // locate the jar file to test;  could put this in the @Test methods but wnated to be clear that this code is not under test:
        MavenResolver resolver = new MavenResolver();
        jarfile = resolver.findJarFile("com.intel.mtwilson.plugins", "mtwilson-version", "1.2-SNAPSHOT");
    }

    @Test
    public void testFindComponentsWithAnnotations() throws Exception {
        JarFileClassLoader cl = new JarFileClassLoader(jarfile);
        Reflections r = new ConfigurationBuilder().addUrls(jarfile.toURI().toURL()).addClassLoader(cl).build();
        Set<Class<?>> annotatedClasses = r.getTypesAnnotatedWith(com.intel.dcsg.cpg.module.annotations.Component.class, true);
        for(Class<?> c : annotatedClasses) {
            log.debug("Found component: {}", c.getName());
            // now check for Activate, Deactivate, Notify, Import/Required, Export annotations on methods in this class
            Set<Method> activateMethods = getAllMethods(c, withAnnotation(Activate.class));
            for(Method method : activateMethods) {
                log.debug("Found @Activate: {}", method.getName());
                // rules to enforce:  if a method has @Activate, then it should have 1) no arguments, 2) void return, 3) no @Deactivate annotation
                Class<?>[] parameters = method.getParameterTypes();
                if( parameters.length > 0 ) {
                    log.error("Methods annotated with @Activate must not have any parameters");
                }
                if(  method.getReturnType().getName().equals("void") ) {
                    log.error("Methods annotated with @Activate must not return any value; this one has return type {}", method.getReturnType().getName());
                }
                if( method.isAnnotationPresent(Deactivate.class)) {
                    log.error("Methods annotated with @Activate must not also be annotated with @Deactivate");
                }
            }
            // there can at most one @Activate method
            if( activateMethods.size() > 1 ) {
                log.error("There can only be one @Activate method");
            }
            Set<Method> deactivateMethods = getAllMethods(c, withAnnotation(Deactivate.class)); 
            for(Method method : deactivateMethods) {
                log.debug("Found @Deactivate: {}", method.getName());
                // rules to enforce:  if a method has @Activate, then it should have 1) no arguments, 2) void return, 3) no @Activate annotation
                Class<?>[] parameters = method.getParameterTypes();
                if( parameters.length > 0 ) {
                    log.error("Methods annotated with @Deactivate must not have any parameters");
                }
                if( method.getReturnType().getName().equals("void") ) {
                    log.error("Methods annotated with @Deactivate must not return any value; this one has return type {}", method.getReturnType().getName());
                }
                if( method.isAnnotationPresent(Activate.class)) {
                    log.error("Methods annotated with @Deactivate must not also be annotated with @Activate");
                }
            }
            // there can be at most one @Deactivate method
            if( deactivateMethods.size() > 1 ) {
                log.error("There can only be one @Deactivate method");
            }
            Set<Method> notifyMethods = getAllMethods(c, withAnnotation(Notice.class), withParametersAssignableTo(Object.class)); 
            for(Method method : notifyMethods) {
                log.debug("Found @Notice: {}", method.getName());
                Class<?>[] parameters = method.getParameterTypes();
                if( parameters.length > 0 ) {
                    log.error("Methods annotated with @Notice must not have any parameters");
                }
                if( method.getReturnType().getClass().equals(void.class) ) { // or  method.getReturnType().getName().equals("void")
                    log.error("Methods annotated with @Notice must not return any value; this one has return type {}", method.getReturnType().getName());
                }
            }
            
        }
        
    }

    @Test
    public void testFindComponentsByConvention() throws Exception {
        JarFileClassLoader cl = new JarFileClassLoader(jarfile);
        Reflections r = new ConfigurationBuilder().addUrls(jarfile.toURI().toURL()).addClassLoader(cl).build();
//        Set<Class<?>> conventionalClasses = r.
//        for(Class<?> c : conventionalClasses) {
//            log.debug("Found component: {}", c.getName());
//        }        
    }
    
    
    /**
     * Sample output:
2013-10-16 21:45:04,221 DEBUG [main] c.i.m.j.ReflectionsTest [ReflectionsTest.java:138] Jar entry: META-INF/
2013-10-16 21:45:04,221 DEBUG [main] c.i.m.j.ReflectionsTest [ReflectionsTest.java:138] Jar entry: META-INF/MANIFEST.MF
2013-10-16 21:45:04,222 DEBUG [main] c.i.m.j.ReflectionsTest [ReflectionsTest.java:138] Jar entry: com/
2013-10-16 21:45:04,222 DEBUG [main] c.i.m.j.ReflectionsTest [ReflectionsTest.java:138] Jar entry: com/intel/
2013-10-16 21:45:04,222 DEBUG [main] c.i.m.j.ReflectionsTest [ReflectionsTest.java:138] Jar entry: com/intel/mtwilson/
2013-10-16 21:45:04,223 DEBUG [main] c.i.m.j.ReflectionsTest [ReflectionsTest.java:138] Jar entry: com/intel/mtwilson/plugin/
2013-10-16 21:45:04,223 DEBUG [main] c.i.m.j.ReflectionsTest [ReflectionsTest.java:138] Jar entry: com/intel/mtwilson/plugin/version/
2013-10-16 21:45:04,224 DEBUG [main] c.i.m.j.ReflectionsTest [ReflectionsTest.java:138] Jar entry: com/intel/mtwilson/plugin/version/TestComponent.class
2013-10-16 21:45:04,224 DEBUG [main] c.i.m.j.ReflectionsTest [ReflectionsTest.java:138] Jar entry: com/intel/mtwilson/plugin/version/TestComponentWithAnnotations.class
2013-10-16 21:45:04,225 DEBUG [main] c.i.m.j.ReflectionsTest [ReflectionsTest.java:138] Jar entry: com/intel/mtwilson/plugin/version/VersionPlugin.class
2013-10-16 21:45:04,225 DEBUG [main] c.i.m.j.ReflectionsTest [ReflectionsTest.java:138] Jar entry: META-INF/maven/
2013-10-16 21:45:04,226 DEBUG [main] c.i.m.j.ReflectionsTest [ReflectionsTest.java:138] Jar entry: META-INF/maven/com.intel.mtwilson.plugins/
2013-10-16 21:45:04,226 DEBUG [main] c.i.m.j.ReflectionsTest [ReflectionsTest.java:138] Jar entry: META-INF/maven/com.intel.mtwilson.plugins/mtwilson-version/
2013-10-16 21:45:04,226 DEBUG [main] c.i.m.j.ReflectionsTest [ReflectionsTest.java:138] Jar entry: META-INF/maven/com.intel.mtwilson.plugins/mtwilson-version/pom.xml
2013-10-16 21:45:04,227 DEBUG [main] c.i.m.j.ReflectionsTest [ReflectionsTest.java:138] Jar entry: META-INF/maven/com.intel.mtwilson.plugins/mtwilson-version/pom.properties
     * 
     * @throws Exception 
     */
    @Test
    public void findAllClassesInJar() throws Exception {
        JarFileClassLoader cl = new JarFileClassLoader(jarfile);
        JarFile jar = new JarFile(jarfile);
        Enumeration<JarEntry> jarEntries = jar.entries();
        while(jarEntries.hasMoreElements()) {
            JarEntry jarEntry = jarEntries.nextElement();
            log.debug("Jar entry: {}", jarEntry.getName());
            if( jarEntry.getName().endsWith("Component.class") ) {
                log.debug("Found possible Component class");
                // now check for activate() and deactivate()
                Class<?> candidate = cl.loadClass(jarEntry.getName().replace("/", ".").substring(0, jarEntry.getName().length()-6 /* length(.class)==6 */));
                // look for no-arg constructor
                boolean noArgConstructor = false;
                Constructor<?>[] constructors = candidate.getConstructors();
                for(Constructor<?> constructor : constructors) {
                    if( constructor.getParameterTypes().length == 0 ) {
                        log.debug("Found no-arg constructor");
                        noArgConstructor = true;
                    }
                }
                Method[] methods =  candidate.getDeclaredMethods();                
                // look for activation method
                Method activate = null;
                for(Method method : methods) {
//                    log.debug("Method name: {}", method.getName());
//                    log.debug("Method parameter types length: {}", method.getParameterTypes().length);
//                    log.debug("Method return type: {}", method.getReturnType().getName());
                    if( method.getName().equals("activate") && method.getParameterTypes().length == 0 && method.getReturnType().getName().equals("void") ) {
                        activate = method;
                    }
                }
                if( activate == null ) {
                    log.debug("Did not find an activation method");
                }
                else {
                    log.debug("Found void activate()");
                }
                // look for deactivation method
                Method deactivate = null;
                for(Method method : methods) {
                    if( method.getName().equals("deactivate") && method.getParameterTypes().length == 0 && method.getReturnType().getName().equals("void") ) {
                        deactivate = method;
                    }
                }
                if( deactivate == null ) {
                    log.debug("Did not find a deactivation method");
                }
                else {
                    log.debug("Found void deactivate()");
                }            
                // is the class qualified as a component?
                if( noArgConstructor && activate != null && deactivate != null ) {
                    log.debug("Identified Component: {}", candidate.getName());
                }
            }
        }
        jar.close();
    }

    @Test
    public void findAllComponentsInJar() throws Exception {
        JarFileClassLoader cl = new JarFileClassLoader(jarfile);
        JarComponentIterator it = new JarComponentIterator(jarfile, cl);
        while(it.hasNext()) {
            Class<?> componentClass = it.next();
            log.debug("Found component: {}", componentClass.getName());
        }
    }
    
}

