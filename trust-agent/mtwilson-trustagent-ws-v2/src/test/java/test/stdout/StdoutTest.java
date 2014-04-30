/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package test.stdout;

import com.intel.mountwilson.common.InputReader;
import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class StdoutTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StdoutTest.class);

    /**
     * Example output:
2014-04-29 12:27:12,448 DEBUG [main] t.s.StdoutTest [StdoutTest.java:26] stdout:
OpenSSL 1.0.1g 7 Apr 2014

2014-04-29 12:27:12,457 DEBUG [main] t.s.StdoutTest [StdoutTest.java:27] exitcode: 0
     * 
     * 
     * @throws IOException
     * @throws InterruptedException 
     */
    @Test
    public void testStdout() throws IOException, InterruptedException {
        Process p = Runtime.getRuntime().exec("openssl version"); // throws IOException
        InputReader stdout = new InputReader(p.getInputStream());
        Thread tStdout = new Thread(stdout);
        tStdout.start();
        int exitcode = p.waitFor();
        tStdout.join(); // throws InterruptedException
        log.debug("stdout:\n{}", stdout.getResult());
        log.debug("exitcode: {}", exitcode);
    }

    @Test(expected=IOException.class)
    public void testUnknownCommand() throws IOException, InterruptedException {
        Process p = Runtime.getRuntime().exec("unknowncommand"); // throws IOException
        fail();
    }
    
    @Test
    public void testStderr() throws IOException, InterruptedException {
        Process p = Runtime.getRuntime().exec("openssl --help"); // throws IOException
        InputReader stdout = new InputReader(p.getErrorStream());
        Thread tStdout = new Thread(stdout);
        tStdout.start();
        int exitcode = p.waitFor();
        tStdout.join(); // throws InterruptedException
        log.debug("stderr:\n{}", stdout.getResult());
        log.debug("exitcode: {}", exitcode);
    }
    

}
