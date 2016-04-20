/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.repository;

/**
 *
 * @author ssbangal
 */
public class RepositoryInvalidInputException extends RepositoryException {
    private Locator locator;
    
    public RepositoryInvalidInputException() {
        super();
    }

    public RepositoryInvalidInputException(String message) {
        super(message);
    }

    public RepositoryInvalidInputException(Throwable cause) {
        super(cause);
    }

    public RepositoryInvalidInputException(Throwable cause, Locator locator) {
        super(cause);
        this.locator = locator;
    }
    
    public RepositoryInvalidInputException(Locator locator) {
        super();
        this.locator = locator;
    }
    
    public RepositoryInvalidInputException(String message, Throwable cause) {
        super(message, cause);
    }

    public Locator getLocator() {
        return locator;
    }

}
