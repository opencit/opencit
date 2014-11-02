/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package test.model.impl;

import com.intel.dcsg.cpg.validation.ObjectModel;
import static org.junit.Assert.*;
import org.junit.Test;
/**
 *
 * @author jbuhacoff
 */
public class HashCodeTest {
    
    public static class ModelA extends ObjectModel {
        private String text;
        public ModelA(String arg) { text = arg; }
        @Override
        protected void validate() { }
        @Override
        public String toString() { return text; }
    }
    public static class ModelB extends ObjectModel {
        private String text;
        public ModelB(String arg) { text = arg; }
        @Override
        protected void validate() { }
        @Override
        public String toString() { return text; }
    }
    public static class ModelC extends ObjectModel {
        private Integer number;
        public ModelC(Integer arg) { number = arg; }
        @Override
        protected void validate() { }
        @Override
        public String toString() { return number.toString(); }
    }
    
    @Test
    public void testModelAHashCode() {
        ModelA a1 = new ModelA("localhost");
        ModelA a2 = new ModelA("localhost");
        System.out.println(String.format("a1: %s  a2: %s", a1.hashCode(), a2.hashCode()));
        assertEquals(a1.hashCode(), a2.hashCode());
        assertTrue(a1.equals(a2));
        assertTrue(a2.equals(a1));
    }
    
    /**
     * ModelA and ModelB both have a single non-transient String field,
     * and share the same hashCode() implementation in their superclass ObjectModel 
     * that uses reflection to generate the hash code via Apache Commons Lang3
     * HashCodeBuilder, without specifying their own random odd numbers for the
     * calculation. So the question is: will the hash codes come out the same??
     * Answer: they did come out the same until I added the getClass().hashCode() into
     * the hashCode() value, then it they came out different.
     */
    @Test
    public void testModelABHashCodeSameValues() {
        ModelA a = new ModelA("localhost"); // it won't validate but the private String input variable will still be set
        ModelB b = new ModelB("localhost"); // it won't validate but the private String input variable will still be set
        System.out.println(String.format("a: %s  b: %s", a.hashCode(), b.hashCode()));
        assertTrue(a.hashCode() != b.hashCode()); // if we didn't include getClass().hashCode() in the calculation the hash code would have been exactly the same... because they both have a single private String variable with the same value
        assertFalse(a.equals(b)); // warning about comparing not-same types ignored because that's exactly what we are testing here; and it works, equals() fails because they are not the same class
        assertFalse(b.equals(a)); // warning about comparing not-same types ignored because that's exactly what we are testing here; and it works, equals() fails because they are not the same class
    }
    @Test
    public void testModelABHashCodeDifferentValues() {
        ModelA a = new ModelA("localhost1"); // it won't validate but the private String input variable will still be set
        ModelB b = new ModelB("localhost2"); // it won't validate but the private String input variable will still be set
        System.out.println(String.format("a: %s  b: %s", a.hashCode(), b.hashCode()));
        assertTrue(a.hashCode() != b.hashCode()); // ack, the hash code is exactly the same... because they both have a single private String variable with the same value
        assertFalse(a.equals(b)); // warning about comparing not-same types ignored because that's exactly what we are testing here; and it works, equals() fails because they are not the same class
        assertFalse(b.equals(a)); // warning about comparing not-same types ignored because that's exactly what we are testing here; and it works, equals() fails because they are not the same class
    }

    @Test
    public void testTwoObjectModelsHashCode() {
        ModelA a = new ModelA("localhost"); // it won't validate but the private String input variable will still be set
        ModelC c = new ModelC(5); // it won't validate but the private String input variable will still be set
        System.out.println(String.format("a: %s  c: %s", a.hashCode(), c.hashCode()));
        assertTrue(a.hashCode() != c.hashCode()); // ack, the hash code is exactly the same.
        assertFalse(a.equals(c)); // warning about comparing not-same types ignored because that's exactly what we are testing here; and it works, equals() fails because they are not the same class
        assertFalse(c.equals(a)); // warning about comparing not-same types ignored because that's exactly what we are testing here; and it works, equals() fails because they are not the same class
    }

}
