/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.launcher.ext.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Auto-complete on Netbeans won't list this class, probably because 
 * there's a class java.lang.Shutdown even though that one is package-private.
 * So you have to "import com.intel.mtwilson.launcher.ext.Shutdown;" 
 * without the auto-complete.
 * @author jbuhacoff
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Shutdown {
    
}
