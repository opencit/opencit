package com.intel.dcsg.cpg.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.MapConfiguration;

/**
 * Utility for conveniently loading non-encrypted Java Properties files as
 * Apache Commons Configuration instances.
 *
 * @since 0.5.2
 * @author jbuhacoff
 */
public final class CommonsConfigurationUtil {
    /**
     * Does NOT close the InputStream
     * 
     * @param properties
     * @return
     * @throws IOException 
     */
    public static Configuration fromInputStream(InputStream properties) throws IOException {
        Properties p = new Properties();
        p.load(properties);
        return new MapConfiguration(p);        
    }
    
    // resource must be visible to classloader of THIS class
    public static Configuration fromResource(String resourceName) throws IOException {
        try(InputStream in = CommonsConfigurationUtil.class.getResourceAsStream(resourceName)) {
            return fromInputStream(in);
        }
    }
    
    /**
     * The Apache Commons Configuration PropertiesConfiguration class is very lax about what it
     * allows in a properties file. Keys can have equal signs, colons, or spaces to separate them
     * from values. This convenience method loads the named file using a Java Properties object
     * and then creates a Configuration object from that. This enforces the Java properties format.
     * @param propertiesFile
     * @return
     * @throws ConfigurationException 
     */
    public static Configuration fromPropertiesFile(File propertiesFile) throws IOException {
        FileInputStream in = new FileInputStream(propertiesFile);
        try {
            return fromInputStream(in);
        }
        finally {
            in.close();
        }
    }
}
