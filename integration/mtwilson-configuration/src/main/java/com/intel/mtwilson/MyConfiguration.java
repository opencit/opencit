/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson;

import com.intel.dcsg.cpg.crypto.file.PasswordEncryptedFile;
import com.intel.dcsg.cpg.i18n.LocaleUtil;
import com.intel.dcsg.cpg.io.ExistingFileResource;
import com.intel.dcsg.cpg.io.Platform;
import com.intel.dcsg.cpg.io.pem.Pem;
import com.intel.mtwilson.configuration.ConfigurationFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
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
 * This utility class aims to make it possible for developers to all use the
 * same JUnit tests and point them at different mt wilson environments without
 * having to modify the JUnit tests. So modifications should only be necessary
 * to change the tests, and not merely to change the IP address of the server or
 * the location of the api client keystore.
 *
 * THIS CLASS LOADS YOUR PERSONAL CONFIGURATION AUTOMATICALLY FROM
 * ~/.mtwilson/mtwilson.properties
 *
 * If that file does not exist, it will be automatically created with default
 * values.
 *
 * After that, you can change which Mt Wilson server you are testing against,
 * etc. by editing those properties. All JUnit tests for the api client should
 * use the "My" class (next to this one) in order to automatically pick up your
 * local settings.
 *
 * NOTE: If you need to change the location of the file from ~/.mtwilson to
 * somewhere else, you can update your Java Preferences using the
 * testSetMyConfigDir() method in this class -- look at that method's Javadoc
 * for details.
 *
 * Example Junit test:
 *
 * MyConfiguration config = new MyConfiguration();
 * KeystoreUtil.createUserInDirectory( config.getKeystoreDir(),
 * config.getKeystoreUsername(), config.getKeystorePassword(),
 * config.getMtWilsonURL(), config.getMtWilsonRoleArray()); *
 *
 *
 * NOTE: the default directory to store all your settings is ~/.mtwilson In
 * order to change it, you have to set your Java Preferences using MyPrefs --
 * see the MyPrefs class for details.
 *
 * @author jbuhacoff
 */
public class MyConfiguration {

    private Logger log = LoggerFactory.getLogger(getClass());
//    private final String PROPERTIES_FILENAME = "mtwilson.properties";
    private Preferences prefs = Preferences.userRoot().node(getClass().getName());
//    private Properties conf = new Properties();
    private Configuration conf = null;
    private HashMap<String, String> keySourceMap = new HashMap<>();

    // look in default locations
    public MyConfiguration() {
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
        while (it.hasNext()) {
            String key = it.next();
            if (!keySourceMap.containsKey(key)) {
                keySourceMap.put(key, source);
            }
        }
    }

    public String getSource(String key) {
        return keySourceMap.get(key); // will be null if the key is not defined
    }

    /**
     * Writes the key-value pair to mtwilson.properties
     */
    public void update(String key, String value) throws FileNotFoundException, IOException {
        List<File> files = listConfigurationFiles(); // in priority order - first one found for which we have write access will be updated
        for (File file : files) {
            log.debug("Looking at file: {}", file.getAbsolutePath());
            if (file.exists() && file.canRead() && file.canWrite()) {
                log.debug("Writable, checking encryption");
                // first check if the file is encrypted... if it is, we need to decrypt it before loading!
                try (FileInputStream in = new FileInputStream(file)) {
                    String content = IOUtils.toString(in);

                    if (Pem.isPem(content)) { // starts with something like -----BEGIN ENCRYPTED DATA----- and ends with -----END ENCRYPTED DATA-----
                        // a pem-format file indicates it's encrypted... we could check for "ENCRYPTED DATA" in the header and footer too.
                        String password = getApplicationConfigurationPassword();
                        if (password == null) {
                            log.warn("Found encrypted configuration file, but no password was found in system properties or environment");
                        }
                        if (password != null) {
                            ExistingFileResource resource = new ExistingFileResource(file);
                            PasswordEncryptedFile encryptedFile = new PasswordEncryptedFile(resource, password);
                            String decryptedContent = encryptedFile.loadString();
                            Properties p = new Properties();
                            p.load(new StringReader(decryptedContent));

                            p.setProperty(key, value);

                            StringWriter writer = new StringWriter();
                            p.store(writer, String.format("Changed: %s", key));
//                            log.debug("Updated: {} = {}", key, value);
                            encryptedFile.saveString(writer.toString());
                            break;
                        }
                    }
                    else {
                        log.debug("Writing plaintext properties to {}", file.getAbsolutePath());
                        Properties p = new Properties();
                        p.load(new StringReader(content));

                        p.setProperty(key, value);

                        try (FileOutputStream out = new FileOutputStream(file)) {
                            p.store(out, String.format("Changed: %s", key));
                        }
                        break;
                    }
                }
            }
        }
    }

