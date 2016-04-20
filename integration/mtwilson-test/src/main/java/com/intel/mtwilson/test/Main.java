/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.test;

import com.intel.mtwilson.My;
import com.intel.mtwilson.MyConfiguration;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import org.apache.commons.configuration.Configuration;
import org.junit.Test;
import org.junit.runner.JUnitCore;

/**
 * This program conducts automated testing of a Mt Wilson installation.
 * Purpose is to identify new defects & regressions automatically in order to
 * speed up the QA process for Mt Wilson, as well as check a new installation
 * for problems immediately after the install.
 * 
 * The tests defined in this module should strongly correlate to TEST CASES that
 * would be executed manually by the QA team. To check if the test cases are covering
 * a sufficient amount of the program code, you should instrument the server using
 * emma and after testing generate a code coverage report.
 * 
 * The program maintains a configuration directory in ~/.mtwilson/autotest containing
 * the list of hosts, policies, and other items to check.
 * 
 * When running from a developer laptop, the configuration in ~/.mtwilson that is 
 * already used for the developer's JUnit tests is automatically read to configure
 * the Mt Wilson sever to test.
 * 
 * When running on a Mt Wilson server, the regular configuration directory /etc/mtwilson
 * (actually /etc/intel/cloudsecurity)
 * is read to obtain all information about the (localhost) Mt Wilson server being tested.
 * 
 * NOTE: the rule for tests defined in this project is that they must be repeatable - at
 * the end of every test, the Mt Wilson server & database must be restored to the state it
 * was in before the test was executed.
 * 
 * 
 * 
 * XXX TODO  instead of writing tests in this project,   this project should simply compile
 * all the -test.jar artifacts from other modules and then run the tests.  so every
 * project that has unit tests that should be included in the QA run and the customer selftest,
 * should attach a test artifact . this module will depend on all those test artifacts and
 * then scan for all tests and automatically run all tests.  
 * SO FOR EXAMPLE,  to test the quickstart procedure,  create an api-quickstart project
 * that has the sample code for the quickstart  (the document should copy-paste from the
 * real java code!!)  and then add a -test.jar artifact to it, and include it here, and it
 * should run.  note that the JUnit test for the quickstart may use the "My" and jpa modules
 * to automatically connect to the database and approve the new user (simulating the 
 * management console activity) in order to run a fully automated test. 
 *
 * 
 * @author jbuhacoff
 */
public class Main {
    public static MyConfiguration config;
    public static void main(String[] args) throws IOException {
        try {
        config = My.configuration();
        printMyPreferences();
        /*
//        String[] classnames = findClassNamesInPackage("com.intel.mtwilson.autotest.")
        String[] classnames = new String[] {
            "com.intel.mtwilson.autotest.junit.Quickstart" 
        };
        JUnitCore.main(args);
        * */
        }
        catch(IOException e) {
            System.err.println("error: "+e.toString());
            // if debug level is on, then print stack trace....
        }
    }
    
    /**
     * Copied from test.myconfig.TestMyConfig in the "my" module
     * @throws MalformedURLException
     * @throws IOException 
     */
    public static void printMyPreferences() throws MalformedURLException, IOException {
        System.out.println("# API CLIENT PREFERENCES");
        System.out.println(String.format("%s=%s", "mtwilson.config.dir", config.getDirectoryPath()));
        System.out.println(String.format("%s=%s", "mtwilson.api.username", config.getKeystoreUsername()));
        System.out.println(String.format("%s=%s", "mtwilson.api.password", config.getKeystorePassword()));
        System.out.println(String.format("%s=%s", "mtwilson.api.url", config.getMtWilsonURL().toString()));
        System.out.println(String.format("%s=%s", "mtwilson.api.roles", config.getMtWilsonRoleString()));
        System.out.println("# DATABASE PREFERENCES");
        System.out.println(String.format("%s=%s", "mtwilson.db.host", config.getDatabaseHost()));
        System.out.println(String.format("%s=%s", "mtwilson.db.port", config.getDatabasePort()));
        System.out.println(String.format("%s=%s", "mtwilson.db.user", config.getDatabaseUsername()));
        System.out.println(String.format("%s=%s", "mtwilson.db.password", config.getDatabasePassword()));
        System.out.println(String.format("%s=%s", "mtwilson.db.schema", config.getDatabaseSchema()));
        System.out.println(String.format("%s=%s", "mtwilson.as.dek", config.getDataEncryptionKeyBase64()));
    }

    
/**
 * Scans all classes accessible from the context class loader which belong to the given package and subpackages.
 *
 * @param packageName The base package
 * @return The classes
 * @throws ClassNotFoundException
 * @throws IOException
 *//*
private static Class[] getClasses(String packageName)
        throws ClassNotFoundException, IOException {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    assert classLoader != null;
    String path = packageName.replace('.', '/');
    Enumeration<URL> resources = classLoader.getResources(path);
    List<File> dirs = new ArrayList<File>();
    while (resources.hasMoreElements()) {
        URL resource = resources.nextElement();
        dirs.add(new File(resource.getFile()));
    }
    ArrayList<Class> classes = new ArrayList<Class>();
    for (File directory : dirs) {
        classes.addAll(findClasses(directory, packageName));
    }
    return classes.toArray(new Class[classes.size()]);
}*/

/**
 * Recursive method used to find all classes in a given directory and subdirs.
 *
 * @param directory   The base directory
 * @param packageName The package name for classes found inside the base directory
 * @return The classes
 * @throws ClassNotFoundException
 *//*
private static List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {
    List<Class> classes = new ArrayList<Class>();
    if (!directory.exists()) {
        return classes;
    }
    File[] files = directory.listFiles();
    for (File file : files) {
        if (file.isDirectory()) {
            assert !file.getName().contains(".");
            classes.addAll(findClasses(file, packageName + "." + file.getName()));
        } else if (file.getName().endsWith(".class")) {
            classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
        }
    }
    return classes;
}  */  
}
