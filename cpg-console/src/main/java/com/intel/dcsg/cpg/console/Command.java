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
//    void setContext(SetupContext ctx);
    void setOptions(Configuration options);
    void execute(String[] args) throws Exception;
//    void setConfiguration(Object config); // XXX use commons-configuration? or our own config object with a rich model?
}
