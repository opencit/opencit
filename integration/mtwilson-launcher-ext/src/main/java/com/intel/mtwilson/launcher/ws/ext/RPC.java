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
 * Classes annotated with @RPC("name") can be accessed via a URL
 * like /v2/rpc/{name} and indicates they follow the Mt Wilson 2.x RPC
 * conventions
 * 
 * @author jbuhacoff
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RPC {
    /**
     * The name of the remote procedure call
     * @return 
     */
    String value();
}
