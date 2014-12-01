/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.console;

import org.apache.commons.configuration.Configuration;

/**
 *
 * @author jbuhacoff
 */
public interface Command {
    void setOptions(Configuration options);
    void execute(String[] args) throws Exception;
}