    private Configuration gatherConfiguration(Properties customProperties) {
        CompositeConfiguration composite = new CompositeConfiguration();

        // first priority: custom properties take priority over any other source
        if (customProperties != null) {
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
                    // first check if the file is encrypted... if it is, we need to decrypt it before loading!
                    try (FileInputStream in = new FileInputStream(f)) {
                        String content = IOUtils.toString(in);

                        if (Pem.isPem(content)) { // starts with something like -----BEGIN ENCRYPTED DATA----- and ends with -----END ENCRYPTED DATA-----
                            // a pem-format file indicates it's encrypted... we could check for "ENCRYPTED DATA" in the header and footer too.
                            String password = getApplicationConfigurationPassword();
                            if (password == null) {
                                log.warn("Found encrypted configuration file, but no password was found in system properties or environment");
                            }
                            if (password != null) {
                                ExistingFileResource resource = new ExistingFileResource(f);
                                PasswordEncryptedFile encryptedFile = new PasswordEncryptedFile(resource, password);
                                String decryptedContent = encryptedFile.loadString();
                                Properties p = new Properties();
                                p.load(new StringReader(decryptedContent));
                                MapConfiguration encrypted = new MapConfiguration(p);
                                logConfiguration("encrypted-file:" + f.getAbsolutePath(), encrypted);
                                composite.addConfiguration(encrypted);
                            }
                        } else {
                            log.debug("FILE {} IS IN REGULAR PROPERTIES FORMAT", f.getAbsolutePath());
                            PropertiesConfiguration standard = new PropertiesConfiguration(f);
                            logConfiguration("file:" + f.getAbsolutePath(), standard);
                            composite.addConfiguration(standard);
                        }
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
                logConfiguration("classpath:" + propertiesFilename, classpath);
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

    private String getApplicationConfigurationPassword() {
        String password = null;
        if (System.getProperties().containsKey("mtwilson.password")) {
            password = System.getProperty("mtwilson.password");
        } else if (System.getenv().containsKey("MTWILSON_PASSWORD")) {
            password = System.getenv("MTWILSON_PASSWORD");
        }
        return password;
    }

    /**
     * This is the only method that uses the Java Preferences API to get its
     * value. Everything else uses the mtwilson.properties file located the
     * directory returned by this method.
     *
     * You can change your directory path preference from the default to any
     * directory by running the MyPrefs command from your shell -- see the
     * MyPrefs class for details
     *
     * @return ~/.mtwilson unless you have changed your preferences (see
     * testSetMyConfigDir)
     */
    public final String getDirectoryPath() {
//        return prefs.get("mtwilson.config.dir", System.getProperty("user.home") + File.separator + ".mtwilson");
        return Folders.configuration(); //getDirectory().getAbsolutePath();
    }

    public final File getDirectory() {
//        return new File(getDirectoryPath());
        return new File(Folders.configuration()); //new File(getMtWilsonConf()); // use MTWILSON_CONF instead of java preferences, so admin can set it before running setup
    }
    /* /// this one is a bad idea because the configuration can come from several places, and the config file is not the top priority source,
     * // so providing this could create a situation where someone think that by writing to this config file they can affect the configuration
     * // when in reality it may be overridden by a number of sources. 
     public final File getConfigFile() {
     return new File(getDirectoryPath() + File.separator + "mtwilson.properties");
     }*/

    /**
     * The list of files returned is in priority order -- properties defined in
     * the first file override all others. Note that when actually loading the
     * configuration, system properties and environment variables have a higher
     * priority than the configuration files.
     *
     * @return
     */
    public List<File> listConfigurationFiles() {
        // prepare a list of files to be loaded, in order
        ArrayList<File> files = new ArrayList<>();


        // fourth priority: if there is a custom configuration directory defined by a system property, load configuration from there
        String customConfigDir = System.getProperty("mtwilson.config.dir", prefs.get("mtwilson.config.dir", ""));
        if (!customConfigDir.isEmpty()) {
            files.add(new File(customConfigDir + File.separator + "mtwilson.properties"));
        }

        // fifth priority:  properties defined in user's home directory ~/.mtwilson
        files.add(new File(System.getProperty("user.home") + File.separator + ".mtwilson" + File.separator + "mtwilson.properties"));


        // sixth priority: properties defined in standard install location
        if (Platform.isWindows()) {
            files.add(new File(getMtWilsonConf() + File.separator + "mtwilson.properties")); // like C:\Intel\MtWilson\conf\mtwilson.properties
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
//             files.add(new File("C:" + File.separator + "Intel" + File.separator
//                    + "CloudSecurity" + File.separator + "PrivacyCA.properties"));

        }
        // linux-specific location
        if (Platform.isUnix()) {
            files.add(new File(getMtWilsonConf() + File.separator + "mtwilson.properties")); // like /etc/mtwilson/mtwilson.properties
//            files.add(new File("/etc/intel/cloudsecurity/" + propertiesFilename));
            files.add(new File("/etc/intel/cloudsecurity/mtwilson.properties"));
            files.add(new File("/etc/intel/cloudsecurity/management-service.properties"));
            files.add(new File("/etc/intel/cloudsecurity/attestation-service.properties"));
            files.add(new File("/etc/intel/cloudsecurity/audit-handler.properties"));
            files.add(new File("/etc/intel/cloudsecurity/mtwilson-portal.properties"));
            files.add(new File("/etc/intel/cloudsecurity/management-cmdutil.properties"));
//            files.add(new File("/etc/intel/cloudsecurity/PrivacyCA.properties"));
        }
        return files;
    }
    
    /**
     * 
     * @return File representing the configuration file mtwilson.properties in the folder returned by @{code getConfiguration()}
     */
    public File getConfigurationFile() {
        return ConfigurationFactory.getConfigurationFile();
    }
    
    

    public Configuration getConfiguration() {
        return conf;
    }

    public Properties getProperties() {
        Properties p = new Properties();
        Iterator<String> it = conf.getKeys();
        while (it.hasNext()) {
            String name = it.next();
            if (conf.containsKey(name)) {
                p.setProperty(name, conf.getString(name));
            }
        }
        return p;
    }

    public Properties getProperties(String... names) {
        Properties p = new Properties();
        for (String name : names) {
            if (conf.containsKey(name)) {
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
        return conf.getString("mtwilson.api.password");
    }

    public String getTagKeystoreUsername() {
        return conf.getString("mtwilson.tag.api.username");
    }

    public String getTagKeystorePassword() {
        return conf.getString("mtwilson.tag.api.password");
    }

    public URL getMtWilsonURL() throws MalformedURLException {
        return new URL(conf.getString("mtwilson.api.url", conf.getString("mtwilson.api.baseurl", "https://127.0.0.1:8181")));
    }

    public String getMtWilsonRoleString() {
        return conf.getString("mtwilson.api.roles", "Attestation,Whitelist,Security,Report,Audit,AssetTagManagement");
    }

    public String[] getMtWilsonRoleArray() {
        return getMtWilsonRoleString().split(",");
    }

    // use this to instantiate a client  from mtwilson-client-java7-jaxrs2
    public Properties getClientProperties() throws MalformedURLException {
        Properties properties = new Properties();
        properties.setProperty("mtwilson.api.url", getMtWilsonURL().toString());
        // x509 authentication
        if (conf.containsKey("mtwilson.api.keystore")) {
            properties.setProperty("mtwilson.api.keystore", conf.getString("mtwilson.api.keystore", getKeystoreFile().getAbsolutePath())); // getKeystoreUsername actually looks at mtwilson.api.username and mtwilson.api.password
            properties.setProperty("mtwilson.api.keystore.password", conf.getString("mtwilson.api.keystore.password", getKeystorePassword()));
            properties.setProperty("mtwilson.api.key.alias", conf.getString("mtwilson.api.key.alias", getKeystoreUsername()));
            properties.setProperty("mtwilson.api.key.password", conf.getString("mtwilson.api.key.password", getKeystorePassword()));
        }
        // hmac authentication
        if (conf.containsKey("mtwilson.api.clientId") && conf.containsKey("mtwilson.api.secretKey")) {
            properties.setProperty("mtwilson.api.clientId", conf.getString("mtwilson.api.clientId"));
            properties.setProperty("mtwilson.api.secretKey", conf.getString("mtwilson.api.secretKey"));
        }
        // basic password authentication
        if (conf.containsKey("mtwilson.api.username") && conf.containsKey("mtwilson.api.password")) {
            properties.setProperty("mtwilson.api.username", conf.getString("mtwilson.api.username"));
            properties.setProperty("mtwilson.api.password", conf.getString("mtwilson.api.password"));

        }
        
        if (conf.containsKey("mtwilson.api.tls.policy.certificate.sha1")) {
            properties.setProperty("mtwilson.api.tls.policy.certificate.sha1", conf.getString("mtwilson.api.tls.policy.certificate.sha1"));
        } else if (conf.containsKey("mtwilson.api.tls.policy.insecure")) {
            properties.setProperty("mtwilson.api.tls.policy.insecure", conf.getString("mtwilson.api.tls.policy.insecure"));
        }
        return properties;
    }

    /**
     * Returns the list of locales configured in "mtwilson.locales" , or
     * the platform default locale if there is nothing configured.
     * 
     * @return an array with at least one locale
     */
    public String[] getAvailableLocales() {
        // example property in file:  mtwilson.locales=en,en-US,es,es-MX
        // the getString(key) function will return the text only up to the first comma, e.g. "en"
        // the getStringArray(key) function never returns null,  if the key is missing or null it returns empty array, and if the value is empty string it returns an array with one element whose value is empty string
        String[] locales = conf.getStringArray("mtwilson.locales");
        if (locales == null || locales.length == 0 || locales[0] == null || locales[0].isEmpty()) {
            return new String[]{LocaleUtil.toLanguageTag(Locale.getDefault())};
        }
        return locales;
    }

    ///////////////////////// database //////////////////////////////////
    public String getDatabaseProtocol() {
        if (conf.containsKey("mtwilson.db.protocol")) {
            conf.getString("mtwilson.db.protocol", "postgresql");
        }
        if (conf.containsKey("mountwilson.as.db.protocol")) {
            conf.getString("mountwilson.as.db.protocol", "postgresql");
        } 
        if (conf.containsKey("mountwilson.ms.db.protocol")) {
            conf.getString("mountwilson.ms.db.protocol", "postgresql");
        } 
        if (conf.containsKey("mtwilson.db.driver")) {
            String driver = conf.getString("mtwilson.db.driver", "");
            if (driver.equals("org.postgresql.Driver")) {
                return "postgresql";
            }
            if (driver.equals("com.mysql.jdbc.Driver")) {
                return "mysql";
            }
        }
        if (conf.containsKey("mtwilson.db.port")) {
            String port = conf.getString("mtwilson.db.port", "");
            if (port.equals("5432")) {
                return "postgresql";
            }
            if (port.equals("3306")) {
                return "mysql";
            }
        }
        return "postgresql";  // used in the jdbc url, so "postgresql" or "mysql"  as in jdbc:mysql://host:port/schema
    }

    public String getDatabaseDriver() {
        if (conf.containsKey("mtwilson.db.driver")) {
            conf.getString("mtwilson.db.driver", "org.postgresql.Driver");
        }
        if (conf.containsKey("mountwilson.as.db.driver")) {
            conf.getString("mountwilson.as.db.driver", "org.postgresql.Driver");
        } 
        if (conf.containsKey("mountwilson.ms.db.driver")) {
            conf.getString("mountwilson.ms.db.driver", "org.postgresql.Driver");
        } 
        if (conf.containsKey("mtwilson.db.protocol")) {
            String protocol = conf.getString("mtwilson.db.protocol", "");
            if (protocol.equals("postgresql")) {
                return "org.postgresql.Driver";
            }
            if (protocol.equals("mysql")) {
                return "com.mysql.jdbc.Driver";
            }
        }
        if (conf.containsKey("mtwilson.db.port")) {
            String port = conf.getString("mtwilson.db.port", "");
            if (port.equals("5432")) {
                return "org.postgresql.Driver";
            }
            if (port.equals("3306")) {
                return "com.mysql.jdbc.Driver";
            }
        }
        return "org.postgresql.Driver"; // either "org.postgresql.Driver" or "com.mysql.jdbc.Driver"
    }

    public String getDatabasePort() {
        if (conf.containsKey("mtwilson.db.port")) {
            conf.getString("mtwilson.db.port", "5432");
        }
        if (conf.containsKey("mountwilson.as.db.port")) {
            conf.getString("mountwilson.as.db.port", "5432");
        } 
        if (conf.containsKey("mountwilson.ms.db.port")) {
            conf.getString("mountwilson.ms.db.port", "5432");
        } 
        if (conf.containsKey("mtwilson.db.protocol")) {
            String protocol = conf.getString("mtwilson.db.protocol", "");
            if (protocol.equals("postgresql")) {
                return "5432";
            }
            if (protocol.equals("mysql")) {
                return "3306";
            }
        }
        if (conf.containsKey("mtwilson.db.driver")) {
            String port = conf.getString("mtwilson.db.driver", "");
            if (port.equals("org.postgresql.Driver")) {
                return "5432";
            }
            if (port.equals("com.mysql.jdbc.Driver")) {
                return "3306";
            }
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

    private File findConfigurationFile(String path) {
        File f = new File(path);
        if (f.isAbsolute()) {
            return f;
        } else {
            return new File(getMtWilsonConf() + File.separator + path);
        }
    }

    /////////////////////////            rootca key            //////////////////////////////////
    public File getRootCaCertificateFile() {
        return findConfigurationFile(conf.getString("mtwilson.rootca.certificate.file", "MtWilsonRootCA.crt.pem"));
    }

    /////////////////////////             pca key              //////////////////////////////////
    public File getPrivacyCaIdentityCacertsFile() {
        return findConfigurationFile(conf.getString("mtwilson.privacyca.aik.cacerts.file", "PrivacyCA.pem")); // list of all approved aik signing certificates from this server and other servers
    }

    public File getPrivacyCaIdentityP12() {
        return findConfigurationFile(conf.getString("mtwilson.privacyca.aik.p12.file", "PrivacyCA.p12")); // ek signing certificate
    }

    public String getPrivacyCaIdentityPassword() {
        return conf.getString("mtwilson.privacyca.aik.p12.password"); // there must not be a default; the password must be generated by the installer 
    }

    public Integer getPrivacyCaIdentityValidityDays() {
        return conf.getInteger("mtwilson.privacyca.aik.validity.days", 3652); // default to approx. 10 years validity period for endorsed TPM EK
    }

    public String getPrivacyCaIdentityIssuer() {
        return conf.getString("mtwilson.privacyca.aik.issuer", "mtwilson-pca-aik"); // default to approx. 10 years validity period for endorsed TPM EK
    }

    public File getPrivacyCaEndorsementCacertsFile() {
        return findConfigurationFile(conf.getString("mtwilson.privacyca.ek.cacerts.file", "EndorsementCA.pem")); // list of all approved ek signing certificates from this server and other servers and tpm manufacturers
    }

    public File getPrivacyCaEndorsementP12() {
        return findConfigurationFile(conf.getString("mtwilson.privacyca.ek.p12.file", "EndorsementCA.p12")); // our own ek signing certificate and private key
    }

    public String getPrivacyCaEndorsementPassword() {
        return conf.getString("mtwilson.privacyca.ek.p12.password"); // there must not be a default; the password must be generated by the installer 
    }

    public Integer getPrivacyCaEndorsementValidityDays() {
        return conf.getInteger("mtwilson.privacyca.ek.validity.days", 3652); // default to approx. 10 years validity period for endorsed TPM EK
    }

    public String getPrivacyCaEndorsementIssuer() {
        return conf.getString("mtwilson.privacyca.ek.issuer", "mtwilson-pca-ek"); // default to approx. 10 years validity period for endorsed TPM EK
    }

    ///////////////////////// saml key for attestation service //////////////////////////////////
    public File getSamlCertificateFile() {
        return findConfigurationFile(conf.getString("mtwilson.saml.certificate.file", "saml.crt.pem"));
    }

    public File getSamlKeystoreFile() {
        return findConfigurationFile(conf.getString("saml.keystore.file", "mtwilson-saml.jks"));
    }

    public String getSamlKeystorePassword() {
        return conf.getString("saml.key.password"); // bug #733 
    }
    
    public String getSamlKeyAlias() {
        return conf.getString("saml.key.alias"); 
    }

    public Integer getSamlValidityTimeInSeconds() {
        return conf.getInteger("saml.validity.seconds", 3600);
    }
    ///////////////////////// tls policy  //////////////////////////////////
    public String getGlobalTlsPolicyId() {
        return conf.getString("mtwilson.global.tls.policy.id"); // no default - when a value is present it means all per-host and default tls policy settings will be ignored
    }
    public String getDefaultTlsPolicyId() {
        return conf.getString("mtwilson.default.tls.policy.id"); // no default - when a value is present it is used whenever a tls connection needs to be made but no per-request or per-host tls policy was specified
    }
    public Set<String> getTlsPolicyAllow() {
        String[] allowed = conf.getStringArray("mtwilson.tls.policy.allow");
        if( allowed.length == 0 ) {
            allowed = new String[] { "certificate", "certificate-digest" }; // the other possible values which are intentionally not included in the default list are public-key, public-key-digest, INSECURE and TRUST_FIRST_CERTIFICATE
        }
        return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(allowed)));
    }
    
    public File getTlsKeystoreFile() {
        return new File(conf.getString("mtwilson.tls.keystore.file", getMtWilsonConf() + File.separator + "mtwilson-tls.jks"));
    }

    public String getTlsKeystorePassword() {
        return conf.getString("mtwilson.tls.keystore.password"); // Intentionally not providing a default password;  the mtwilson-server install script automatically generates a password for new installs. 
    }

    public boolean getAutoUpdateHosts() {
        return conf.getBoolean("mtwilson.as.autoUpdateHost", false);
    }

    // asset tagging html5 resources (used by the reference implementation)
    public String getAssetTagHtml5Dir() {
        return conf.getString("mtwilson.atag.html5.dir", "clap://html5/"); // the clap protocol means classpath for the restlet engine
    }

    public File getAssetTagCaCertificateFile() {
        return findConfigurationFile(conf.getString("mtwilson.tag.cacerts.file", "tag-cacerts.pem"));
    }

    // asset tag server url
    public URL getAssetTagServerURL() throws MalformedURLException {
        return new URL(conf.getString("mtwilson.atag.url", "https://localhost:9999"));
    }

    public String getAssetTagServerString() throws MalformedURLException {
        return conf.getString("mtwilson.atag.url", "https://localhost:9999");
    }

    /*
     public String getAssetTagKeyStorePath() {
     return conf.getString("mtwilson.atag.keystore", "serverAtag.jks");
     }
    
     public String getAssetTagKeyStorePassword() {
     return conf.getString("mtwilson.atag.keystore.password"); // must not have default password; run setup
     }
    
     public String getAssetTagKeyPassword() {
     return conf.getString("mtwilson.atag.key.password");// must not have default password; run setup
     }
     */
    public String getAssetTagApiUsername() {
        return conf.getString("mtwilson.atag.api.username", "admin");
    }

    public String getAssetTagApiPassword() {
        return conf.getString("mtwilson.atag.api.password"); // must not have default password; run setup
    }

    public Boolean getAssetTagAutoImport() {
        return conf.getBoolean("mtwilson.atag.certificate.import.auto", true);
    }

    public String getAssetTagMtWilsonBaseUrl() {
        return conf.getString("mtwilson.atag.mtwilson.baseurl", "");
    }

    public int getAssetTagCertificateValidityPeriod() {
        return conf.getInt("mtwilson.atag.certificate.validity.period", 365);
    }

    public boolean getAssetTagAutoDeploy() {
        return conf.getBoolean("mtwilson.atag.certificate.deploy.auto", false);
    }

    ///////////////////////// mtwilson portal  //////////////////////////////////
    public String getPortalHtml5Dir() {
        return conf.getString("mtwilson.portal.html5.dir");
    }

    ///////////////////////// filesystem locations  //////////////////////////////////
    /**
     *
     * @return /opt/mtwilson on Linux or value of MTWILSON_HOME
     */
    public String getMtWilsonHome() {
        /*
        String mtwilsonHome = System.getenv("MTWILSON_HOME");
        log.debug("MTWILSON_HOME={}", mtwilsonHome);
        if (mtwilsonHome == null) {
            if (Platform.isUnix()) {
                mtwilsonHome = "/opt/mtwilson";
                log.debug("MTWILSON_HOME={} (Linux default)", mtwilsonHome);
            }
            if (Platform.isWindows()) {
                mtwilsonHome = "C:" + File.separator + "mtwilson"; // applications in Program Files need administrator permission to write to their folders 
                log.debug("MTWILSON_HOME={} (Windows default)", mtwilsonHome);
            }
        }
        if (mtwilsonHome == null) {
            throw new IllegalStateException("MTWILSON_HOME environment variable must be defined");
        }
        return mtwilsonHome;
        */
        return Folders.application();
    }

    /**
     *
     * @return /etc/mtwilson on Linux or value of MTWILSON_CONF
     */
    public String getMtWilsonConf() {
        /*
        String mtwilsonConf = System.getenv("MTWILSON_CONF");
        log.debug("MTWILSON_CONF={}", mtwilsonConf);
        if (mtwilsonConf == null) {
            if (Platform.isUnix()) {
                //mtwilsonConf = "/etc/mtwilson";
                mtwilsonConf = "/etc/intel/cloudsecurity";
                log.debug("MTWILSON_CONF={} (Linux default)", mtwilsonConf);
            }
            if (Platform.isWindows()) {
                mtwilsonConf = getMtWilsonHome() + File.separator + "configuration";
                log.debug("MTWILSON_CONF={} (Windows default)", mtwilsonConf);
            }
        }
        if (mtwilsonConf == null) {
            throw new IllegalStateException("MTWILSON_CONF environment variable must be defined");
        }
        return mtwilsonConf;
        */
        return Folders.configuration();
    }

    /**
     *
     * @return /opt/mtwilson/bin on Linux or MTWILSON_HOME/bin
     */
    public String getMtWilsonBin() {
        return getMtWilsonHome() + File.separator + "bin";
    }

    /**
     *
     * @return /opt/mtwilson/env.d on Linux or MTWILSON_HOME/env.d
     */
    public String getMtWilsonEnv() {
        return getMtWilsonHome() + File.separator + "env.d";
    }

    /**
     *
     * @return /opt/mtwilson/java on Linux or MTWILSON_HOME/java or
     * MTWILSON_JAVA
     */
    public String getMtWilsonJava() {
        String mtwilsonJava = System.getenv("MTWILSON_JAVA");
        log.debug("MTWILSON_JAVA={}", mtwilsonJava);
        /*
        if (mtwilsonJava == null) {
            mtwilsonJava = conf.getString("mtwilson.fs.java");
        }
        */
        if (mtwilsonJava == null) {
            mtwilsonJava = getMtWilsonHome() + File.separator + "java";
        }
        return mtwilsonJava;
    }

    /**
     *
     * @return /opt/mtwilson/util.d on Linux or MTWILSON_HOME/util.d
     */
    public String getMtWilsonUtil() {
        return getMtWilsonHome() + File.separator + "util.d";
    }

    /**
     *
     * @return /opt/mtwilson/resource on Linux or MTWILSON_HOME/resource
     */
    public String getMtWilsonResource() {
        return getMtWilsonHome() + File.separator + "resource";
    }

    /**
     *
     * @return /opt/mtwilson/license.d on Linux or MTWILSON_HOME/license.d
     */
    public String getMtWilsonLicense() {
        return getMtWilsonHome() + File.separator + "license.d";
    }

    ///////////////////////// certificate authority //////////////////////////////////
    public File getCaKeystoreFile() {
        return new File(getMtWilsonConf() + File.separator + "cakey.pem");
    }
    
    public File getCaCertsFile() {
        return new File(getMtWilsonConf() + File.separator + "cacerts.pem");
    }

    ///////////////////////// anti-replay protection //////////////////////////////////
    public int getAntiReplayProtectionWindowMilliseconds() {
        return conf.getInt("mtwilson.security.x509.request.expires", 60 * 60 * 1000); // default 1 hour
    }
}
