/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
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
