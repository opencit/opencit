/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.validation;

/**
 *
 * Individual Model implementations should not throw exceptions, but 
 * frameworks or applications that use Model implementations may throw this 
 * exception when they expect a valid Model as input and receive an instance 
 * for which isValid() returns false.
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
