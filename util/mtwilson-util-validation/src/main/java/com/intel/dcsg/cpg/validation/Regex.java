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
 * When a String member variable is annotated with Regex, its
 * value is checked against the regex specified with this annotation.
 * When a method is annotated with Regex, its String return value is checked
 * against the regex specified with this annotation.
 * It is an error to annotate a non-String variable or return value from
 * a method.
 *
 * @author jbuhacoff
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Regex {
    String value();
}
