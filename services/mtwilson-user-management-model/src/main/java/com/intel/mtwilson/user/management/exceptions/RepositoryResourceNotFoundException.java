/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.user.management.exceptions;

/**
 *
 * @author ssbangal
 */
public class RepositoryResourceNotFoundException extends RepositoryException {

    public RepositoryResourceNotFoundException() {
        super();
    }

    public RepositoryResourceNotFoundException(String message) {
        super(message);
    }

    public RepositoryResourceNotFoundException(Throwable cause) {
        super(cause);
    }

    public RepositoryResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
