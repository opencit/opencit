/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.common;

/**
 *
 * @author jbuhacoff
 */
public class CommandResult {
    protected String command;
    protected String stdout;
    protected String stderr;
    protected int exitcode;

    public String getCommand() {
        return command;
    }

    public String getStdout() {
        return stdout;
    }

    public String getStderr() {
        return stderr;
    }

    public int getExitcode() {
        return exitcode;
    }
    
    
}
