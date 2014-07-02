/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.user.management.exceptions;

/**
 *
 * @author ssbangal
 */
public class RepositoryResourceConflictException extends RepositoryException {

    public RepositoryResourceConflictException() {
        super();
    }

    public RepositoryResourceConflictException(String message) {
        super(message);
    }

    public RepositoryResourceConflictException(Throwable cause) {
        super(cause);
    }

    public RepositoryResourceConflictException(String message, Throwable cause) {
        super(message, cause);
    }

}
