/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.launcher.ws.ext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Classes annotated with @V1 and @Path("path") can be accessed at a URL 
 * like /v1/{path} and indicates they are part of the Mt Wilson 1.x APIs
 *
 * @author jbuhacoff
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface V1 {
    
}
