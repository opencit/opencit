/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.launcher;

import com.intel.dcsg.cpg.io.file.DirectoryFilter;
import com.intel.dcsg.cpg.performance.AlarmClock;
import com.intel.dcsg.cpg.util.ArrayIterator;
import com.intel.dcsg.cpg.util.BreadthFirstTreeIterator;
import com.intel.dcsg.cpg.util.FileTree;
import com.intel.dcsg.cpg.util.Filter;
import com.intel.dcsg.cpg.module.Container;
import com.intel.dcsg.cpg.module.Module;
import com.intel.dcsg.cpg.module.ModuleRepository;
import com.intel.dcsg.cpg.module.ModuleUtil;
import com.intel.dcsg.cpg.classpath.ClassLoadingStrategy;
import com.intel.dcsg.cpg.classpath.DirectoryResolver;
import com.intel.dcsg.cpg.classpath.FencedClassLoadingStrategy;
import com.intel.dcsg.cpg.classpath.JarUtil;
import com.intel.dcsg.cpg.io.file.FilenameEndsWithFilter;
import com.intel.dcsg.cpg.module.ContainerException;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.jar.Manifest;

/**
 * Given a directory containing some jar files of which at least one is a Module, this launcher starts the container and
 * activates all the modules in the directory.
 *
 * The requirements on a container are that it have these methods: start() stop() register(Module)
 *
 * @author jbuhacoff
 */
public class ModuleDirectoryLauncher {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ModuleDirectoryLauncher.class);
    private static final FilenameEndsWithFilter jarfilter = new FilenameEndsWithFilter(".jar");
    private Container container = new Container();
    private ClassLoadingStrategy classLoadingStrategy = new FencedClassLoadingStrategy(); // a reasonable default until we get semantic versioning working

    /**
     * Where the module jar files and their dependencies are stored, for example /opt/mtwilson/java
     */
    private File directory;
    private boolean continueEventLoop = true;

    public File getDirectory() {
        return directory;
    }

    public void setDirectory(File directory) {
        this.directory = directory;
    }

    public Container getContainer() {
        return container;
    }

    public void setContainer(Container container) {
        this.container = container;
    }

    public ClassLoadingStrategy getClassLoadingStrategy() {
        return classLoadingStrategy;
    }

    public void setClassLoadingStrategy(ClassLoadingStrategy classLoadingStrategy) {
        this.classLoadingStrategy = classLoadingStrategy;
    }

    /**
     * Initialize everything but do NOT start the event loop (caller must start it and stop it as needed)
     */
    public void launch() throws IOException, ContainerException {
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

    public void loadModules() throws IOException {
        DirectoryResolver resolver = new DirectoryResolver(directory);
//        JarFileIterator it = new JarFileIterator(directory); // scans directory and its subdirectories for jar files
        Iterator<File> it = new ArrayIterator<File>(directory.listFiles(jarfilter)); // only scans directory for jar files; does NOT scan subdirectories
        while (it.hasNext()) {
            File jar = it.next();
            if (ModuleUtil.isModule(jar)) {
                Manifest manifest = JarUtil.readManifest(jar);
                Module module = new Module(jar, manifest, classLoadingStrategy.getClassLoader(jar, manifest, resolver));
                log.debug("Module: {}", module.getImplementationTitle() + "-" + module.getImplementationVersion());
                log.debug("Class-Path: {}", (Object)module.getClasspath());
                log.debug("Module-Components: {}", (Object)module.getComponentNames());
                // before we try to activate the module, make sure that all its dependencies are present and if not try to download them automatically
                List<String> missingArtifacts = listMissingArtifacts(module);
                // if any are missing we gquit
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
    

    public List<String> listMissingArtifacts(Module module) {
        ArrayList<String> missing = new ArrayList<String>();
        String[] jars = module.getClasspath();
        for (String jar : jars) {
            if (!contains(jar)) {
                missing.add(jar);
                log.debug("Repository missing jar: {}", jar);
            }

        }
        return missing;
    }

    public boolean contains(String artifact) {
        // in java7 this would be:
        // return directory.toPath().resolve(artifact).toFile().exists();
        File file = new File(directory.getAbsolutePath(), artifact);
        return file.exists();
    }

    // this inner class scans the directory and any subdirectories
    // for jar files ... 
    // if you need a non-recursive list of jars you can use simply:
    // Iterator<File> it = new ArrayIterator<File>( directory.listFiles(jarfilter) );
    public static class RecursiveJarFileIterator implements Iterator<File> {

        private final FileTree tree = new FileTree();
        private final Iterator<File> folders; // iterates over all subdirectories (including directory itself)
        private Iterator<File> files = null; // iterates over files in current folder ; when it runs out we need to move on to next folder

        public RecursiveJarFileIterator(File directory) {
            folders = new BreadthFirstTreeIterator<File>(tree, directory, new DirectoryFilter());
        }

        @Override
        public boolean hasNext() {
            if (files != null && files.hasNext()) {
                return true;
            }
            while (folders.hasNext()) {
                File nextFolder = folders.next();
                files = new ArrayIterator<File>(nextFolder.listFiles(jarfilter));
                if (files.hasNext()) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public File next() {
            return files.next();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException(); //"Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }


}
