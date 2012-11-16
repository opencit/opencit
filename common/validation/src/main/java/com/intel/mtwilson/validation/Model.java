/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.validation;

import java.util.List;

/**
 *
 * @author jbuhacoff
 */
public interface Model {
    
    /**
     * Evaluate the model to determine if it is valid. 
     * Valid models should not throw any NullPointerExceptions or 
     * IllegalArgumentExceptions when they are used.
     * @return true if the model is valid
     */
    boolean isValid();
    
    /**
     * If isValid() returns false, then it should generate a list of faults
     * that describe why the model is not valid. A copy of that list can be
     * obtained by calling getFaults(). 
     * This method will never return null. 
     * If isValid() returns true, the list will be empty. 
     * If isValid() returns false, the list must have at least one fault.
     * @return list of faults if isValid()==false, or empty list if isValid()==true
     */
    List<Fault> getFaults();
}
