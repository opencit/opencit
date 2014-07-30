/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.v1;

import com.intel.dcsg.cpg.crypto.file.PasswordEncryptedFile;
import com.intel.dcsg.cpg.io.AllCapsEnvironmentConfiguration;
import com.intel.dcsg.cpg.io.ExistingFileResource;
import com.intel.dcsg.cpg.io.Platform;
import com.intel.dcsg.cpg.io.pem.Pem;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.prefs.Preferences;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.EnvironmentConfiguration;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This utility class aims to make it possible for developers to all use the same JUnit tests and point them at
 * different mt wilson environments without having to modify the JUnit tests. So modifications should only be necessary to change
 * the tests, and not merely to change the IP address of the server or the location of the api client keystore.
 * 
 * THIS CLASS LOADS YOUR PERSONAL CONFIGURATION AUTOMATICALLY FROM ~/.mtwilson/mtwilson.properties
 * 
 * If that file does not exist, it will be automatically created with default values.  
 * 
 * After that, you can change which Mt Wilson server you are testing against, etc.  by editing those properties.
 * All JUnit tests for the api client should use the "My" class (next to this one) in order to automatically pick up your
 * local settings.
 * 
 * NOTE: If you need to change the location of the file from ~/.mtwilson to somewhere else, you can update your 
 * Java Preferences using the testSetMyConfigDir() method in this class -- look at that method's Javadoc for details.
 *
 * Example Junit test:
 *
 * MyConfiguration config = new MyConfiguration(); KeystoreUtil.createUserInDirectory( config.getKeystoreDir(),
 * config.getKeystoreUsername(), config.getKeystorePassword(), config.getMtWilsonURL(), config.getMtWilsonRoleArray());  *
 *
 * 
 * NOTE:  the default directory to store all your settings is  ~/.mtwilson
 * In order to change it, you have to set your Java Preferences using MyPrefs -- see the MyPrefs class for details.
 *
 * @author jbuhacoff
 */
public class MyConfiguration {
    private Logger log = LoggerFactory.getLogger(getClass());
//    private final String PROPERTIES_FILENAME = "mtwilson.properties";

    private Preferences prefs = Preferences.userRoot().node(getClass().getName());
//    private Properties conf = new Properties();
    private Configuration conf = null;
    private HashMap<String,String> keySourceMap = new HashMap<String,String>();
    
    // look in default locations
    public MyConfiguration() throws IOException {
        /*
        File directory = getDirectory();
        if( !directory.exists() && !directory.mkdirs() ) {
            throw new IOException("Cannot create configuration directory: "+directory.getAbsolutePath());
        }
        File file = getConfigFile();
        if( !file.exists() ) {
            // write a "starter" configuratio file (not encrypted) which the user can then customize and use, or encrypt, etc.
            FileOutputStream out = new FileOutputStream(file);
            getDefaultProperties().store(out, "Default Mt Wilson Settings... Customize for your environment");
            out.close();        
        }
        // here we load the configuration file, which could be either plain or encrypted
        FileInputStream in = new FileInputStream(file);
        conf.load(in);
        in.close();
        */
        conf = gatherConfiguration(null);
    }
    
    public MyConfiguration(Properties custom) {
        // the custom properties take priority over any other source
        conf = gatherConfiguration(custom);
    }
    
    // commenting out unused function for removal (6/11 1.2)
    //
    //private Properties getDefaultProperties() {
    //    Properties p = new Properties();
        // api client
    //    p.setProperty("mtwilson.api.username", System.getProperty("user.name", "anonymous"));
    //    p.setProperty("mtwilson.api.password", "password");
    //    p.setProperty("mtwilson.api.url", "https://127.0.0.1:8181");
    //    p.setProperty("mtwilson.api.roles", "Attestation,Whitelist,Security,Report,Audit");
        // database
    //    p.setProperty("mtwilson.db.protocol", "postgresql"); // new default in mtwilson 1.2
    //    p.setProperty("mtwilson.db.driver", "org.postgresql.Driver"); // new default in mtwilson 1.2
    //    p.setProperty("mtwilson.db.host", "127.0.0.1");
    //    p.setProperty("mtwilson.db.schema", "mw_as");
    //    p.setProperty("mtwilson.db.user", ""); // we must keep this entry because we use it to write out the "starter" config file;  but in mtwilson 1.2 we remove the default value; both mysql and postgresql support localhost connection without authentication
    //    p.setProperty("mtwilson.db.password", ""); // we must keep this entry because we use it to write out the "starter" config file;  but in mtwilson 1.2 we remove the default value;  both mysql and postgresql support localhost connection without authentication
    //    p.setProperty("mtwilson.db.port", "5432"); // in mtwilson 1.2 the default changed from mysql/3306 to postgresql/5432
    //    p.setProperty("mtwilson.as.dek", "");   // we must keep this entry because we use it to write out the "starter" config file;  but in mtwilson 1.2 we remove the default value;  we must force customer to create one during install             
    //    return p;
    //}
    
