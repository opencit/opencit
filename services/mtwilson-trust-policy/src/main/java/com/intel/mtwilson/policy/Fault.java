/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

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
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, property="fault_name")
public class Fault {
    private final String description;
    private final Throwable cause;
    
    // for desearializing jackson
    public Fault() {
        this.cause = null;
        this.description = null;
    }
    
    @JsonCreator
    public Fault(@JsonProperty("description") String description) {
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
    
    public String getDescription() {
        return description;
    }
    
    @Override
    public String toString() {
        return description;
    }
    
    /**
     * This method is present for the purpose of serializing the faults in a report.
     * Normally the object mapper may not include class names so this method ensures
     * the fault's class name is serialized. For subclasses this returns the name of
     * the subclass.
     * @return the class name of the fault
     */
    public String getFaultName() { return getClass().getName(); }
    
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
