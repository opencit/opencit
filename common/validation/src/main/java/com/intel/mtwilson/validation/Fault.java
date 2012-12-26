/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.validation;

/**
 * This class strongly resembles an Exception object but it is used differently.
 * 
 * In this validation package, Faults are collected, not thrown. This allows
 * an application to fully validate potentially complex user input, collect
 * all the faults, and then provide
 * complete guidance on any errors that may exist in the input. 
 * 
 * Contrast this to Exceptions, that can only be thrown one at a time and
 * interrupt the flow of execution, forcing the user to correct and resubmit
 * one error at a time until the input is completely validated.
 * 
 * First, 
 * @since 1.1
 * @author jbuhacoff
 */
public class Fault {
    private final String description;
    private final Throwable cause;
    
    public Fault(String description) {
        this.cause = null;
        this.description = description;
    }
    
    public Fault(String format, Object... args) {
        this.cause = null;
        this.description = String.format(format, args);
    }
    
    public Fault(Throwable e, String description) {
        this.cause = e;
        this.description = description;
    }
    
    public Fault(Throwable e, String format, Object... args) {
        this.cause = e;
        this.description = String.format(format, args);
    }
    
    @Override
    public String toString() {
        return description;
    }
    
    /**
     * This method allows the application to get additional information about 
     * the error or exception that caused the fault, if that was the case.
     * 
     * @return the Throwable that is represented by this Fault, or null if there isn't one
     */
    public Throwable getCause() {
        return cause;
    }
}
