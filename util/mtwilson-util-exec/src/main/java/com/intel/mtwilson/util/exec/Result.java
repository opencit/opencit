/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.exec;

import org.apache.commons.exec.ExecuteException;

/**
 *
 * @author jbuhacoff
 */
public class Result {
    private int exitCode;
    private String stdout;
    private String stderr;

    public Result(int exitCode, String stdout, String stderr) {
        this.exitCode = exitCode;
        this.stdout = stdout;
        this.stderr = stderr;
    }
    
    public int getExitCode() {
        return exitCode;
    }

    public String getStderr() {
        return stderr;
    }

    public String getStdout() {
        return stdout;
    }
    
    
}