    private void logConfiguration(String source, Configuration config) {
        log.debug("Loaded configuration keys from {}: {}", source, StringUtils.join(config.getKeys(), ", "));
        // we log the source of each configuration key;  CompositeConfiguration has a method called getSource() but it throws IllegalArgumentException if more than one child configuration has the key, which is wrong because get*() functions return from the first configuration to have it, and so getSource() should also return the first configuration that has the key... but it doesn't. so we keep track ourselves.
        Iterator<String> it = config.getKeys();
        while(it.hasNext()) {
            String key = it.next();
            if( !keySourceMap.containsKey(key) ) {
                keySourceMap.put(key, source);                
            }
        }
    }
    
    public String getSource(String key) {
        return keySourceMap.get(key); // will be null if the key is not defined
    }
    
    private Configuration gatherConfiguration(Properties customProperties) {
        CompositeConfiguration composite = new CompositeConfiguration();

        // first priority: custom properties take priority over any other source
        if( customProperties != null ) {
            MapConfiguration customconfig = new MapConfiguration(customProperties);
            logConfiguration("custom", customconfig);
            composite.addConfiguration(customconfig);
        }
        
        // second priority are properties defined on the current JVM (-D switch
        // or through web container)
        SystemConfiguration system = new SystemConfiguration();
        logConfiguration("system", system);
        composite.addConfiguration(system);
        
        // third priority: environment variables (regular and also converted from dot-notation to all-caps)
        EnvironmentConfiguration env = new EnvironmentConfiguration();
        logConfiguration("environment", env);
        composite.addConfiguration(env);
//        AllCapsEnvironmentConfiguration envAllCaps = new AllCapsEnvironmentConfiguration();
//        logConfiguration("environment_allcaps", envAllCaps);
//        composite.addConfiguration(envAllCaps);
        
        
        List<File> files = listConfigurationFiles();
        // add all the files we found so far, in the priority order
        for (File f : files) {
//            System.out.println("Looking for "+f.getAbsolutePath());
            try {
                if (f.exists() && f.canRead()) {
                    String content;
                    try (FileInputStream in = new FileInputStream(f)) {
                        content = IOUtils.toString(in);
                    }
                    if( Pem.isPem(content) ) { // starts with something like -----BEGIN ENCRYPTED DATA----- and ends with -----END ENCRYPTED DATA-----
                        // a pem-format file indicates it's encrypted... we could check for "ENCRYPTED DATA" in the header and footer too.
                        String password = null;
                        if( system.containsKey("mtwilson.password") ) {
                            password = system.getString("mtwilson.password");
                        }
                        else if( env.containsKey("MTWILSON_PASSWORD") ) {
                            password = env.getString("MTWILSON_PASSWORD");
                        }
                        else {
                            log.warn("Found encrypted configuration file, but no password was found in system properties or environment");
                        }
                        if( password != null ) {
                            ExistingFileResource resource = new ExistingFileResource(f);
                            PasswordEncryptedFile encryptedFile = new PasswordEncryptedFile(resource, password);
                            String decryptedContent = encryptedFile.loadString();
                            Properties p = new Properties();
                            p.load(new StringReader(decryptedContent));
                            MapConfiguration encrypted = new MapConfiguration(p);
                            logConfiguration("encrypted-file:"+f.getAbsolutePath(), encrypted);
                            composite.addConfiguration(encrypted);
                        }
                    }
                    else {
                        log.debug("FILE {} IS IN REGULAR PROPERTIES FORMAT", f.getAbsolutePath());
                        PropertiesConfiguration standard = new PropertiesConfiguration(f);
                        logConfiguration("file:"+f.getAbsolutePath(), standard);
                        composite.addConfiguration(standard);
                    }
                }
            } catch (FileNotFoundException ex) { // shouldn't happen since we check for f.exists() first, but must handle it because FileInputStream can throw it
                log.error("File not found: " + f.getAbsolutePath(), ex);
            } catch (IOException ex) {
                log.error("Cannot load configuration: " + f.getAbsolutePath(), ex);
            } catch (ConfigurationException ex) {
                log.error("Cannot load configuration from " + f.getAbsolutePath(), ex);
            }
        }

        
        // seventh priority are properties defined on the classpath (for example defaults provided with the application, or placed in the web server container)
        String propertiesFilename = "mtwilson.properties";
        InputStream in = getClass().getResourceAsStream(
                "/" + propertiesFilename);
        try {
            // user's home directory (assuming it's on the classpath!)
            if (in != null) {
                Properties properties = new Properties();
                properties.load(in);
                MapConfiguration classpath = new MapConfiguration(properties);
                logConfiguration("classpath:"+propertiesFilename, classpath);
                composite.addConfiguration(classpath);
            }
        } catch (IOException ex) {
            log.debug("Did not find [" + propertiesFilename + "] properties on classpath",
                    ex);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    log.error("Failed to close input stream for "
                            + propertiesFilename);
                }
            }
        }
        
        
        return composite;
    }
    
    /**
     * This is the only method that uses the Java Preferences API to get its value. Everything
     * else uses the mtwilson.properties file located the directory returned by this method.
     * 
     * You can change your directory path preference from the default to any directory by 
     * running the MyPrefs command from your shell -- see the MyPrefs class for details
     * 
     * @return ~/.mtwilson  unless you have changed your preferences (see testSetMyConfigDir)
     */
    public final String getDirectoryPath() {
        return prefs.get("mtwilson.config.dir", System.getProperty("user.home") + File.separator + ".mtwilson");
    }
    public final File getDirectory() {
        return new File(getDirectoryPath());
    }
