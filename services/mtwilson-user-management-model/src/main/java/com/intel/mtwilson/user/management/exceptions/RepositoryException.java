/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.user.management.exceptions;

/**
 *
 * @author ssbangal
 */
public class RepositoryException extends RuntimeException {

    public RepositoryException() {
        super();
    }

    public RepositoryException(String message) {
        super(message);
    }

    public RepositoryException(Throwable cause) {
        super(cause);
    }

    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }

    
}
