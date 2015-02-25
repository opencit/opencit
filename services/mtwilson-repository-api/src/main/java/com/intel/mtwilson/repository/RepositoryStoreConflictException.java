/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.repository;

/**
 *
 * @author ssbangal
 */
public class RepositoryStoreConflictException extends RepositoryStoreException {
    private Locator locator;
    
    public RepositoryStoreConflictException() {
        super();
    }

    public RepositoryStoreConflictException(String message) {
        super(message);
    }

    public RepositoryStoreConflictException(Throwable cause) {
        super(cause);
    }

    public RepositoryStoreConflictException(Throwable cause, Locator locator) {
        super(cause);
        this.locator = locator;
    }

    public RepositoryStoreConflictException(Locator locator) {
        super();
        this.locator = locator;
    }
    
    public RepositoryStoreConflictException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public Locator getLocator() {
        return super.getLocator();
    }

}
