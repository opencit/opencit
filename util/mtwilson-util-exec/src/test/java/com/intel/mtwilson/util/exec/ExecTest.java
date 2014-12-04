/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.exec;

import java.io.IOException;
import org.apache.commons.exec.ExecuteException;
import org.junit.Test;
import static org.junit.Assert.*;
import com.intel.mtwilson.util.filesystem.Platform;
import java.util.concurrent.ExecutionException;
/**
 *
 * @author jbuhacoff
 */
public class ExecTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExecTest.class);

    @Test
    public void testWindows() throws ExecuteException, IOException {
       if( Platform.isWindows() ) {
           Result result = ExecUtil.execute("cmd.exe", "/C", "dir");
           log.debug("exit code {}", result.getExitCode());
           log.debug("stdout: {}", result.getStdout());
           log.debug("stderr: {}", result.getStderr());
           assertEquals(0, result.getExitCode());
       } 
    }


    @Test
    public void testWindowsQuietFailure() throws ExecuteException, IOException {
       if( Platform.isWindows() ) {
           Result result = ExecUtil.executeQuietly("cmd.exe", "/C", "dir", "___ this path does not exist ___");
           log.debug("exit code {}", result.getExitCode());
           log.debug("stdout: {}", result.getStdout());
           log.debug("stderr: {}", result.getStderr());
           assertEquals(1, result.getExitCode());
       } 
    }

    @Test(expected=ExecuteException.class)
    public void testWindowsFailure() throws ExecuteException, IOException {
       if( Platform.isWindows() ) {
           Result result = ExecUtil.execute("cmd.exe", "/C", "dir", "___ this path does not exist ___");
           log.debug("exit code {}", result.getExitCode());
           log.debug("stdout: {}", result.getStdout());
           log.debug("stderr: {}", result.getStderr());
       } 
    }
    
}
