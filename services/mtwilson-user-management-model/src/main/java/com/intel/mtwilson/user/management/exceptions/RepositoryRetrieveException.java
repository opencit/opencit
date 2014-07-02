/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.user.management.exceptions;

/**
 *
 * @author ssbangal
 */
public class RepositoryRetrieveException extends RepositoryException {

    public RepositoryRetrieveException() {
        super();
    }

    public RepositoryRetrieveException(String message) {
        super(message);
    }

    public RepositoryRetrieveException(Throwable cause) {
        super(cause);
    }

    public RepositoryRetrieveException(String message, Throwable cause) {
        super(message, cause);
    }

}
