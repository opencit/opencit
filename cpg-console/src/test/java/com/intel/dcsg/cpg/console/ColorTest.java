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
    
    @Test
    public void testColor() {
        Color color = new Color();
        color.error("hello world error");
        color.warning("hello world warning");
        color.success("hello world success");
    }
}
