/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.launcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;
import com.intel.dcsg.cpg.performance.AlarmClock;
import com.intel.dcsg.cpg.util.ArrayIterator;
import com.intel.dcsg.cpg.module.Container;
import com.intel.dcsg.cpg.classpath.MavenResolver;
import com.intel.dcsg.cpg.module.Module;
import com.intel.dcsg.cpg.module.ModuleRepository;
import com.intel.dcsg.cpg.module.ModuleUtil;
import com.intel.dcsg.cpg.classpath.ClassLoadingStrategy;
import com.intel.dcsg.cpg.classpath.FencedClassLoadingStrategy;
import com.intel.dcsg.cpg.classpath.JarUtil;
import com.intel.dcsg.cpg.classpath.MultiJarFileClassLoader;
import com.intel.dcsg.cpg.module.ContainerException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.jar.Manifest;

/**
 * Given some names of artifacts in the local maven repository, this launcher starts a Container and activates the
 * dependencies directly from the maven repository without copying them to any temporary directory.
 *
 * This launcher is more convenient to use in junit tests together with netbeans and maven because each test can define
 * exactly which modules should be involved. Unlike the DirectoryLauncher, this launcher does not automatically scan any
 * location to detect modules because it's assumed that the repository is large and that not all available modules
 * should be started (this assumption makes it easier to create focused junit tests)
 *
 * It helps to define modules as groupId:artifactId:version when using this launcher, instead of artifactId-version.jar
 * as with the DirectoryLauncher.
 *
 * Example of launching a container with 2 modules: MavenLauncher launcher = new MavenLauncher();
 * launcher.getModuleNames().add("com.intel.mtwilson.plugins:mtwilson-version:1.2-SNAPSHOT");
 * launcher.getModuleNames().add("com.intel.mtwilson.plugins:mtwilson-status:1.2-SNAPSHOT"); launcher.launch();
 *
 *
 * System properties: mtwilson.jmod.dir=/path/to/jar/directory (needed to load modules from a directory)
 * localRepository=~/.m2/repository (only needed if you use the MavenResolver)
 *
 * @author jbuhacoff
 */
public class MavenLauncher {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MavenLauncher.class);
    private final MavenResolver resolver = new MavenResolver();
    private final Properties configuration;
    private final HashSet<String> moduleNames = new HashSet<String>();
    private File targetDirectory;
    private File mavenRepositoryDirectory;
    private Container container = new Container();
    private boolean continueEventLoop = true;
    private ClassLoadingStrategy classLoadingStrategy = new FencedClassLoadingStrategy(); // a reasonable default until we get semantic versioning working

    public MavenLauncher() {
        this(new Properties());
    }

    public MavenLauncher(Properties configuration) {
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

    public Container getContainer() {
        return container;
    }

    public void setContainer(Container container) {
        this.container = container;
    }
    
    

    public void init() {
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

    private File locateModuleJarFile(String moduleName) {
        String[] parts = moduleName.split(":"); // should be exactly 3 :  groupId, artifactId, version 
        File moduleJarFile = resolver.findJarFile(parts[0], parts[1], parts[2]);
        if (!moduleJarFile.exists()) {
            log.error("Cannot find module: {}", moduleName);
        }
        return moduleJarFile;
    }


    /**
     * Initialize everything but do NOT start the event loop (caller must start it and stop it as needed)
     */
    public void launch() throws IOException, ContainerException {
        init();
        
        loadModules();

        // add a shutdown hook so we can automatically shut down the container if the VM is exiting
        addShutdownHook();

        // now start all modules we loaded and registered with the container
        container.start();

        // now list the registered modules
        log.debug("There are {} registered modules", container.getModules().size());
        for (Module module : container.getModules()) {
            log.debug("Module: {};active={}", module.getImplementationTitle() + "-" + module.getImplementationVersion(), (module.isActive() ? "yes" : "no"));
        }

    }
    
    public void loadModules() throws IOException {
        for (String moduleName : moduleNames) {
            File moduleJarFile = locateModuleJarFile(moduleName);
            if (ModuleUtil.isModule(moduleJarFile)) {
                Manifest manifest = JarUtil.readManifest(moduleJarFile);
                //                Set<File> classpath = resolver.resolveClasspath(module.getManifest()); 
                Module module = new Module(moduleJarFile, manifest, classLoadingStrategy.getClassLoader(moduleJarFile, manifest, resolver));
                log.debug("Module: {}", module.getImplementationTitle() + "-" + module.getImplementationVersion());
                log.debug("Class-Path: {}", (Object[])module.getClasspath());
                log.debug("Module-Components: {}", (Object[])module.getComponentNames());
                // before we try to activate the module, make sure that all its dependencies are present and if not try to download them automatically
                Collection<String> missingArtifacts = resolver.listMissingArtifacts(manifest);
                // if any are missing we quit
                if (missingArtifacts.isEmpty()) {
                    log.debug("Classpath ok, registering module");
                    container.register(module);
                } else {
                    log.warn("Module {} is missing {} jars from classpath", module.getImplementationTitle(), missingArtifacts.size());
                }

            }
        }
        log.debug("Found {} modules", container.getModules().size());
    }
    

    /**
     * Note: this method never returns! Call stopEventLoop() from another thread to terminate.
     */
    public void startEventLoop() {
        AlarmClock alarm = new AlarmClock(1, TimeUnit.SECONDS);
        while (continueEventLoop) {
            try {
                alarm.sleep();
            } catch (Exception e) {
                log.trace("Interrupted sleep", e);
            }
        }
    }

    public void stopEventLoop() {
        continueEventLoop = false;
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread("MtWilson Shutdown Hook") {
            @Override
            public void run() {
                try {
                    if (container != null) {
                        log.debug("Waiting for modules to deactivate");
                        container.stop();
                    }
                } catch (Exception ex) {
                    System.err.println("Error stopping container: " + ex);
                }
            }
        });
    }
    
    /**
     * When setting up a test environment you can write: launcher.module("groupId:artifactId:version") to add another
     * module to the set that will be loaded.
     *
     * @param mavenGroupIdArtifactIdVersion
     */
    public void module(String mavenGroupIdArtifactIdVersion) {
        moduleNames.add(mavenGroupIdArtifactIdVersion);
    }

    /**
     * When setting up a test environment you can write:
     * launcher.module().groupId("groupId").artifactId("artifactId").version("version").add(); to add another module to
     * the set that will be loaded.
     *
     * @param mavenGroupIdArtifactIdVersion
     */
    public MavenArtifactBuilder module() {
        return new MavenArtifactBuilder(this);
    }

    public static class MavenArtifactBuilder {

        private MavenLauncher launcher;
        private String groupId, artifactId, version;

        protected MavenArtifactBuilder(MavenLauncher launcher) {
            this.launcher = launcher;
        }

        public MavenArtifactBuilder groupId(String groupId) {
            this.groupId = groupId;
            return this;
        }

        public MavenArtifactBuilder artifactId(String artifactId) {
            this.artifactId = artifactId;
            return this;
        }

        public MavenArtifactBuilder version(String version) {
            this.version = version;
            return this;
        }

        public void add() {
            launcher.module(groupId + ":" + artifactId + ":" + version);
        }
    }
}
