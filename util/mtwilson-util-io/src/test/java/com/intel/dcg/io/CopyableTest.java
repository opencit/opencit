/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcg.io;

import com.intel.dcsg.cpg.io.Copyable;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class CopyableTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CopyableTest.class);

    @Test
    public void testCopy() {
        Fruit a = new Fruit("apple", "red");
        Fruit b = a.copy();
        log.debug("Original fruit type {} color {}", a.getType(), a.getColor());
        assertEquals("apple", a.getType());
        assertEquals("red", a.getColor());
        log.debug("Copied fruit type {} color {}", b.getType(), b.getColor());
        assertEquals("apple", b.getType());
        assertEquals("red", b.getColor());
        b.color = "green";
        assertEquals("apple", b.getType());
        assertEquals("green", b.getColor());
        log.debug("After edit, copied fruit type {} color {}", b.getType(), b.getColor());
    }
    
    public static class Fruit implements Copyable {
        private String type;
        private String color;

        public Fruit(String type, String color) {
            this.type = type;
            this.color = color;
        }

        @Override
        public Fruit copy() {
            return new Fruit(type,color);
        }

        public String getColor() {
            return color;
        }

        public String getType() {
            return type;
        }
        
        
    }
}
