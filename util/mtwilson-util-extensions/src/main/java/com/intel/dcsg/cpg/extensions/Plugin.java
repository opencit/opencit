/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.extensions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to mark any class as a Plugin that should be registered by the container
 * as an alternative for any interface it implements.
 * 
 * Tentative: optional attributes are id, version, and author.
 * Id should be a UUID in dash format (36 characters)
 * Version should be either major, major.minor, or major.minor.patch with
 * an optional -qualifier appended such as "-SNAPSHOT" or "-RC1" etc.
 * Author should be the individual or organization name responsible for
 * the plugin. The problem with making them optional is that it would require
 * default values in the annotation definition and there aren't any 
 * reasonable defaults other than empty string.
 * 
 * Tentative: The container might instead get equivalent attributes from the jar file
 * in which the plugin was found. This means plugins 
 * should not be aggregated in the same jar unless they have the same overall
 * identifier, version, and author information.
 * 
 * This Plugin annotation should NOT be altered to add a title or description
 * of the plugin - that information is localizable and must be 
 * maintained in an external file that can be translated.
 * 
 * @author jbuhacoff
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Plugin {
    /*
    String id();    
    String version();
    String author();
    */
}
