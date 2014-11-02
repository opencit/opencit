/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.validation;

/**
 * This is a convenience base class for input validators - objects that
 * accept a specific type and report whether it is valid. 
 *
 * Extend InputValidator when you need to validate input that is either not
 * a String or is a String but has validation rules that cannot be easily
 * expressed in a Regex. 
 * 
 * When subclassing this validator, override validate() and use getInput() to
 * obtain the input to validate; use any of the fault() methods to register
 * problems with the input. The input will be considered valid if no faults
 * are registered. If null is a valid input, validate() should simply return
 * if input is null. If null is not a valid input, validate() should register
 * a fault indicating null input is not valid.
 * 
 * @since 0.2
 * @author jbuhacoff
 */
public abstract class InputValidator<T> extends ObjectModel {
    private T input;
    
    /**
     * If you use this constructor you must later call setInput(input) to
     * provide input to validate.
     */
    public InputValidator() {
        
    }
    
    /**
     * If you use this constructor you can later call isValid() or value() to
     * validate the input and obtain results.
     * @param input to validate
     */
    public InputValidator(T input) {
        setInput(input);
    }
    
    /**
     * Call this method to set or change the input that should be validated.
     * @param input 
     */
    public final void setInput(T input) {
        this.input = input;
    }
    
    /**
     * This method returns the original input, or null if the input
     * was null.
     * @return the original input
     */
    public final T getInput() {
        return input;
    }

}