/* /// this one is a bad idea because the configuration can come from several places, and the config file is not the top priority source,
 * // so providing this could create a situation where someone think that by writing to this config file they can affect the configuration
 * // when in reality it may be overridden by a number of sources. 
    public final File getConfigFile() {
        return new File(getDirectoryPath() + File.separator + "mtwilson.properties");
    }*/
    
    /**
     * The list of files returned is in priority order -- properties defined in the first file override all others.
     * Note that when actually loading the configuration, system properties and environment variables have a higher
     * priority than the configuration files.
     * @return 
     */
    public List<File> listConfigurationFiles() {
        // prepare a list of files to be loaded, in order
        ArrayList<File> files = new ArrayList<File>();
        
        
        // fourth priority: if there is a custom configuration directory defined by a system property, load configuration from there
        String customConfigDir = System.getProperty("mtwilson.config.dir", prefs.get("mtwilson.config.dir", ""));
        if( !customConfigDir.isEmpty() ) {
            files.add(new File(customConfigDir + File.separator + "mtwilson.properties"));
        }
        
        // fifth priority:  properties defined in user's home directory ~/.mtwilson
        files.add(new File(System.getProperty("user.home") + File.separator + ".mtwilson" + File.separator + "mtwilson.properties"));


        // sixth priority: properties defined in standard install location
        if( Platform.isWindows() ) {
            files.add(new File("C:" + File.separator + "Intel" + File.separator
                    + "CloudSecurity" + File.separator + "mtwilson.properties"));
//            files.add(new File(System.getProperty("user.home") + File.separator
//                    + propertiesFilename));

            files.add(new File("C:" + File.separator + "Intel" + File.separator
                    + "CloudSecurity" + File.separator + "management-service.properties"));
            files.add(new File("C:" + File.separator + "Intel" + File.separator
                    + "CloudSecurity" + File.separator + "attestation-service.properties"));
            files.add(new File("C:" + File.separator + "Intel" + File.separator
                    + "CloudSecurity" + File.separator + "audit-handler.properties"));
            files.add(new File("C:" + File.separator + "Intel" + File.separator
                    + "CloudSecurity" + File.separator + "mtwilson-portal.properties"));
            files.add(new File("C:" + File.separator + "Intel" + File.separator
                    + "CloudSecurity" + File.separator + "management-cmdutil.properties"));
             files.add(new File("C:" + File.separator + "Intel" + File.separator
                    + "CloudSecurity" + File.separator + "PrivacyCA.properties"));

        }
        // linux-specific location
        if (Platform.isUnix() ) {
//            files.add(new File("/etc/intel/cloudsecurity/" + propertiesFilename));
            files.add(new File("/etc/intel/cloudsecurity/mtwilson.properties"));
            files.add(new File("/etc/intel/cloudsecurity/management-service.properties"));
            files.add(new File("/etc/intel/cloudsecurity/attestation-service.properties"));
            files.add(new File("/etc/intel/cloudsecurity/audit-handler.properties"));
            files.add(new File("/etc/intel/cloudsecurity/mtwilson-portal.properties"));
            files.add(new File("/etc/intel/cloudsecurity/management-cmdutil.properties"));
            files.add(new File("/etc/intel/cloudsecurity/PrivacyCA.properties"));
        }
        return files;
    }
    
    public Configuration getConfiguration() { return conf; }
    
    public Properties getProperties() {
        Properties p = new Properties();
        Iterator<String> it = conf.getKeys();
        while(it.hasNext()) {
            String name = it.next();
            if(conf.containsKey(name)) {
                p.setProperty(name, conf.getString(name));
            }
        }
        return p;
    }

    public Properties getProperties(String... names) {
        Properties p = new Properties();
        for(String name : names) {
            if(conf.containsKey(name)) {
                p.setProperty(name, conf.getString(name));
            }
        }
        return p;
    }
    
    public final File getEnvironmentFile() {
        return new File(getDirectoryPath() + File.separator + "environment.txt");
    }
    
    ///////////////////////// api client //////////////////////////////////
    
    public File getKeystoreDir() {
        return getDirectory();
    }

    public File getKeystoreFile() {
        String username = getKeystoreUsername();
        return new File(getDirectoryPath() + File.separator + username + ".jks");
    }

    public String getKeystoreUsername() {
        return conf.getString("mtwilson.api.username", System.getProperty("user.name", "anonymous"));
    }

    public String getKeystorePassword() {
        return conf.getString("mtwilson.api.password", conf.getString("KEYSTOREPASSWORD", "password")); 
    }

    public URL getMtWilsonURL() throws MalformedURLException {
        return new URL(conf.getString("mtwilson.api.url", "https://127.0.0.1:8181"));
    }

    public String getMtWilsonRoleString() {
        return conf.getString("mtwilson.api.roles", "Attestation,Whitelist,Security,Report,Audit,AssetTagManagement");
    }

    public String[] getMtWilsonRoleArray() {
        return getMtWilsonRoleString().split(",");
    }
    
    public String[] getAvailableLocales() {
//        return conf.getString("mtwilson.locales", "en").split(",");
        String localeParsed = conf.getProperty("mtwilson.locales").toString().replaceAll("\\s+", "");
        return localeParsed.substring(1, localeParsed.length() - 1).split(",");
    }

    ///////////////////////// database //////////////////////////////////

    public String getDatabaseProtocol() {
        if( conf.containsKey("mtwilson.db.protocol") ) { conf.getString("mtwilson.db.protocol", "postgresql"); }
        if( conf.containsKey("mountwilson.as.db.protocol") ) { conf.getString("mountwilson.as.db.protocol", "postgresql"); } 
        if( conf.containsKey("mountwilson.ms.db.protocol") ) { conf.getString("mountwilson.ms.db.protocol", "postgresql"); } 
        if( conf.containsKey("mtwilson.db.driver") ) {
            String driver = conf.getString("mtwilson.db.driver", "");
            if( driver.equals("org.postgresql.Driver") ) { return "postgresql"; }
            if( driver.equals("com.mysql.jdbc.Driver") ) { return "mysql"; }
        }
        if( conf.containsKey("mtwilson.db.port") ) {
            String port = conf.getString("mtwilson.db.port", "");
            if( port.equals("5432") ) { return "postgresql"; }
            if( port.equals("3306") ) { return "mysql"; }
        }
        return "postgresql";  // used in the jdbc url, so "postgresql" or "mysql"  as in jdbc:mysql://host:port/schema
    }
    
    public String getDatabaseDriver() {
        if( conf.containsKey("mtwilson.db.driver") ) { conf.getString("mtwilson.db.driver", "org.postgresql.Driver"); }
        if( conf.containsKey("mountwilson.as.db.driver") ) { conf.getString("mountwilson.as.db.driver", "org.postgresql.Driver"); } 
        if( conf.containsKey("mountwilson.ms.db.driver") ) { conf.getString("mountwilson.ms.db.driver", "org.postgresql.Driver"); } 
        if( conf.containsKey("mtwilson.db.protocol") ) {
            String protocol = conf.getString("mtwilson.db.protocol", "");
            if( protocol.equals("postgresql") ) { return "org.postgresql.Driver"; }
            if( protocol.equals("mysql") ) { return "com.mysql.jdbc.Driver"; }
        }
        if( conf.containsKey("mtwilson.db.port") ) {
            String port = conf.getString("mtwilson.db.port", "");
            if( port.equals("5432") ) { return "org.postgresql.Driver"; }
            if( port.equals("3306") ) { return "com.mysql.jdbc.Driver"; }
        }
        return "org.postgresql.Driver"; // either "org.postgresql.Driver" or "com.mysql.jdbc.Driver"
    }

    public String getDatabasePort() {
        if( conf.containsKey("mtwilson.db.port") ) { conf.getString("mtwilson.db.port", "5432"); }
        if( conf.containsKey("mountwilson.as.db.port") ) { conf.getString("mountwilson.as.db.port", "5432"); } 
        if( conf.containsKey("mountwilson.ms.db.port") ) { conf.getString("mountwilson.ms.db.port", "5432"); } 
        if( conf.containsKey("mtwilson.db.protocol") ) {
            String protocol = conf.getString("mtwilson.db.protocol", "");
            if( protocol.equals("postgresql") ) { return "5432"; }
            if( protocol.equals("mysql") ) { return "3306"; }
        }
        if( conf.containsKey("mtwilson.db.driver") ) {
            String port = conf.getString("mtwilson.db.driver", "");
            if( port.equals("org.postgresql.Driver") ) { return "5432"; }
            if( port.equals("com.mysql.jdbc.Driver") ) { return "3306"; }
        }
        return "5432"; // 5432 is postgresql default, 3306 is mysql default
    }
    
    public String getDatabaseHost() {
        return conf.getString("mtwilson.db.host", "127.0.0.1");
    }

    public String getDatabaseUsername() {
        return conf.getString("mtwilson.db.user", ""); // removing default in mtwilson 1.2; was "root"
    }

    public String getDatabasePassword() {
        return conf.getString("mtwilson.db.password", conf.getString("PGPASSWORD", "")); // removing default in mtwilson 1.2;  was "password";   // bug #733 
    }

    public String getDatabaseSchema() {
        return conf.getString("mtwilson.db.schema", "mw_as");
    }

    public String getDataEncryptionKeyBase64() {
        return conf.getString("mtwilson.as.dek", ""); // removing default in mtwilson 1.2;  was "hPKk/2uvMFRAkpJNJgoBwA=="
    }
    
    ///////////////////////// saml key for attestation service //////////////////////////////////

    public File getSamlKeystoreFile() {
        return new File(conf.getString("saml.keystore.file", getDirectoryPath() + File.separator + "mtwilson-saml.jks"));
    }
    public String getSamlKeystorePassword() {
        return conf.getString("saml.key.password", ""); // bug #733 
    }
    
    ///////////////////////// tls policy  //////////////////////////////////
    public String getDefaultTlsPolicyName() {
        return conf.getString("mtwilson.default.tls.policy.id", "TRUST_CA_VERIFY_HOSTNAME"); // issue #871 default should be secure;  customer can explicitly set to TRUST_FIRST_CERTIFICATE if that's what they want
    }

    public File getTlsKeystoreFile() {
        return new File(conf.getString("mtwilson.tls.keystore.file",getDirectoryPath() + File.separator + "mtwilson-tls.jks"));
    }

    public String getTlsKeystorePassword() {
        return conf.getString("mtwilson.tls.keystore.password", ""); // Intentionally not providing a default password;  the mtwilson-server install script automatically generates a password for new installs. 
    }
    
    public boolean getAutoUpdateHosts() {
        return conf.getBoolean("mtwilson.as.autoUpdateHost", false);
    }    

    // asset tagging html5 resources (used by the reference implementation)
    public String getAssetTagHtml5Dir() {
        return conf.getString("mtwilson.atag.html5.dir", "clap://html5/"); // the clap protocol means classpath for the restlet engine
    }
    
    // asset tag server url
    public URL getAssetTagServerURL() throws MalformedURLException {
        return new URL(conf.getString("mtwilson.atag.url", "https://localhost:9999"));
    }
        
    public String getAssetTagServerString() throws MalformedURLException {
        return conf.getString("mtwilson.atag.url", "https://localhost:9999");
    }
    
    public String getAssetTagKeyStorePath() {
        return conf.getString("mtwilson.atag.keystore", "serverAtag.jks");
    }
    
    public String getAssetTagKeyStorePassword() {
        return conf.getString("mtwilson.atag.keystore.password"); // must not have default password; run setup
    }
    
    public String getAssetTagKeyPassword() {
        return conf.getString("mtwilson.atag.key.password");// must not have default password; run setup
    }
    
    public String getAssetTagApiUsername() {
        return conf.getString("mtwilson.atag.api.username", "admin");
    }
    
    public String getAssetTagApiPassword() {
        return conf.getString("mtwilson.atag.api.password"); // must not have default password; run setup
    }
    
    public Boolean getAssetTagAutoImport() {
        return conf.getBoolean("mtwilson.atag.certificate.import.auto",true);
    }
    
    public String getAssetTagMtWilsonBaseUrl() {
        return conf.getString("mtwilson.atag.mtwilson.baseurl", "");
    }
    

}
