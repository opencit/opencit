/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.repository;

/**
 *
 * @author ssbangal
 */
public class RepositoryNotSupportedException extends RepositoryException {

    public RepositoryNotSupportedException() {
        super();
    }

    public RepositoryNotSupportedException(String message) {
        super(message);
    }

    public RepositoryNotSupportedException(Throwable cause) {
        super(cause);
    }

    public RepositoryNotSupportedException(String message, Throwable cause) {
        super(message, cause);
    }

}
