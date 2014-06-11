/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.user.management.exceptions;

import com.intel.mtwilson.i18n.ErrorCode;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author ssbangal
 */
public class RepositoryException extends RuntimeException {

    private ErrorCode errorCode; // The errorCode enum would map to a particular error message which would have place holder for the parameters
    private final Map<String,Object> properties = new TreeMap<>(); // To capture the parameters to be used when building the error message
    
    public RepositoryException() {
        super();
    }

    public RepositoryException(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }        
    
    public ErrorCode getErrorCode() {
        return errorCode;
    }
    
    public RepositoryException setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
        return this;
    }
	
    public Map<String, Object> getProperties() {
            return properties;
    }
	
    @SuppressWarnings("unchecked")
    public <T> T get(String name) {
        return (T)properties.get(name);
    }
	
    public RepositoryException set(String name, Object value) {
        properties.put(name, value);
        return this;
    }    
}
