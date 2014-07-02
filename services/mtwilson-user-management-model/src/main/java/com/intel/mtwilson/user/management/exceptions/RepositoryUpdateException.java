/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.user.management.exceptions;

/**
 *
 * @author ssbangal
 */
public class RepositoryUpdateException extends RepositoryException {

    public RepositoryUpdateException() {
        super();
    }

    public RepositoryUpdateException(String message) {
        super(message);
    }

    public RepositoryUpdateException(Throwable cause) {
        super(cause);
    }

    public RepositoryUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
