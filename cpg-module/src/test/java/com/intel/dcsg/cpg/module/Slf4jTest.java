/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.module;

import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class Slf4jTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Slf4jTest.class);
    
    @Test
    public void testLogOneArg() {
        log.debug("One arg: {}", "foo");
    }

    @Test
    public void testLogTwoArg() {
        log.debug("Two args: {}, {}", "foo", "bar");
    }

    @Test
    public void testLogThreeArg() {
        log.debug("Three args: {}, {}, {}", "foo", "bar", "quz"); // not available in slf4j 1.6.4 !!! so use latest like 1.7.5
    }

    @Test
    public void testLogOneArgWithException() {
        try {
            throw new RuntimeException("ack");
        }
        catch(Exception e) {
        log.debug("One arg with exception: {}", "foo", e);            
        }
    }

    @Test
    public void testLogTwoArgWithException() {
        try {
            throw new RuntimeException("ack");
        }
        catch(Exception e) {
        log.debug("Two args with exception: {}, {}", "foo", "bar", e);
        }
    }

    @Test
    public void testLogThreeArgWithException() {
        try {
            throw new RuntimeException("ack");
        }
        catch(Exception e) {
        log.debug("Three args with exception: {}, {}, {}", "foo", "bar", "quz", e);// not available in slf4j 1.6.4 !!! so use latest like 1.7.5
        }
    }

}
