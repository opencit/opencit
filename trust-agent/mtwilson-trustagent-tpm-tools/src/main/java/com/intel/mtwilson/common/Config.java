/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.common;

import com.intel.mtwilson.Folders;
import java.io.File;
import org.apache.commons.configuration.Configuration;
import org.slf4j.LoggerFactory;



/**
 *
 * @author dsmagadX
 */
public class Config {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Config.class);

    private static Configuration config = TAConfig.getConfiguration();
    private static Config instance = null;
    //private static String appPath = config.getString("app.path"); // System.getProperty("app.path",".");;
    private static Boolean debug;

    private static String homeFolder = "./config";

    public static String getHomeFolder() {
		return homeFolder;
	}


	public static void setHomeFolder(String homeFolder) {
		Config.homeFolder = homeFolder;
	}



	static{
//        TrustagentConfiguration configuration = TrustagentConfiguration.loadConfiguration();
//		File propFile = new File(MyFilesystem.getApplicationFilesystem().getConfigurationPath() + File.separator + "trustagent.properties");
        homeFolder = Folders.configuration();
        log.debug("Home folder. Using " + homeFolder);
    }
    
    public static boolean isDebug() {
        if( debug == null ) {
            debug =  config.getString("debug").equalsIgnoreCase("true");
        }
        return debug;
    }
    
    
    private Config() {
    }
    
    public static Config getInstance() {
        if(instance == null){
            instance = new Config();
        }
        
        return instance;
    }
    
    public String getProperty(String property){
        if( config.containsKey(property) ) {
            return config.getString(property);
        }
        else {
            log.warn("Property {} missing in config file.", property);
            return null;
        }
    }
    
    
     
    public static String getAppPath(){
        return Folders.application();
    }
    
    
    
    public static String getBinPath() {
        return Folders.application() + File.separator + "bin"; //.getBinPath();
    }
    
    
}
