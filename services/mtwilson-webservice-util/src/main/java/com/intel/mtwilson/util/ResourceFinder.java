package com.intel.mtwilson.util;

import com.intel.mtwilson.My;
import com.intel.mtwilson.MyFilesystem;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Locates a resource such as a configuration file.
 * 
 * The resource search locations, in order:
 * Classpath
 * Standard configuration locations for windows/linux
 * 
 * @author jbuhacoffResourceFinder
 */
public class ResourceFinder {
    private static Logger log = LoggerFactory.getLogger(ResourceFinder.class);
    
    // returns a File from which you can getAbsolutePath or wrap with FileInputStream
    public static File getFile(String filename) throws FileNotFoundException {
        // try standard install locations        
//        System.out.println("ResourceFinder os.name="+System.getProperty("os.name"));
        ArrayList<File> files = new ArrayList<File>();
        // first try an absolute filename or relative to current directory
        files.add(new File(filename));
        // then try a location relative to application configuration dir and then application dir
        files.add(new File(MyFilesystem.getApplicationFilesystem().getConfigurationPath()+File.separator+filename));
        files.add(new File(MyFilesystem.getApplicationFilesystem().getApplicationPath()+File.separator+filename));
        // windows-specific location
        if( System.getProperty("os.name", "").toLowerCase().contains("win") ) {
            System.out.println("ResourceFinder user.home="+System.getProperty("user.home"));
            files.add(new File("C:"+File.separator+"Intel"+File.separator+"CloudSecurity"+File.separator+filename));
            files.add(new File(System.getProperty("user.home")+File.separator+filename));
        }
        // linux-specific location
        if( System.getProperty("os.name", "").toLowerCase().contains("linux") || System.getProperty("os.name", "").toLowerCase().contains("unix") ) {
            files.add(new File("./config/"+filename));
            files.add(new File("/etc/intel/cloudsecurity/"+filename));
            files.add(new File(System.getProperty("user.home")+File.separator+filename));
        }
        // try all the files we found
        for(File f : files) {
            if( f.exists() && f.canRead() ) {
                return f;
            }
        }
        
        throw new FileNotFoundException("cannot find "+filename+" [os.name="+System.getProperty("os.name")+"]");        
    }
    
    public static URL getURL(String filename) throws FileNotFoundException {
        // try classpath
        URL relativeClasspathResource = ResourceFinder.class.getResource(filename);
        if( relativeClasspathResource != null ) {
            return relativeClasspathResource;
        }
        URL absoluteClasspathResource = ResourceFinder.class.getResource("/"+filename);
        if( absoluteClasspathResource != null ) {
            return absoluteClasspathResource;
        }

        try {
            File f = getFile(filename);
            return f.toURI().toURL();
        } catch (MalformedURLException ex) {
            log.error("Invalid path or URL: "+filename, ex);
        } // not catching FileNotFoundException because if we don't find it here we're throwing the same exception anyway
        
        throw new FileNotFoundException("Cannot find "+filename);        

    } 
    
    // retained for compatibility with previous version of this class which did not have the getURL() method
    public static String getLocation(String filename) throws FileNotFoundException {
        return getURL(filename).toExternalForm();
    }
}
