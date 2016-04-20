/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.test;

import com.intel.dcsg.cpg.classpath.ClassLoadingStrategy;
import com.intel.dcsg.cpg.classpath.FencedClassLoadingStrategy;
import com.intel.dcsg.cpg.classpath.JarUtil;
import com.intel.dcsg.cpg.classpath.MavenResolver;
import com.intel.dcsg.cpg.classpath.UnitedClassLoadingStrategy;
import com.intel.dcsg.cpg.extensions.AnnotationRegistrar;
import com.intel.dcsg.cpg.extensions.ExtensionUtil;
import com.intel.dcsg.cpg.extensions.ImplementationRegistrar;
import com.intel.dcsg.cpg.module.Container;
import com.intel.dcsg.cpg.classpath.JarClassIterator;
import com.intel.dcsg.cpg.module.Module;
import com.intel.dcsg.cpg.module.ModuleUtil;
import com.intel.mtwilson.launcher.ExtensionLauncher;
import com.intel.mtwilson.launcher.ext.Initialize;
import com.intel.mtwilson.launcher.ext.Configure;
import com.intel.mtwilson.launcher.ext.Validate;
import com.intel.mtwilson.launcher.ext.Start;
import com.intel.mtwilson.launcher.ext.Stop;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.jar.Manifest;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.util.Factory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.reflections.Reflections;

/**
 * @author jbuhacoff
 */
public class JunitMavenLauncher extends ExtensionLauncher {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JunitMavenLauncher.class);
    private static JunitMavenLauncher launcher;
    
    private final MavenResolver resolver = new MavenResolver();
    private final Properties configuration;
    private final HashSet<String> moduleNames = new HashSet<String>();
    private final Set<Module> modules = new HashSet<Module>();
    private File targetDirectory;
    private File mavenRepositoryDirectory;
//    private Container container = new Container();
//    private boolean continueEventLoop = true;
    private ClassLoadingStrategy classLoadingStrategy = new FencedClassLoadingStrategy(); // a reasonable default until we get semantic versioning working

    public JunitMavenLauncher() {
        this(new Properties());
    }

    public JunitMavenLauncher(Properties configuration) {
        this.configuration = configuration;
    }

    public ClassLoadingStrategy getClassLoadingStrategy() {
        return classLoadingStrategy;
    }

    public void setClassLoadingStrategy(ClassLoadingStrategy classLoadingStrategy) {
        this.classLoadingStrategy = classLoadingStrategy;
    }

    
    public Set<String> getModuleNames() {
        return moduleNames;
    }
    
    public Properties getProperties() {
        return configuration;
    }
    
    /**
     * 
     * @param moduleName in the maven form groupId:artifactId:version
     */
    public void addModule(String moduleName) {
        log.debug("Adding module: {}", moduleName);
        moduleNames.add(moduleName);
    }
    
    public void addModule(String groupId, String artifactId, String version) {
        addModule(String.format("%s:%s:%s", groupId, artifactId, version));
    }

    private void initializeEnvironment() {
        // maybe this default directory code should be inside MavenModuleRepository ? or inside MavenResolver ?
        String defaultLocalRepository = System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository";
        mavenRepositoryDirectory = new File(configuration.getProperty("localRepository", defaultLocalRepository));
        if (!mavenRepositoryDirectory.exists()) {
            throw new IllegalStateException("Missing maven repository");
        }
        targetDirectory = new File("." + File.separator + "target" + File.separator + "jmod");
        if (!targetDirectory.exists()) {
            targetDirectory.mkdirs();
        }
        
    }
    
    // XXX TODO  similar code here and in DirectoryLauncher and MavenLauncher
    public void loadModules() throws IOException {
        for (String moduleName : moduleNames) {
            log.debug("Loading module: {}", moduleName);
            File moduleJarFile = locateModuleJarFile(moduleName);
            if (ModuleUtil.isModule(moduleJarFile)) {
                Manifest manifest = JarUtil.readManifest(moduleJarFile);
                //                Set<File> classpath = resolver.resolveClasspath(module.getManifest()); // XXX TODO  need to make the class loader strategy aware of the resolver?? should be a DirectoryResolver and a MavenResolver ... each needs to know the jar File and Manifest (for classpath or maven-classpath)
                Module module = new Module(moduleJarFile, manifest, classLoadingStrategy.getClassLoader(moduleJarFile, manifest, resolver));
                log.debug("Module: {}", module.getImplementationTitle() + "-" + module.getImplementationVersion());
                log.debug("Class-Path: {}", (Object[])module.getClasspath());
                log.debug("Module-Components: {}", (Object[])module.getComponentNames());
                // before we try to activate the module, make sure that all its dependencies are present and if not, XXX TODO try to download them automatically
                Collection<String> missingArtifacts = resolver.listMissingArtifacts(manifest);
                // if any are missing we quit
                if (missingArtifacts.isEmpty()) {
                    log.debug("Classpath ok, registering module");
//                    container.register(module);
                    modules.add(module);
                } else {
                    log.warn("Module {} is missing {} jars from classpath", module.getImplementationTitle(), missingArtifacts.size());
                }

            }
        }
//        log.debug("Found {} modules", container.getModules().size());
        log.debug("Found {} modules", modules.size());
    }
    
    private File locateModuleJarFile(String moduleName) {
        String[] parts = moduleName.split(":"); // should be exactly 3 :  groupId, artifactId, version 
        File moduleJarFile = resolver.findJarFile(parts[0], parts[1], parts[2]);
        if (!moduleJarFile.exists()) {
            log.error("Cannot find module: {}", moduleName);
        }
        return moduleJarFile;
    }
    
    private void registerExtensions() throws IOException {
        // XXX TODO  this should really be somewhere else... but right now Module is tightly coupled to the Component design in cpg-module , and it has
        // a component search function but it uses component-specific interfaces with JarComponentIterator.
        ImplementationRegistrar registrar = new ImplementationRegistrar();
        for(Module module : modules) {
            JarClassIterator it = new JarClassIterator(module.getJarFile(), module.getClassLoader());
            while(it.hasNext()) {
                Class<?> clazz = it.next();
                log.debug("Scanning class for extensions {}", clazz.getName());
                ExtensionUtil.scan(registrar, clazz); // the whiteboard will include EVERYTHING that implements any interface 
            }
        }
        
        
        /*
        Reflections reflections = new Reflections(); // not specifying package name "my.project.prefix" 
        Class<?>[] lifecycle = new Class<?>[] { Initialize.class, Configure.class, Validate.class, Startup.class, Shutdown.class };
        for(Class<?> phase : lifecycle) {
        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith((Class<? extends Annotation>)phase);
        AnnotationRegistrar registrar = new AnnotationRegistrar((Class<? extends Annotation>)phase);
        ExtensionUtil.scan(registrar, annotated);
            
        }
        */
        /*
        Reflections reflections = new Reflections(); // not specifying package name "my.project.prefix" 
        Set<Class<?>> phaseExtensions = new HashSet<Class<?>>();
        phaseExtensions.addAll(reflections.getSubTypesOf(Initialize.class));
        phaseExtensions.addAll(reflections.getSubTypesOf(Configure.class));
        phaseExtensions.addAll(reflections.getSubTypesOf(Validate.class));
        phaseExtensions.addAll(reflections.getSubTypesOf(Start.class));
        phaseExtensions.addAll(reflections.getSubTypesOf(Stop.class));
        
        ImplementationRegistrar registrar = new ImplementationRegistrar();
        ExtensionUtil.scan(registrar, phaseExtensions);
* */    }
    
    /**
     * Default implementation is empty;  override to add modules using
     * addModule().  No need to call super.modules() because it would do
     * nothing.
     */
