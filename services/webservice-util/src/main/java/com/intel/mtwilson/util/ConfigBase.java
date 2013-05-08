package com.intel.mtwilson.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import org.apache.commons.configuration.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Attempts to use commons-configuration to load the Attestation Service
 * settings.
 *
 * The configuration is loaded in the following priority order: System
 * properties Properties in the file attestation-service.properties (create this
 * file in your classpath or home directory to customize local settings)
 * Properties in the file attestation-service-defaults.properties (included with
 * ASCommon) Hard-coded defaults (defined in this class)
 */
public abstract class ConfigBase {

    private Logger log = LoggerFactory.getLogger(getClass());
    private Configuration config = null;
    private final String propertyFileName;
    private Properties defaultProperties ;//= new Properties();
    private Properties customProperties = null;
    
    public Configuration getConfigurationInstance() {
        if( config == null ) {
            defaultProperties = getDefaults();
            config = gatherConfiguration(propertyFileName, defaultProperties);
        }
        return config;
    }

    public ConfigBase(String propertyFileName/*, Properties defaultProperties*/) {
        this.propertyFileName = propertyFileName;
//        this.defaultProperties = defaultProperties;
    }
    public ConfigBase(String propertyFileName, Properties custom) {
        this.propertyFileName = propertyFileName;
        this.customProperties = custom;
    }

    public Properties getDefaults()  { return new Properties(); }
    
    // for troubleshooting
    private void dumpConfiguration(Configuration c, String label) {
//        if( defaultProperties == null ) { System.out.println("WTF DEFAULT PROPERTIES IS NULL"); return; }
//        if( defaultProperties.stringPropertyNames() == null ) { System.out.println("WTF NO STRING PROPERTY NAMES"); return; }
        HashSet<String> keyset = new HashSet<String>(defaultProperties.stringPropertyNames()); // make a copy because we will add to it
        String keys[] = new String[]{"mtwilson.api.baseurl",
            "mtwilson.api.ssl.verifyHostname", 
            "mtwilson.db.driver", "mtwilson.db.host", "mtwilson.db.port", "mtwilson.db.schema", "mtwilson.db.user", "mtwilson.db.password" };
        keyset.addAll(Arrays.asList(keys));
        for (String key : keyset) {
            String value = c.getString(key);
            log.debug(String.format("%s [%s]: %s=%s", getClass().getSimpleName(), label, key, value)); // label==null?"null":label), (key==null?"null":key), (value==null?"null":value)));
        }
    }
            
    // stdalex 1/23 massive remove of println statements to decrese logging to installer

