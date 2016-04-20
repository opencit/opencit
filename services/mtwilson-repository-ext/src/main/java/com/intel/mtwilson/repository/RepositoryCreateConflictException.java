/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.repository;

/**
 *
 * @author ssbangal
 */
public class RepositoryCreateConflictException extends RepositoryCreateException {
    private Locator locator;
    
    public RepositoryCreateConflictException() {
        super();
    }

    public RepositoryCreateConflictException(String message) {
        super(message);
    }

    public RepositoryCreateConflictException(Throwable cause) {
        super(cause);
    }

    public RepositoryCreateConflictException(Throwable cause, Locator locator) {
        super(cause);
        this.locator = locator;
    }
    
    public RepositoryCreateConflictException(Locator locator) {
        super();
        this.locator = locator;
    }
    
    public RepositoryCreateConflictException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public Locator getLocator() {
        return super.getLocator();
    }

}
