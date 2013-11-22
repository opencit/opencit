/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jmod;

/**
 * XXX Tentative : considering removing the ModuleEvent interface and posting Objects directly, using
 * an expression language like XPath to evaluate their properties for filtering.
 * @author jbuhacoff
 */
public interface ModuleEvent {
    String getName();
}
