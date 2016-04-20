/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.logging;

import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class LogExceptionTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LogExceptionTest.class);

    private void run1() throws Exception {
        throw new Exception("error in run");
    }

    private void run2() throws Exception {
        throw new Exception();
    }
    
    /**
     * Prints a message with no stack trace.  
2013-12-03 08:00:51,902 ERROR [main] t.l.LogExceptionTest [LogExceptionTest.java:30] testLogPlusMessage1: error in run
2013-12-03 08:00:51,927 ERROR [main] t.l.LogExceptionTest [LogExceptionTest.java:36] testLogPlusMessage2: null
     * 
     */
    @Test
    public void testLogPlusMessage() {
        try {
            run1();
        }
        catch(Exception e) {
            log.error("testLogPlusMessage1: "+e.getMessage());
        }
        try {
            run2();
        }
        catch(Exception e) {
            log.error("testLogPlusMessage2: "+e.getMessage());
        }
    }

    /**
     * Same output as testLogPlusMessage, but avoids the string concatentation
     * when the log level is suppressed, and also easier for future customization
     * of the log handler because it knows which part is the info from the
     * log line and which part is the varaible.
2013-12-03 08:59:11,230 ERROR [main] t.l.LogExceptionTest [LogExceptionTest.java:55] testLogCommaMessage1: error in run
2013-12-03 08:59:11,236 ERROR [main] t.l.LogExceptionTest [LogExceptionTest.java:61] testLogCommaMessage2: null
     */
    @Test
    public void testLogCommaMessage() {
        try {
            run1();
        }
        catch(Exception e) {
            log.error("testLogCommaMessage1: {}",e.getMessage());
        }
        try {
            run2();
        }
        catch(Exception e) {
            log.error("testLogCommaMessage2: {}",e.getMessage());
        }
    }
    
    /**
2013-12-03 08:01:28,146 ERROR [main] t.l.LogExceptionTest [LogExceptionTest.java:51] testLogPlusMessageAndStackTrace1: error in run
java.lang.Exception: error in run
	at test.logging.LogExceptionTest.run1(LogExceptionTest.java:17)
	at test.logging.LogExceptionTest.testLogPlusMessageAndStackTrace(LogExceptionTest.java:48)
    ...
2013-12-03 08:01:28,159 ERROR [main] t.l.LogExceptionTest [LogExceptionTest.java:58] testLogPlusMessageAndStackTrace2: null
java.lang.Exception
	at test.logging.LogExceptionTest.run2(LogExceptionTest.java:21)
	at test.logging.LogExceptionTest.testLogPlusMessageAndStackTrace(LogExceptionTest.java:55)
    ...
     * 
     */
    @Test
    public void testLogPlusMessageAndStackTrace() {
        try {
            run1();
        }
        catch(Exception e) {
            log.error("testLogPlusMessageAndStackTrace1: "+e.getMessage());
            e.printStackTrace(System.err);
        }
        try {
            run2();
        }
        catch(Exception e) {
            log.error("testLogPlusMessageAndStackTrace2: "+e.getMessage());
            e.printStackTrace(System.err);
        }
    }

    /**
     * Looks a lot like testLogPlusMessageAndStackTrace except that the exception's
     * message is printed next to the exception class name on the second line, 
     * and also that the stack trace always ends up in the log if its log level is
     * enabled, whereas when the exception is printed to System.err it's the customer
     * cannot suppress it with the logging configuration and also it may not end up
     * in the right log file at all because System.err may be redirected by the container
     * to a different log than the application log, and then it's more work to correlate
     * them.
     * 
2013-12-03 08:02:46,844 ERROR [main] t.l.LogExceptionTest [LogExceptionTest.java:82] testLogSlf4jStyle1: 
java.lang.Exception: error in run
	at test.logging.LogExceptionTest.run1(LogExceptionTest.java:17) ~[test-classes/:na]
	at test.logging.LogExceptionTest.testLogSlf4jStyle(LogExceptionTest.java:79) ~[test-classes/:na]
    ...
2013-12-03 08:02:46,858 ERROR [main] t.l.LogExceptionTest [LogExceptionTest.java:88] testLogSlf4jStyle2: 
java.lang.Exception: null
	at test.logging.LogExceptionTest.run2(LogExceptionTest.java:21) ~[test-classes/:na]
	at test.logging.LogExceptionTest.testLogSlf4jStyle(LogExceptionTest.java:85) ~[test-classes/:na]
    ...
     * 
     */
    @Test
    public void testLogSlf4jStyle() {
        try {
            run1();
        }
        catch(Exception e) {
            log.error("testLogSlf4jStyle1: ", e);
        }
        try {
            run2();
        }
        catch(Exception e) {
            log.error("testLogSlf4jStyle2: ", e);
        }
    }
    
}
