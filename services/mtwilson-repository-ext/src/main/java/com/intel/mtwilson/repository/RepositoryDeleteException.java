/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.repository;

/**
 *
 * @author ssbangal
 */
public class RepositoryDeleteException extends RepositoryException {
    private Locator locator;

    public RepositoryDeleteException() {
        super();
    }

    public RepositoryDeleteException(String message) {
        super(message);
    }

    public RepositoryDeleteException(Throwable cause) {
        super(cause);
    }

    public RepositoryDeleteException(Throwable cause, Locator locator) {
        super(cause);
        this.locator = locator;
    }
    
    public RepositoryDeleteException(String message, Throwable cause) {
        super(message, cause);
    }

    public Locator getLocator() {
        return locator;
    }
   
}
