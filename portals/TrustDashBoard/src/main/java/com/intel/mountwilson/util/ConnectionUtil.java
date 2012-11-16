/**
 * 
 */
package com.intel.mountwilson.util;

import java.io.IOException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import com.intel.mountwilson.common.DemoPortalException;
import com.intel.mtwilson.ApiException;
import com.sun.jersey.api.client.ClientHandlerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yuvrajsX
 *
 */
public class ConnectionUtil {
        static Logger log = LoggerFactory.getLogger(ConnectionUtil.class.getName());
	
	public static DemoPortalException handleException(Exception exceptionObject) throws DemoPortalException{
		exceptionObject.printStackTrace();
		if(exceptionObject.getClass().equals(ClientHandlerException.class)){
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
			
			/* Soni_Begin_17/09/2012_issue_for_consistent_Error_Message  */
			ApiException ae=(ApiException) exceptionObject;
            return new DemoPortalException(ae.getMessage());
        	/* Soni_End_17/09/2012_issue_for_consistent_Error_Message  */
            //return new DemoPortalException("ApiException."+exceptionObject.getMessage(),exceptionObject);
		}
		if (exceptionObject.getClass().equals(IllegalArgumentException.class)) {
			return new DemoPortalException("IllegalArgumentException: "+exceptionObject.getMessage(),exceptionObject);
		}
		// Bug: 445 - To remove the "Error Cause" from the exception information.		
		return new DemoPortalException(exceptionObject.getMessage(),exceptionObject);
	}
	
}