    private Configuration gatherConfiguration(String propertiesFilename,
            Properties defaults) {
        CompositeConfiguration composite = new CompositeConfiguration();

        if( customProperties != null ) {
            MapConfiguration customconfig = new MapConfiguration(customProperties);
            dumpConfiguration(customconfig, "custom");
            composite.addConfiguration(customconfig);
        }
        
        // first priority are properties defined on the current JVM (-D switch
        // or through web container)
        SystemConfiguration system = new SystemConfiguration();
        dumpConfiguration(system, "system");
        composite.addConfiguration(system);

        // second priority are properties defined on the classpath (like user's
        // home directory)
        InputStream in = getClass().getResourceAsStream(
                "/" + propertiesFilename);
        try {
            // user's home directory (assuming it's on the classpath!)
            if (in != null) {
                Properties properties = new Properties();
                properties.load(in);
                MapConfiguration classpath = new MapConfiguration(properties);
                dumpConfiguration(classpath, "classpath:" + propertiesFilename);
                composite.addConfiguration(classpath);
            }
        } catch (IOException ex) {
            log.info("Did not find [" + propertiesFilename + "] properties on classpath",
                    ex);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    log.warn("Failed to close input stream for "
                            + propertiesFilename);
                }
            }
        }

        // third priority are properties defined in standard install location
        //System.out.println("ConfigBase os.name="
        //		+ System.getProperty("os.name"));
        ArrayList<File> files = new ArrayList<File>();
        // windows-specific location
        if (System.getProperty("os.name", "").toLowerCase().contains("win")) {
            //System.out.println("ConfigBase user.home="
            //		+ System.getProperty("user.home"));
            files.add(new File("C:" + File.separator + "Intel" + File.separator
                    + "CloudSecurity" + File.separator + propertiesFilename));
            files.add(new File(System.getProperty("user.home") + File.separator
                    + propertiesFilename));

            files.add(new File("C:" + File.separator + "Intel" + File.separator
                    + "CloudSecurity" + File.separator + "mtwilson.properties"));

        }
        // linux-specific location
        if (System.getProperty("os.name", "").toLowerCase().contains("linux")
                || System.getProperty("os.name", "").toLowerCase()
                .contains("unix")) {
            files.add(new File("/etc/intel/cloudsecurity/" + propertiesFilename));
            files.add(new File("/etc/intel/cloudsecurity/mtwilson.properties"));
        }
        // add all the files we found
        for (File f : files) {
//            System.out.println("Looking for "+f.getAbsolutePath());
            try {
                if (f.exists() && f.canRead()) {
                    PropertiesConfiguration standard = new PropertiesConfiguration(
                            f);
                    dumpConfiguration(standard, "file:" + f.getAbsolutePath());
                    composite.addConfiguration(standard);
                }
            } catch (ConfigurationException ex) {
                log.error("Cannot load configuration from " + f.getAbsolutePath(), ex);
            }
        }

        // last priority are the defaults that were passed in, we use them if no
        // better source was found
        if (defaults != null) {
            MapConfiguration defaultconfig = new MapConfiguration(defaults);
            dumpConfiguration(defaultconfig, "default");
            composite.addConfiguration(defaultconfig);
        }

        dumpConfiguration(composite, "composite");
        return composite;
    }
    /*

     public static Configuration loadConfiguration(String propertiesFilename) {
     CompositeConfiguration composite = new CompositeConfiguration();

     // first priority are properties defined on the current JVM (-D switch
     // or through web container)
     SystemConfiguration system = new SystemConfiguration();
     dumpConfiguration(system, "system");
     composite.addConfiguration(system);
        
     // second priority are properties defined on the classpath (like user's
     // home directory, or junit resources folder if invoked from IDE)
     try {
     List<Configuration> classpath = new ArrayList<Configuration>();
     classpath.add(readConfigurationFileFromClasspath("/" + propertiesFilename));
     classpath.add(readConfigurationFileFromClasspath(propertiesFilename));
     for( Configuration c : classpath ) {
     if( c != null ) {
     composite.addConfiguration(c);
     }
     }
     } catch (IOException ex) {
     log.info(
     "Did not find " + propertiesFilename + " on classpath", ex);
     }
        
     // third priority are properties defined in standard install location
     // for the operating system
     List<File> filesystem = findConfigurationFile(propertiesFilename);
     for (File f : filesystem) {
     try {
     if (f.exists() && f.canRead()) {
     PropertiesConfiguration standard = new PropertiesConfiguration(
     f);
     dumpConfiguration(standard, "file:" + f.getAbsolutePath());
     composite.addConfiguration(standard);
     }
     } catch (ConfigurationException ex) {
     log.info(ex.toString(), ex);
     }
     }
        
     // last priority are built-in defaults
     //composite.addConfiguration(getDefaultConfiguration());
        
     dumpConfiguration(composite, "composite");
     return composite;
        
     }
    

     private static Configuration readConfigurationFileFromClasspath(String filename) throws IOException {
     InputStream in = ConfigurationFactory.class.getResourceAsStream(filename);
     if (in != null) {
     try {
     Properties properties = new Properties();
     properties.load(in);
     MapConfiguration classpath = new MapConfiguration(properties);
     dumpConfiguration(classpath, "classpath:/" + filename);
     return classpath;
     } finally {
     in.close();
     }
     }
     return null;
     }
    
    
     private static List<File> findConfigurationFile(String propertiesFilename) {
     log.debug("os.name=" + System.getProperty("os.name"));
     ArrayList<File> files = new ArrayList<File>();
     // current directory
     files.add(new File(propertiesFilename));
     // windows-specific location
     if (System.getProperty("os.name", "").toLowerCase().contains("win")) {
     log.debug("user.home="
     + System.getProperty("user.home"));
     files.add(new File("C:" + File.separator + "Intel" + File.separator
     + "CloudSecurity" + File.separator + propertiesFilename));
     files.add(new File(System.getProperty("user.home") + File.separator
     + propertiesFilename));
     }
     // linux-specific location
     if (System.getProperty("os.name", "").toLowerCase().contains("linux")
     || System.getProperty("os.name", "").toLowerCase().contains("unix")) {
     files.add(new File("/etc/intel/cloudsecurity/" + propertiesFilename));
     }
     return files;
     }
    
     * 
     */
}
