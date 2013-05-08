/**
 * This class is used to handle Exceptions, also return specific message based on the exception type. 
 */
package com.intel.mountwilson.util;

import com.intel.mountwilson.common.ManagementConsolePortalException;
import com.intel.mtwilson.ApiException;
import com.sun.jersey.api.client.ClientHandlerException;
import java.io.IOException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yuvrajsX
 *
 */
public class ConnectionUtil {
	
                        private static final Logger log = LoggerFactory.getLogger(ConnectionUtil.class.getName());
	
	// method to take a common Exception and return specific error based on Exception type.
	public static ManagementConsolePortalException handleException(Exception exceptionObject) throws ManagementConsolePortalException{
		exceptionObject.printStackTrace();
		if(exceptionObject.getClass().equals(ClientHandlerException.class)){
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
}
