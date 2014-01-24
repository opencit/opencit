/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.client;

/**
 *
 * @author jbuhacoff
 */
public interface Command {
    void execute(String[] args) throws Exception;
}
