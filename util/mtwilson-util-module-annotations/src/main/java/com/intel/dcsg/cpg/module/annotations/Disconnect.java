/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.module.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to mark a method that is an "unsetter" which the container can call to 
 * notify this component that another service or component is about to be deactivated.
 * If a component implements Connect but not Disconnect, then the component itself will
 * be deactivated and then reactivated (this time without a Connect call for the other
 * component that got deactivatd)
 * @author jbuhacoff
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Disconnect {
    
}
