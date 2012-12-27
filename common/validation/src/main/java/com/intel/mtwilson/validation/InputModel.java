/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.validation;

/**
 * This is a convenience base class for input validators - objects that
 * accept a String, and convert it to an Object if it's a
 * valid representation. Examples of String input are form fields and database
 * fields. The created Object is not necessarily a Model.
 * 
 * Extend InputModel instead of ObjectModel when
 * you need a re-usable input validator to create Model or non-Model instances.
 * InputModel instances are intended to be re-used to parse additional
 * input and create corresponding Object instances (that do not necessarily
 * implement Model), 
 * whereas ObjectModel instances represent a single input.
 * 
 * For example in order to create a re-usable Integer-within-range validator
 * you could create a subclass of InputModel<Integer> and implement the range
 * conditions. Callers can then use isValid() to learn about the input and
 * value() to obtain the validated Integer representation. Integer itself does
 * not have to implement Model. Another example is to set restrictions on String
 * input, such as minimum or maximum length, allowed characters, etc. If the
 * restrictions are part of the application's object model they should probably
 * be implemented as a subclass of ObjectModel, but if they are intended to 
 * prevent cross-site scripting, SQL injection, etc. they are essentially 
 * perimeter checks and once validated the String object is fine to use in
 * the application - that's a good candidate for extending InputModel<String>
 * to validate the input.
 * 
 * You can construct an InputModel with a given value, or you can use the
 * empty constructor and provide the value later.  Validation does not happen
 * until you call either isValid() or value().  Validation only happens once
 * per input - repeated calls to isValid() or value() will NOT re-validate
 * if the input has not changed.
 * 
 * You can call setInput(String) to set or change the input. This will
 * cause it to be validated the next time you call isValid() or value().
 * 
 * Because you can reset the input, you can construct just one instance of
 * this class and re-use it to validate all input of the same type. Valid
 * inputs always instantiate new model objects so the return value will 
 * only be the same object if you call value() multiple times without changing
 * the input.
 * 
 * @since 1.1
 * @author jbuhacoff
 */
public abstract class InputModel<T> extends ObjectModel {
    private String input;
    private T value;
    
    /**
     * If you use this constructor you must later call setInput(text) to
     * provide input to validate.
     */
    public InputModel() {
        
    }
    
    /**
     * If you use this constructor you can later call isValid() or value() to
     * validate the input and obtain results.
     * @param text to validate and convert into the model
     */
    public InputModel(String text) {
        setInput(text);
    }
    
    /**
     * Call this method to set or change the input that should be validated.
     * @param text 
     */
    public final void setInput(String text) {
        input = text;
    }
    
    /**
     * Validates the input only if it has not already been validated since
     * it was set or changed.
     */
    @Override
    protected void validate() {
        value = convert(input);
    }
    
    /**
     * Subclasses must implement the convert method in order to validate
     * and convert the input from String into the desired data model.
     * Call fault() as many times as necessary to log input errors.
     * @param input
     * @return a valid input object or null
     */
    abstract protected T convert(String input); // { return null; }
    

    /**
     * Validates the input if it has not already been validated. 
     * @return the validated model or null if it is invalid
     */
    public final T value() { 
        if( isValid() ) { return value; }
        return null; 
    }
    
    /**
     * This method returns the original input String, or null if the input
     * was null.
     * @return the original input String
     */
    public final String input() {
        return input;
    }

}
