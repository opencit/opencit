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
    private Locator locator;
    
    public RepositoryCreateException() {
        super();
    }

    public RepositoryCreateException(String message) {
        super(message);
    }

    public RepositoryCreateException(Throwable cause) {
        super(cause);
    }

    public RepositoryCreateException(Throwable cause, Locator locator) {
        super(cause);
        this.locator = locator;
    }
    
    public RepositoryCreateException(String message, Throwable cause) {
        super(message, cause);
    }

    public Locator getLocator() {
        return locator;
    }
    
}
