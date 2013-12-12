/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.validation;

import java.util.List;

/**
 *
 * The Model interface is a simple abstraction that can be used by input
 * validators and object-oriented models that need detailed or comprehensive
 * error checking. In contrast to an exception-based style, where the first input
 * validation failure generates an exception that stops the validation 
 * processing, implementations of Model try to validate as much as possible and log the problems
 * as "faults" which can then be reported via the getFaults() method.
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
