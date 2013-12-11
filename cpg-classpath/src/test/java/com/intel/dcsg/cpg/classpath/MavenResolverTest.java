/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.classpath;

import com.intel.dcsg.cpg.classpath.MavenResolver;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.jar.Manifest;
import org.apache.commons.io.IOUtils;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Exclusion;
import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.junit.Test;
import static org.junit.Assert.*;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.DefaultMavenProjectBuilder;
import org.apache.maven.project.DefaultProjectBuilderConfiguration;
/**
 *
 * @author jbuhacoff
 */
public class MavenResolverTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MavenResolverTest.class);

    @Test
    public void testResolveArtifact() throws FileNotFoundException, IOException {
        MavenResolver m2 = new MavenResolver();
        String artifactName = "cpg-crypto-0.1-SNAPSHOT.jar";
        InputStream in = m2.findJar(artifactName);
        assertNotNull(in);
        FileOutputStream out = new FileOutputStream(new File("."+File.separator+"target"+File.separator+"jmod"+File.separator+artifactName)); // throws FileNotFoundException
        IOUtils.copy(in, out); // throws IOException
        in.close();
        out.close();
        File copy = new File("."+File.separator+"target"+File.separator+"jmod"+File.separator+artifactName);
        assertTrue(copy.exists());
    }
    
    

    /**
     * Minimum code to read a pom from the repository and show the dependencies listed there.
     * However, that is not enough because the model is provided as-is ... any variables used
     * in the pom are not interpolated, and we also don't get the parent project's dependencies
     * that are inherited, etc. 
     * 
     * Sample output:
2013-10-16 11:07:12,557 DEBUG [main] c.i.m.j.MavenResolver [MavenResolver.java:112] m2.repository=C:\Users\jbuhacof\.m2\repository
2013-10-16 11:07:12,572 DEBUG [main] c.i.m.j.MavenResolver [MavenResolver.java:133] Resolving target pom C:\Users\jbuhacof\.m2\repository\com\intel\mtwilson\plugins\mtwilson-version\1.2-SNAPSHOT\mtwilson-version-1.2-SNAPSHOT.pom
2013-10-16 11:07:12,658 DEBUG [main] c.i.m.j.MavenResolverTest [MavenResolverTest.java:76] Dependency: Type:jar Scope:provided GroupId:com.intel.mtwilson.services ArtifactId:mtwilson-jmod-annotations Version:${mtwilson.version} Classifier:null Optional:null
2013-10-16 11:07:12,659 DEBUG [main] c.i.m.j.MavenResolverTest [MavenResolverTest.java:76] Dependency: Type:jar Scope:null GroupId:com.intel.dcsg.cpg ArtifactId:cpg-crypto Version:0.1-SNAPSHOT Classifier:null Optional:null
2013-10-16 11:07:12,660 DEBUG [main] c.i.m.j.MavenResolverTest [MavenResolverTest.java:76] Dependency: Type:jar Scope:null GroupId:commons-codec ArtifactId:commons-codec Version:1.1 Classifier:null Optional:null
2013-10-16 11:07:12,661 DEBUG [main] c.i.m.j.MavenResolverTest [MavenResolverTest.java:76] Dependency: Type:jar Scope:null GroupId:commons-collections ArtifactId:commons-collections Version:2.1 Classifier:null Optional:null
2013-10-16 11:07:12,662 DEBUG [main] c.i.m.j.MavenResolverTest [MavenResolverTest.java:76] Dependency: Type:jar Scope:test GroupId:junit ArtifactId:junit Version:null Classifier:null Optional:null
2013-10-16 11:07:12,663 DEBUG [main] c.i.m.j.MavenResolverTest [MavenResolverTest.java:76] Dependency: Type:jar Scope:null GroupId:org.slf4j ArtifactId:slf4j-api Version:1.6.4 Classifier:null Optional:null
2013-10-16 11:07:12,664 DEBUG [main] c.i.m.j.MavenResolverTest [MavenResolverTest.java:76] Dependency: Type:jar Scope:null GroupId:ch.qos.logback ArtifactId:logback-classic Version:1.0.9 Classifier:null Optional:null
2013-10-16 11:07:12,664 DEBUG [main] c.i.m.j.MavenResolverTest [MavenResolverTest.java:76] Dependency: Type:jar Scope:null GroupId:ch.qos.logback ArtifactId:logback-core Version:1.0.9 Classifier:null Optional:null
     * @throws Exception 
     */
    @Test
    public void testReadModulePomInRespositoryModelOnly() throws Exception {
        // read model
        MavenResolver resolver = new MavenResolver();
        File pomFile = resolver.findPomFile("com.intel.mtwilson.plugins", "mtwilson-version", "1.2-SNAPSHOT");
        MavenXpp3Reader reader = new MavenXpp3Reader();
        FileInputStream in = new FileInputStream(pomFile);
        Model pom = reader.read(in);
        in.close();
        /*
         * The model is abstract - it's only fo rthis artifact (does not include dependencies declared in parent)
         * and does not have variables resolved (so you see variable names like version is ${mtwilson.version} instead
         * of the value 1.2-SNAPSHOT) .  so we would need to do additional work to resolve everything , and 
         * maven already knows how to do it so we would need to set up a context for it to initialize the project,
         * and then run maven-dependency-plugin to generate the exact information we need.
        MavenProject project = new MavenProject(pom);
        DefaultMavenProjectBuilder builder = new DefaultMavenProjectBuilder();
        DefaultProjectBuilderConfiguration config = new DefaultProjectBuilderConfiguration();
//        config.setLocalRepository(null)
        // XXX TODO need to fill in the configuration...  probably trying to enter maven code at the wrong spot. really we just need mvn dependency:list
        // report
         */
        // report:
        List<Dependency> dependencies = pom.getDependencies();
        assertNotNull(dependencies);
        for(Dependency dependency : dependencies) {
            log.debug("Dependency: {}", String.format("Type:%s Scope:%s GroupId:%s ArtifactId:%s Version:%s Classifier:%s Optional:%s",
                    dependency.getType(),
                    dependency.getScope(),
                    dependency.getGroupId(),
                    dependency.getArtifactId(),
                    dependency.getVersion(),
                    dependency.getClassifier(),
                    dependency.getOptional()));
            if( !dependency.getExclusions().isEmpty() ) {
                for( Exclusion exclusion : dependency.getExclusions() ) {
                    log.debug("Excluding: {}", String.format("GroupId:%s ArtifactId:%s", exclusion.getGroupId(), exclusion.getArtifactId()));
                }
            }
        }
    }

    @Test
    public void testReadMavenClasspathFromManifest() throws Exception {
        MavenResolver resolver = new MavenResolver();
        // load the module's jarfile from the repository
        File jarFile = resolver.findJarFile("com.intel.mtwilson.plugins", "mtwilson-version", "1.2-SNAPSHOT");
        Manifest manifest = JarUtil.readManifest(jarFile);
        // read the maven classpath
        String classpathText = manifest.getMainAttributes().getValue("Maven-Classpath");
        String[] classpathArray = classpathText.split(":");
        for(String path : classpathArray) {
            String fullpath = path.replaceFirst("^\\.", "~/.m2/repository");
            log.debug("Artifact: {}", fullpath);
        }
    }

    
}
