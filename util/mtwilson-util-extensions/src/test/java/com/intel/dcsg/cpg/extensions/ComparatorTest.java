/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.extensions;

import com.intel.dcsg.cpg.extensions.PreferenceComparator;
import com.intel.dcsg.cpg.extensions.pojostyle.Apple;
import com.intel.dcsg.cpg.extensions.pojostyle.Banana;
import com.intel.dcsg.cpg.extensions.pojostyle.Carrot;
import java.util.ArrayList;
import java.util.Collections;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class ComparatorTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ComparatorTest.class);

    @Test
    public void testCompareClasses1() {
        ArrayList<String> preferenceOrder = new ArrayList<String>(); // highest to lowest
        preferenceOrder.add("com.intel.dcsg.cpg.extensions.pojostyle.Apple");
        preferenceOrder.add("com.intel.dcsg.cpg.extensions.pojostyle.Banana");
        ArrayList<Class<?>> implementations = new ArrayList<Class<?>>();
        implementations.add(Apple.class);
        implementations.add(Banana.class);
        implementations.add(Carrot.class);
        PreferenceComparator c = new PreferenceComparator(preferenceOrder);
        Collections.sort(implementations, c);
        assertEquals(0, implementations.indexOf(Apple.class));
        assertEquals(1, implementations.indexOf(Banana.class));
        assertEquals(2, implementations.indexOf(Carrot.class));
    }

    @Test
    public void testCompareClasses2() {
        ArrayList<String> preferenceOrder = new ArrayList<String>(); // highest to lowest
        preferenceOrder.add("com.intel.dcsg.cpg.extensions.pojostyle.Banana");
        ArrayList<Class<?>> implementations = new ArrayList<Class<?>>();
        implementations.add(Apple.class);
        implementations.add(Banana.class);
        implementations.add(Carrot.class);
        PreferenceComparator c = new PreferenceComparator(preferenceOrder);
        Collections.sort(implementations, c);
        for(Class<?> clazz : implementations) { log.debug("Implementation: {}", clazz.getName()); }
        // only banana is guaranteed to be first, the other two can be in any order
        assertEquals(0, implementations.indexOf(Banana.class));
        assertTrue(implementations.indexOf(Apple.class) > 0);
        assertTrue(implementations.indexOf(Carrot.class) > 0);
    }

    @Test
    public void testCompareClasses3() {
        ArrayList<String> preferenceOrder = new ArrayList<String>(); // highest to lowest
        preferenceOrder.add("com.intel.dcsg.cpg.extensions.pojostyle.Apple");
        preferenceOrder.add("com.intel.dcsg.cpg.extensions.pojostyle.Banana");
        preferenceOrder.add("com.intel.dcsg.cpg.extensions.pojostyle.Carrot");
        ArrayList<Class<?>> implementations = new ArrayList<Class<?>>();
        implementations.add(Apple.class);
        implementations.add(Banana.class);
        implementations.add(Carrot.class);
        PreferenceComparator c = new PreferenceComparator(preferenceOrder);
        Collections.sort(implementations, c);
        assertEquals(0, implementations.indexOf(Apple.class));
        assertEquals(1, implementations.indexOf(Banana.class));
        assertEquals(2, implementations.indexOf(Carrot.class));
    }

    @Test
    public void testCompareClasses4() {
        ArrayList<String> preferenceOrder = new ArrayList<String>(); // no preference
        ArrayList<Class<?>> implementations = new ArrayList<Class<?>>();
        implementations.add(Apple.class);
        implementations.add(Banana.class);
        implementations.add(Carrot.class);
        PreferenceComparator c = new PreferenceComparator(preferenceOrder);
        Collections.sort(implementations, c);
        // order not guaranteed at all because no preferences:
        assertTrue(implementations.indexOf(Apple.class) > -1);
        assertTrue(implementations.indexOf(Banana.class) > -1);
        assertTrue(implementations.indexOf(Carrot.class) > -1);
    }
    
}