//    protected abstract void modules(); { log.debug("DEFAULT MODULES FUNCTION CALLED"); }
    
    /*
    @BeforeClass
    public static void startup() throws IOException {
        // first we need to initialize our extensions registry by scanning the classpath for all the launcher extensions...
        launcher.initializeEnvironment();
        launcher.modules(); // add any modules that should be included in the test
        launcher.loadModules();
        launcher.registerExtensions(); // throws IOException
        launcher.initialize();
        launcher.configure();
        launcher.validate();
        launcher.start();
    }

    @AfterClass
    public static void shutdown() {
        launcher.stop();
    }*/
    
//    private boolean init = false;
    
    private void initializeShiro() {
        Factory<org.apache.shiro.mgt.SecurityManager> factory = new IniSecurityManagerFactory("classpath:shiro.ini");
        org.apache.shiro.mgt.SecurityManager securityManager = factory.getInstance();
        SecurityUtils.setSecurityManager(securityManager); // sets a single shiro security manager to be used for entire jvm... fine for a stand-alone app but when running inside a web app container or in a multi-user env. it needs to be maintained by some container and set on every thread that will do work ...         
    }
    
    @BeforeClass
    public static void startup() {
        log.debug("BeforeClass startup, is there an instance set? {}", launcher);
    }
    // from junit @BeforeClass, instantiate JunitMavenLauncher and call addModule() once for each module to add before calling startup()
    @Before
    public void startupInstance() throws IOException {
        if( launcher == null ) {
            log.debug("Before first test, initializing instance");
//            init = true;
            launcher = this;
            initializeEnvironment();
            loadModules();
            registerExtensions();
//            initialize();
            initializeShiro(); // XXX TODO this is here only for junit testing as a custom method to initialize using shiro.ini ; in  production this needs to be a plugin, maybe mtwilson-shiro-jdbc which relies on My.configuration() to get the encrypted/integrity-protected shiro.ini from the database and uses that...
//            configure();
//            validate();
//            start();
        }
    }
    
    @AfterClass
    public static void shutdown() {
        log.debug("AfterClass shutdown, stopping the current instance");
        launcher.stop();
        launcher = null;
    }
    
}
