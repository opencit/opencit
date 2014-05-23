
import java.util.HashMap;
import java.util.Properties;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.Test;
import static org.junit.Assert.*;

/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */

/**
 *
 * @author jbuhacoff
 */
public class StringArrayTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StringArrayTest.class);

    @Test
    public void testStringMissing() {
        Properties p = new Properties();
        Configuration c = new MapConfiguration(p);
        String test = c.getString("does.not.exist");
        assertNull(test);
    }
    @Test
    public void testStringArrayMissing() {
        Properties p = new Properties();
        Configuration c = new MapConfiguration(p);
        String[] test = c.getStringArray("does.not.exist");
        assertNotNull(test);
        assertEquals(0, test.length);
    }
    
    @Test
    public void testStringNull() {
        HashMap<String,Object> p = new HashMap<>();
        p.put("null.value", null);
        Configuration c = new MapConfiguration(p);
        String test = c.getString("null.value");
        assertNull(test);
    }
    @Test
    public void testStringArrayNull() {
        HashMap<String,Object> p = new HashMap<>();
        p.put("null.value", null);
        Configuration c = new MapConfiguration(p);
        String[] test = c.getStringArray("null.value");
        assertNotNull(test);
        assertEquals(0, test.length);
    }
    
    @Test
    public void testStringEmpty() {
        Properties p = new Properties();
        p.setProperty("empty.value", "");
        Configuration c = new MapConfiguration(p);
        String test = c.getString("empty.value");
        assertEquals("", test);
    }
    @Test
    public void testStringArrayEmpty() {
        Properties p = new Properties();
        p.setProperty("empty.value", "");
        Configuration c = new MapConfiguration(p);
        String[] test = c.getStringArray("empty.value");
        assertNotNull(test);
        assertEquals(1, test.length);
        assertEquals("", test[0]);
    }

    @Test
    public void testStringOneValue() {
        Properties p = new Properties();
        p.setProperty("one.value", "x");
        Configuration c = new MapConfiguration(p);
        String test = c.getString("one.value");
        assertEquals("x", test);
    }
    @Test
    public void testStringArrayOneValue() {
        Properties p = new Properties();
        p.setProperty("one.value", "x");
        Configuration c = new MapConfiguration(p);
        String[] test = c.getStringArray("one.value");
        assertNotNull(test);
        assertEquals(1, test.length);
        assertEquals("x", test[0]);
    }

    @Test
    public void testStringOneValueWithSpaces() {
        Properties p = new Properties();
        p.setProperty("one.value", " x ");
        Configuration c = new MapConfiguration(p);
        String test = c.getString("one.value");
        assertEquals("x", test);
    }
    @Test
    public void testStringArrayOneValueWithSpaces() {
        Properties p = new Properties();
        p.setProperty("one.value", " x ");
        Configuration c = new MapConfiguration(p);
        String[] test = c.getStringArray("one.value");
        assertNotNull(test);
        assertEquals(1, test.length);
        assertEquals("x", test[0]);
    }
    
    @Test
    public void testStringTwoValues() {
        Properties p = new Properties();
        p.setProperty("two.values", "x,y");
        Configuration c = new MapConfiguration(p);
        String test = c.getString("two.values");
        assertEquals("x", test);  // NOTE:  this is the commons-configuration behavior even though it is not intuitive - to get first element of something expected to be an array, programmer should use getStringArray then access first element
    }
    
    @Test
    public void testStringArrayTwoValues() {
        Properties p = new Properties();
        p.setProperty("two.values", "x,y");
        Configuration c = new MapConfiguration(p);
        String[] test = c.getStringArray("two.values");
        assertNotNull(test);
        assertEquals(2, test.length);
        assertEquals("x", test[0]);
        assertEquals("y", test[1]);
    }

    @Test
    public void testStringArrayThreeValuesWithSpaces() {
        Properties p = new Properties();
        p.setProperty("three.values", " x , y, z ");
        Configuration c = new MapConfiguration(p);
        String[] test = c.getStringArray("three.values");
        assertNotNull(test);
        assertEquals(3, test.length);
        assertEquals("x", test[0]);
        assertEquals("y", test[1]);
        assertEquals("z", test[2]);
    }
    
}
