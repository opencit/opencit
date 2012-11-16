/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.validation;

/**
 *
 * @author jbuhacoff
 */
public class InvalidModelException extends RuntimeException {
    private Model model;
    
    public InvalidModelException(@Unchecked Model model) {
        super();
        this.model = model;
    }
    public InvalidModelException(@Unchecked Model model, Throwable cause) {
        super(cause);
        this.model = model;
    }
    public InvalidModelException(String message, @Unchecked Model model) {
        super(message);
        this.model = model;
    }
    public InvalidModelException(String message, @Unchecked Model model, Throwable cause) {
        super(message, cause);
        this.model = model;
    }
    
    public Model getModel() { return model; }
}
