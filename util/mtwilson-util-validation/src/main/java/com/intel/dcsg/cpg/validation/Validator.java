/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * When a member variable is annotated with Validator, its
 * value is checked using an instance of the class specified with this annotation.
 * When a method is annotated with Validator, its return value is checked
 * against using an instance of the class specified with this annotation.
 * 
 * Any type can be annotated with Validator. The corresponding validation class
 * specified when using the annotation must implement Model and must have
 * a setter method "setInput" with a single argument matching the type of the
 * field or return value of the method that was annotated. 
 * 
 * A new instance of the validation class
 * is created for each validation.
 *
 * @author jbuhacoff
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Validator {
    Class value();
}
