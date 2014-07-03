/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.repository;

/**
 *
 * @author ssbangal
 */
public class RepositoryCreateException extends RepositoryException {

    public RepositoryCreateException() {
        super();
    }

    public RepositoryCreateException(String message) {
        super(message);
    }

    public RepositoryCreateException(Throwable cause) {
        super(cause);
    }

    public RepositoryCreateException(String message, Throwable cause) {
        super(message, cause);
    }

}
