/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.api;

//import com.intel.mtwilson.ApiException;
//import com.intel.mtwilson.ClientException;
//import com.intel.mtwilson.KeystoreUtil;
//import com.intel.mtwilson.crypto.CryptographyException;
import com.intel.mtwilson.MyConfiguration;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class TestMyConfig {
    
    /**
     * Use this method to set your personal preferences... just customize the settings and run it as a junit test. After
     * you run it to set YOUR LOCAL JAVA PREFERENCES, please restore the settings IN THIS FILE to what they were, you
     * can simply "undo" all the changes until it's back to original form.
     * 
     * You only need to modify & run this if you do NOT want your settings to be stored at ~/.mtwilson
     * 
     * NOTE:  the @Test annotation is commented out to prevent this code from running automatically and 
     * resetting someone's preferences w/o them noticing;   so be sure to comment it out again when you're done!
     */
//    @Test
    public void testSetMyLocalDirectoryPath() throws IOException {
        MyConfiguration my = new MyConfiguration();
        my.setDirectoryPath(null); // restore default:  ~/.mtwilson
        my.setDirectoryPath("C:/Intel/CloudSecurity");
    }
    
    /**
     * Use this method to check what is stored in your personal preferences... just run it as a junit test to see the
     * console output
     *
     *
     */
    @Test
    public void testCheckMyPreferences() throws MalformedURLException, IOException {
        MyConfiguration myconfig = new MyConfiguration();
        System.out.println("----- API CLIENT PREFERENCES -----");
        System.out.println(String.format("%s: %s", "mtwilson.config.dir", myconfig.getDirectoryPath()));
        System.out.println(String.format("%s: %s", "mtwilson.api.username", myconfig.getKeystoreUsername()));
        System.out.println(String.format("%s: %s", "mtwilson.api.password", myconfig.getKeystorePassword()));
        System.out.println(String.format("%s: %s", "mtwilson.api.url", myconfig.getMtWilsonURL().toString()));
        System.out.println(String.format("%s: %s", "mtwilson.api.roles", myconfig.getMtWilsonRoleString()));
        System.out.println("----- DATABASE PREFERENCES -----");
        System.out.println(String.format("%s: %s", "mtwilson.db.host", myconfig.getDatabaseHost()));
        System.out.println(String.format("%s: %s", "mtwilson.db.port", myconfig.getDatabasePort()));
        System.out.println(String.format("%s: %s", "mtwilson.db.user", myconfig.getDatabaseUsername()));
        System.out.println(String.format("%s: %s", "mtwilson.db.password", myconfig.getDatabasePassword()));
        System.out.println(String.format("%s: %s", "mtwilson.db.schema", myconfig.getDatabaseSchema()));
        System.out.println(String.format("%s: %s", "mtwilson.as.dek", myconfig.getDataEncryptionKeyBase64()));
    }

    
    
    /**
     * Creates a local user keystore and registers your new user with Mt Wilson
     * After running this method, go to the Mt Wilson Management Console and approve your new user.
     * 
     * @throws MalformedURLException
     * @throws IOException
     * @throws ApiException
     * @throws ClientException
     * @throws CryptographyException 
     */
    @Test
    public void testCreateLocalDirectory() throws  IOException {
        MyConfiguration config = new MyConfiguration();
        File directory = config.getKeystoreDir();
        if( !directory.exists() ) {
            directory.mkdirs();
        }
		/*
        KeystoreUtil.createUserInDirectory(
           config.getKeystoreDir(), 
           config.getKeystoreUsername(), 
           config.getKeystorePassword(), 
           config.getMtWilsonURL(), 
           config.getMtWilsonRoleArray()); 
		   */
    }
    
    
    
}
