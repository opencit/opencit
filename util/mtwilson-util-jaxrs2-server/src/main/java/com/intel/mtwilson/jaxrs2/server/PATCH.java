/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jaxrs2.server;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.ws.rs.HttpMethod;

/**
 *
 * @author jbuhacoff
 */ 
@Target({ElementType.METHOD}) 
@Retention(RetentionPolicy.RUNTIME) 
@HttpMethod("PATCH") 
public @interface PATCH { 
}
