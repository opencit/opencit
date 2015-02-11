/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.classpath;

import com.intel.dcsg.cpg.io.file.DirectoryFilter;
import com.intel.mtwilson.tree.DepthFirstTreeIterator;
import com.intel.mtwilson.tree.FileTree;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.Manifest;

/**
 * When our jar directory is missing an artifact, we can use a resolver like MavenResolver to automatically find the
 * missing artifact and copy it to the jar directory.
 *
 * References:
 * Maven source code  http://grepcode.com/snapshot/repo1.maven.org/maven2/org.apache.maven/maven-model/3.0.2
 * 
 * @author jbuhacoff
 */
public class MavenResolver implements FileResolver {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MavenResolver.class);
    private File directory = getDirectory();
    
    public MavenResolver() {
        if (!directory.exists()) {
            log.error("Maven repository is not found in {}", directory.getAbsolutePath());
        }        
    }
    
    /**
     * given a Manifest from a module, returns an array of File objects where each required jar can be found...
     * this list does NOT include the jar file that contains the maniefst itself.
     * 
     * if any files must be downloaded from remote sites, that must happen during resolution so we can return
     * a File object pointing to local storage. 
     * 
     * XXX TODO it might also be useful to define another interface that returns URLs instead of Files so
     * jars can be downloaded and stored in memory directly instead of saving on disk;  ... wishlist item.
     * 
     * XXX TODO the Class-Path in MANIFEST.MF is just artifact names... so it really slows down the test
     * to have to search the m2 repository for each one of those. if we configure maven to add the path
     * then it will need extra processing for directory-based deployment, but much easier because it just has to strip off the path, a 
     * string operation. 
     * 
     * When bootstrapping a container using artifacts from the maven repository, those artifacts should include
     * the following properties in their MANIFEST.MF:
     * Maven-ArtifactId: ${project.artifactId}
     * Maven-GroupId: ${project.groupId}
     * Maven-Version: ${project.version}
     * 
     * XXX to solve the mismatch (manifest class-path contains only artifactId-version.jar and we need groupId too to
     * find it efficiently) we can look for the other required headers:
Implementation-Title: ${project.name}
Implementation-Version: ${project.version}
Implementation-Vendor-Id: ${project.groupId}
Implementation-Vendor: ${project.organization.name}
     * so we have groupId and version,  
     * 
     * 
     * Sometimes the maven-dependency-plugin sneaks in a full path to a target folder of another module instead of
     * getting its path relative to the maven repository:
     * FOr example  notice the C:/Users/...  below that is unlike the other artifacts with relative path ./org/... or ./com/..
     * 
 
Maven-Classpath: ./org/restlet/jse/org.restlet/2.2-M4/org.restlet-2.2-
 M4.jar:./org/restlet/jse/org.restlet.ext.slf4j/2.2-M4/org.restlet.ext
 .slf4j-2.2-M4.jar:C:/Users/jbuhacof/workspace/mtwilson-dev/plugins/mt
 wilson-restlet/target/mtwilson-restlet-1.2-SNAPSHOT.jar:./org/eclipse
 /jetty/jetty-server/8.1.5.v20120716/jetty-server-8.1.5.v20120716.jar:
 ./org/eclipse/jetty/jetty-ajp/8.1.5.v20120716/jetty-ajp-8.1.5.v201207
 16.jar:./ch/qos/logback/lo ....
 * 
     * Which causes:
15:22:33.506 [main] DEBUG c.i.mtwilson.jmod.cl.MavenResolver - Resolving target jar C:\Users\jbuhacof\.m2\repository\com\intel\mtwilson\plugins\mtwilson-version\1.2-SNAPSHOT\mtwilson-version-1.2-SNAPSHOT.jar
15:22:33.508 [main] DEBUG c.i.mtwilson.jmod.cl.MavenResolver - Artifact: C:\Users\jbuhacof\.m2\repository\org\restlet\jse\org.restlet\2.2-M4\org.restlet-2.2-M4.jar
15:22:33.508 [main] DEBUG c.i.mtwilson.jmod.cl.MavenResolver - Artifact: C:\Users\jbuhacof\.m2\repository\org\restlet\jse\org.restlet.ext.slf4j\2.2-M4\org.restlet.ext.slf4j-2.2-M4.jar
15:22:33.509 [main] WARN  c.i.mtwilson.jmod.cl.MavenResolver - Artifact with absolute path: C
15:22:33.510 [main] ERROR c.i.mtwilson.jmod.cl.MavenResolver - Missing dependency: C
15:22:33.510 [main] WARN  c.i.mtwilson.jmod.cl.MavenResolver - Artifact with absolute path: /Users/jbuhacof/workspace/mtwilson-dev/plugins/mtwilson-restlet/target/mtwilson-restlet-1.2-SNAPSHOT.jar
15:22:33.510 [main] DEBUG c.i.mtwilson.jmod.cl.MavenResolver - Artifact: C:\Users\jbuhacof\.m2\repository\org\eclipse\jetty\jetty-server\8.1.5.v20120716\jetty-server-8.1.5.v20120716.jar
15:22:33.510 [main] DEBUG c.i.mtwilson.jmod.cl.MavenResolver - Artifact: C:\Users\jbuhacof\.m2\repository\org\eclipse\jetty\jetty-ajp\8.1.5.v20120716\jetty-ajp-8.1.5.v20120716.jar
15:22:33.511 [main] DEBUG c.i.mtwilson.jmod.cl.MavenResolver - Artifact: C:\Users\jbuhacof\.m2\repository\ch\qos\logback\logback-classic\1.0.9\logback-classic-1.0.9.jar
15:22:33.511 [main] DEBUG c.i.mtwilson.jmod.cl.MavenResolver - Artifact: C:\Users\jbuhacof\.m2\repository\org\slf4j\slf4j-api\1.6.4\slf4j-api-1.6.4.jar
15:22:33.511 [main] DEBUG c.i.mtwilson.jmod.cl.MavenResolver - Artifact: C:\Users\jbuhacof\.m2\repository\org\restlet\jse\org.restlet.ext.jetty\2.2-M4\org.restlet.ext.jetty-2.2-M4.jar
15:22:33.512 [main] DEBUG c.i.mtwilson.jmod.cl.MavenResolver - Artifact: C:\Users\jbuhacof\.m2\repository\ch\qos\logback\logback-core\1.0.9\logback-core-1.0.9.jar
15:22:33.512 [main] DEBUG c.i.mtwilson.jmod.cl.MavenResolver - Artifact: C:\Users\jbuhacof\.m2\repository\org\codehaus\woodstox\stax2-api\3.1.1\stax2-api-3.1.1.jar

15:22:33.543 [main] DEBUG c.i.mtwilson.launcher.MavenLauncher - Jmod-Components: 
15:22:33.543 [main] DEBUG c.i.mtwilson.jmod.cl.MavenResolver - Artifact: C:\Users\jbuhacof\.m2\repository\org\restlet\jse\org.restlet\2.2-M4\org.restlet-2.2-M4.jar
15:22:33.544 [main] DEBUG c.i.mtwilson.jmod.cl.MavenResolver - Artifact: C:\Users\jbuhacof\.m2\repository\org\restlet\jse\org.restlet.ext.slf4j\2.2-M4\org.restlet.ext.slf4j-2.2-M4.jar
15:22:33.544 [main] DEBUG c.i.mtwilson.jmod.cl.MavenResolver - Artifact: C:\Users\jbuhacof\.m2\repository
15:22:33.544 [main] DEBUG c.i.mtwilson.jmod.cl.MavenResolver - Artifact: C:\Users\jbuhacof\.m2\repositoryUsers\jbuhacof\workspace\mtwilson-dev\plugins\mtwilson-restlet\target\mtwilson-restlet-1.2-SNAPSHOT.jar
15:22:33.544 [main] ERROR c.i.mtwilson.jmod.cl.MavenResolver - Missing dependency: C:\Users\jbuhacof\.m2\repositoryUsers\jbuhacof\workspace\mtwilson-dev\plugins\mtwilson-restlet\target\mtwilson-restlet-1.2-SNAPSHOT.jar
15:22:33.544 [main] DEBUG c.i.mtwilson.jmod.cl.MavenResolver - Artifact: C:\Users\jbuhacof\.m2\repository\org\eclipse\jetty\jetty-server\8.1.5.v20120716\jetty-server-8.1.5.v20120716.jar
15:22:33.545 [main] DEBUG c.i.mtwilson.jmod.cl.MavenResolver - Artifact: C:\Users\jbuhacof\.m2\repository\org\eclipse\jetty\jetty-ajp\8.1.5.v20120716\jetty-ajp-8.1.5.v20120716.jar

* 
* 
     * 
     */
    @Override
    public Set<File> resolveClasspath(Manifest manifest) {
        log.debug("Resolving classpath in manifest");
        HashSet<File> files = new HashSet<>();
        // how to locate the module that contains this manifest:
//        String groupId = manifest.getMainAttributes().getValue("Maven-GroupId");
//        String artifactId = manifest.getMainAttributes().getValue("Maven-ArtifactId");
//        String version = manifest.getMainAttributes().getValue("Maven-Version");
        String[] classpathArray = getClasspathArray(manifest);
        // read the maven classpath
        for(String path : classpathArray) {
            File jar = getClasspathJar(path);
            files.add(jar);
        }
        return files;
    }
    
    // this is the opposite of resolveClasspath .... this one lists the missing artifacts only
    public Set<String> listMissingArtifacts(Manifest manifest) {
        log.debug("Checking for missing artifacts in classpath");
        HashSet<String> missing = new HashSet<>();
        // how to locate the module that contains this manifest:
//        String groupId = manifest.getMainAttributes().getValue("Maven-GroupId");
//        String artifactId = manifest.getMainAttributes().getValue("Maven-ArtifactId");
//        String version = manifest.getMainAttributes().getValue("Maven-Version");
        String[] classpathArray = getClasspathArray(manifest);
        // read the maven classpath
        for(String path : classpathArray) {
            File jar = getClasspathJar(path);
            if( !jar.exists() ) { missing.add(jar.getName()); }
        }
        return missing;
    }
    
    private String[] getClasspathArray(Manifest manifest) {
        String classpathText = manifest.getMainAttributes().getValue("Maven-Classpath");
        String[] classpathArray = classpathText.split("\\|"); // we define the Maven-Classpath to be : separated on all platforms , so this is NOT File.pathSeparator
        return classpathArray;
    }
    
    private File getClasspathJar(String path) {
            if( path.startsWith(".") ) {
                String fullpath = directory.getAbsolutePath()+path.substring(1).replace("/", File.separator); // replace leading "." with  local value of ~/.m2/repository,  and replace forward slashes with local file separator ; we define the file separator used in Maven-Classpath to be forward slash on all platforms
                log.debug("Artifact: {}", fullpath);
                File jar = new File(fullpath);
                if( !jar.exists() ) { log.error("Missing dependency: {}", fullpath); }
            return jar;
            }
            else {
                log.warn("Artifact with absolute path: {}", path);
                File jar = new File(path);
                if( !jar.exists() ) { log.error("Missing dependency: {}", path); }
            return jar;
            }
    }
    

    private File getDirectory() {
//        String dir = System.getenv("M2_HOME"); // XXX TODO to support M2_HOME  we the have to read  M2_HOME/conf/settings.xml  to find the local repository and remote repositories ... best to do that with the actual maven classes
        String dir = System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository";
//        if (dir == null || dir.isEmpty()) {
//            log.debug("localRepository={}", System.getProperty("localRepository"));
            // XXX   when running in netbeans it's possible for  the syste mproperty  localRepository to be set to something like  C:\Program Files\NetBeans 7.3.1\java\maven   and would be ok except 
//            dir = System.getProperty("localRepository", System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository");
//        }
        log.debug("m2.repository={}", dir);
        File mavenRepositoryDirectory = new File(dir);
        return mavenRepositoryDirectory;
    }

    public File findJarFile(String groupId, String artifactId, String version) {
        if (!directory.exists()) {
            log.error("Maven repository is not found in {}", directory.getAbsolutePath());
            return null;
        }
        File target = directory.toPath().resolve(groupId.replace(".", File.separator)).resolve(artifactId).resolve(version).resolve(artifactId+"-"+version+".jar").toFile();
        log.debug("Resolving target jar {}", target.getAbsolutePath());
        return target;
    }
    
    public File findPomFile(String groupId, String artifactId, String version) {
        if (!directory.exists()) {
            log.error("Maven repository is not found in {}", directory.getAbsolutePath());
            return null;
        }
        File target = directory.toPath().resolve(groupId.replace(".", File.separator)).resolve(artifactId).resolve(version).resolve(artifactId+"-"+version+".pom").toFile();
        log.debug("Resolving target pom {}", target.getAbsolutePath());
        return target;
    }
    
    public InputStream findJar(String groupId, String artifactId, String version) {
        if (!directory.exists()) {
            log.error("Maven repository is not found in {}", directory.getAbsolutePath());
            return null;
        }
        File target = findJarFile(groupId, artifactId, version);
        if (target != null && target.exists()) {
            try {
                return new FileInputStream(target);
            } catch (FileNotFoundException e) {
                log.debug("File not found", e);
                return null;
            }
        }
        return null;

    }
    
    /**
     * 
     * @param name
     * @return a reference to an existing jar file, or null if it is not found
     */
    public File findJarFile(String name) {
        if (!directory.exists()) {
            log.error("Maven repository is not found in {}", directory.getAbsolutePath());
            return null;
        }
        // if the filename includes a file separator, then assume it is a maven repository groupId/artifactId/version type path.
        // otherwise just assume its a jarfile and search for it.
        if( name.contains("/") ) { name = name.replace("/", File.separator); }
        if( name.contains(File.separator) ) {
            File jar = directory.toPath().resolve(name).toFile();
            return jar;
        }
        
        // repository folder exists, now try to find the artifact
        FileTree tree = new FileTree();
        DirectoryFilter folderFilter = new DirectoryFilter();
        DepthFirstTreeIterator<File> it = new DepthFirstTreeIterator<>(tree, directory, folderFilter);
        File found = null;
        while (it.hasNext()) {
            File folder = it.next();
            File jar = folder.toPath().resolve(name).toFile();
            if (jar.exists()) {
                found = jar;
                break;
            }
        }
        if (found == null || !found.exists()) {
            log.debug("Artifact not found in {}", directory.getAbsolutePath());
            return null;
        }
        return found;
    }

    // name like   cpg-crypto-0.1.4-SNAPSHOT.jar
    public File findExistingJarFile(String name) throws FileNotFoundException {
        File jar = findJarFile(name);
        if( jar == null ) {
            throw new FileNotFoundException("Artifact not found: "+ name);
        }
        return jar;
    }
}
