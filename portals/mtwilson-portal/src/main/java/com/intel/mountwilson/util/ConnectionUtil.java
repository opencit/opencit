/**
 * This class is used to handle Exceptions, also return specific message based on the exception type. 
 */
package com.intel.mountwilson.util;

import com.intel.mountwilson.common.DemoPortalException;
import com.intel.mountwilson.common.ManagementConsolePortalException;
import com.intel.mountwilson.common.WLMPortalException;
import com.intel.mtwilson.api.*;
import javax.ws.rs.client.ResponseProcessingException;
//import com.sun.jersey.api.client.ClientHandlerException;
import java.io.IOException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
//import org.codehaus.jackson.JsonParseException;
//import org.codehaus.jackson.map.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yuvrajsX
 *
 */
public class ConnectionUtil {
	
                        private static final Logger log = LoggerFactory.getLogger(ConnectionUtil.class.getName());
	
	// method to take a common Exception and return specific error based on Exception type.
	public static ManagementConsolePortalException handleManagementConsoleException(Exception exceptionObject) throws ManagementConsolePortalException{
		exceptionObject.printStackTrace();
		//if(exceptionObject.getClass().equals(ClientHandlerException.class)){
                if(exceptionObject.getClass().equals(ResponseProcessingException.class)){
			return new ManagementConsolePortalException("Could not able to Connect to Server. Error Connection Refused.",exceptionObject);
		}
		if (exceptionObject.getClass().equals(JsonParseException.class)) {
			log.error("Error While Parsing Data. "+exceptionObject.getMessage());
			return new ManagementConsolePortalException("Error While parsing Data Using Jackson.",exceptionObject);
		}
		if (exceptionObject.getClass().equals(JsonMappingException.class)) {
			log.error("Error While Mapping Data. "+exceptionObject.getMessage());
			return new ManagementConsolePortalException("Error While Mapping Data Using Jackson.",exceptionObject);
		}
		if (exceptionObject.getClass().equals(IOException.class)) {
			return new ManagementConsolePortalException("IOEception."+exceptionObject.getMessage(),exceptionObject);
		}
		if (exceptionObject.getClass().equals(ApiException.class)) {
			ApiException ae=(ApiException) exceptionObject;
                                                                        // Added the error code to the display of the message                        
                                                                        return new ManagementConsolePortalException(ae.getMessage() + "[" + ae.getErrorCode() + "]");
		}
		if (exceptionObject.getClass().equals(IllegalArgumentException.class)) {
			return new ManagementConsolePortalException("IllegalArgumentException: "+exceptionObject.getMessage(),exceptionObject);
		}
		
		return new ManagementConsolePortalException(exceptionObject.getMessage(),exceptionObject);
	}
    
    
	// method to take a common Exception and return specific error based on Exception type.
	public static DemoPortalException handleDemoPortalException(Exception exceptionObject) throws DemoPortalException{
		exceptionObject.printStackTrace();
		//if(exceptionObject.getClass().equals(ClientHandlerException.class)){
                if(exceptionObject.getClass().equals(ResponseProcessingException.class)){
			return new DemoPortalException("Could not able to Connect to Server. Error Connection Refused.",exceptionObject);
		}
		if (exceptionObject.getClass().equals(JsonParseException.class)) {
			log.error("Error While Parsing Data. "+exceptionObject.getMessage());
			return new DemoPortalException("Error While parsing Data Using Jackson.",exceptionObject);
		}
		if (exceptionObject.getClass().equals(JsonMappingException.class)) {
			log.error("Error While Mapping Data. "+exceptionObject.getMessage());
			return new DemoPortalException("Error While Mapping Data Using Jackson.",exceptionObject);
		}
		if (exceptionObject.getClass().equals(IOException.class)) {
			return new DemoPortalException("IOEception."+exceptionObject.getMessage(),exceptionObject);
		}
		if (exceptionObject.getClass().equals(ApiException.class)) {
			ApiException ae=(ApiException) exceptionObject;
                                                                        // Added the error code to the display of the message                        
                                                                        return new DemoPortalException(ae.getMessage() + "[" + ae.getErrorCode() + "]");
		}
		if (exceptionObject.getClass().equals(IllegalArgumentException.class)) {
			return new DemoPortalException("IllegalArgumentException: "+exceptionObject.getMessage(),exceptionObject);
		}
		
		return new DemoPortalException(exceptionObject.getMessage(),exceptionObject);
	}    
    

	// method to take a common Exception and return specific error based on Exception type.
	public static WLMPortalException handleWLMPortalException(Exception exceptionObject) throws WLMPortalException{
		exceptionObject.printStackTrace();
		//if(exceptionObject.getClass().equals(ClientHandlerException.class)){
                if(exceptionObject.getClass().equals(ResponseProcessingException.class)){
			return new WLMPortalException("Could not able to Connect to Server. Error Connection Refused.",exceptionObject);
		}
		if (exceptionObject.getClass().equals(JsonParseException.class)) {
			log.error("Error While Parsing Data. "+exceptionObject.getMessage());
			return new WLMPortalException("Error While parsing Data Using Jackson.",exceptionObject);
		}
		if (exceptionObject.getClass().equals(JsonMappingException.class)) {
			log.error("Error While Mapping Data. "+exceptionObject.getMessage());
			return new WLMPortalException("Error While Mapping Data Using Jackson.",exceptionObject);
		}
		if (exceptionObject.getClass().equals(IOException.class)) {
			return new WLMPortalException("IOEception."+exceptionObject.getMessage(),exceptionObject);
		}
		if (exceptionObject.getClass().equals(ApiException.class)) {
			ApiException ae=(ApiException) exceptionObject;
                                                                        // Added the error code to the display of the message                        
                                                                        return new WLMPortalException(ae.getMessage() + "[" + ae.getErrorCode() + "]");
		}
		if (exceptionObject.getClass().equals(IllegalArgumentException.class)) {
			return new WLMPortalException("IllegalArgumentException: "+exceptionObject.getMessage(),exceptionObject);
		}
		
		return new WLMPortalException(exceptionObject.getMessage(),exceptionObject);
	}        
}
