/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.extensions.factorystyle;

/**
 * This simple example is analogous to the host agents used in Mt Wilson - 
 * each plugin may support one or more host types, but because the plugins
 * are not known in advance they have to be queries for which host types 
 * they support; if they support the given type they will return a host agent,
 * otherwise they will return null.
 * 
 * @author jbuhacoff
 */
public interface TelephoneFactory {
    Telephone create();
}
