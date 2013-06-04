package com.intel.dcsg.cpg.console;

/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */


//import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class ColorTest {
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    /**
     * Colors work in netbean's output window but other things like underline, blinking, and clearing the line don't work there.
     * To test those features you need to create a stand-alone program and run it from a linux terminal.  These features
     * don't work in a Windows command line either.
     */
    @Test
    public void testColor() {
        Term term = new Term();
        term.printlnError("hello world error");
        term.printlnWarning("hello world warning");
        term.printlnSuccess("hello world success");
        System.out.println(term.underline()+"test underline"+term.reset());
        System.out.println(term.blue()+"test blue"+term.reset());
        System.out.println(term.green()+term.dim()+"test dim green"+term.reset());
        System.out.println("foo"+term.clearLine()+"bar");
    }
}
