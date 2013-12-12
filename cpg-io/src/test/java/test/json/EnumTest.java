/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.json;

import static org.junit.Assert.*;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class EnumTest {
    
    
    public static enum Name {
        A, B, c, d;
        
        // cannot name this "valueOf" because enum already has it and it's not possible to extend enum class
        public static Name valueOfIgnoreCase(String text) {
            for(Name name : Name.values()) {
                if( name.name().equalsIgnoreCase(text)) { return name; }
            }
            throw new IllegalArgumentException("No enum constant matches text");
        }
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testValueOfNameMustMatch() {
        Name name = Name.valueOf("a"); // no enum constant test.json.EnumTest.Name.a
        assertNotNull(name);
        assertEquals(Name.A, name);
    }

    @Test
    public void testValueOfNameCaseInsensitive() {
        Name nameA = Name.valueOfIgnoreCase("a");
        assertNotNull(nameA);
        assertEquals(Name.A, nameA);
        Name namec = Name.valueOfIgnoreCase("c");
        assertNotNull(namec);
        assertEquals(Name.c, namec);
    }

    @Test
    public void testEnumToString() {
        assertEquals("A", Name.A.toString());
        assertEquals("A", Name.A.name());
        assertEquals("c", Name.c.toString());
        assertEquals("c", Name.c.name());
    }
}
