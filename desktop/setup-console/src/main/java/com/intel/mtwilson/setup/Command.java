/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup;

/**
 *
 * @author jbuhacoff
 */
public interface Command {
    void setContext(SetupContext ctx);
    void execute(String[] args) throws Exception;
//    void setConfiguration(Object config); // XXX use commons-configuration? or our own config object with a rich model?
}
