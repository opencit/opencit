/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.myconfig;

import com.intel.mtwilson.MyConfiguration;
import java.io.IOException;
import java.net.MalformedURLException;
import org.junit.Test;
/**
 *
 * @author jbuhacoff
 */
public class InitMyConfig {
    
    /**
     * Use this method to check what is stored in your personal preferences... just run it as a junit test to see the
     * console output
     *
     * If you don't have any preferences, a default set will be automatically created for you in ~/.mtwilson
     * 
     * If instead of ~/.mtwilson you want to use a different configuration directory, do something like this:
     * cd mtwilson/integration/my/target
     * java -jar my-1.2-SNAPSHOT-with-dependencies.jar  set mtwilson.config.dir C:\Intel\CloudSecurity
     * 
     * If you want to go back to using ~/.mtwilson again, you can do this:
     * cd mtwilson/integration/my/target
     * java -jar my-1.2-SNAPSHOT-with-dependencies.jar  remove mtwilson.config.dir
     * 
     */
    @Test
    public void testCheckMyPreferences() throws MalformedURLException, IOException {
        MyConfiguration myconfig = new MyConfiguration();
        System.out.println("# API CLIENT PREFERENCES");
        System.out.println(String.format("%s=%s", "mtwilson.config.dir", myconfig.getDirectoryPath()));
        System.out.println(String.format("%s=%s", "mtwilson.api.username", myconfig.getKeystoreUsername()));
        System.out.println(String.format("%s=%s", "mtwilson.api.password", myconfig.getKeystorePassword()));
        System.out.println(String.format("%s=%s", "mtwilson.api.url", myconfig.getMtWilsonURL().toString()));
        System.out.println(String.format("%s=%s", "mtwilson.api.roles", myconfig.getMtWilsonRoleString()));
        System.out.println("# DATABASE PREFERENCES");
        System.out.println(String.format("%s=%s", "mtwilson.db.host", myconfig.getDatabaseHost()));
        System.out.println(String.format("%s=%s", "mtwilson.db.port", myconfig.getDatabasePort()));
        System.out.println(String.format("%s=%s", "mtwilson.db.user", myconfig.getDatabaseUsername()));
        System.out.println(String.format("%s=%s", "mtwilson.db.password", myconfig.getDatabasePassword()));
        System.out.println(String.format("%s=%s", "mtwilson.db.schema", myconfig.getDatabaseSchema()));
        System.out.println(String.format("%s=%s", "mtwilson.as.dek", myconfig.getDataEncryptionKeyBase64()));
    }

   
}
