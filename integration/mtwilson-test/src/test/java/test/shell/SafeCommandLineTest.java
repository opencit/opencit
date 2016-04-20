/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package test.shell;

import java.util.regex.Pattern;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class SafeCommandLineTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SafeCommandLineTest.class);

    private static final Pattern singleQuoteShellSpecialCharacters = Pattern.compile("[*?#~=%\\[]");

    public static Boolean containsSingleQuoteShellSpecialCharacters(String input) {
        Pattern p = Pattern.compile("(.*?)" + singleQuoteShellSpecialCharacters.pattern() + "(.*?)");
        return input.matches(p.pattern());
    }
    
    public static String escapeShellArgument(String input) {
        return "'" + input.replaceAll(singleQuoteShellSpecialCharacters.pattern(), "\\\\$0") + "'";
    }
    
    public static String escapeShellOption(String input) {
        if (input.contains("=")) {
            String[] option = input.split("=", 2);
            String parameter = option[0];
            String value = option[1];
            return parameter + "='" + value.replaceAll(singleQuoteShellSpecialCharacters.pattern(), "\\\\$0") + "'";
        } else {
            return escapeShellArgument(input);
        }
    }
    
    @Test
    public void testFindSingleQuoteShellSpecialCharacters() {
        assertTrue(containsSingleQuoteShellSpecialCharacters("hello*world"));
        assertTrue(containsSingleQuoteShellSpecialCharacters("hello?world"));
        assertTrue(containsSingleQuoteShellSpecialCharacters("hello#world"));
        assertTrue(containsSingleQuoteShellSpecialCharacters("hello~world"));
        assertTrue(containsSingleQuoteShellSpecialCharacters("hello=world"));
        assertTrue(containsSingleQuoteShellSpecialCharacters("hello%world"));
        assertTrue(containsSingleQuoteShellSpecialCharacters("hello[world"));
        assertFalse(containsSingleQuoteShellSpecialCharacters("hello world"));
        assertFalse(containsSingleQuoteShellSpecialCharacters("hello.world"));
        assertFalse(containsSingleQuoteShellSpecialCharacters("hello,world"));
        assertFalse(containsSingleQuoteShellSpecialCharacters("hello_world"));
        assertFalse(containsSingleQuoteShellSpecialCharacters("hello-world"));
        assertFalse(containsSingleQuoteShellSpecialCharacters("hello]world"));
        assertFalse(containsSingleQuoteShellSpecialCharacters("hello\\world"));
        assertFalse(containsSingleQuoteShellSpecialCharacters("hello/world"));
        assertFalse(containsSingleQuoteShellSpecialCharacters("hello>world"));
        assertFalse(containsSingleQuoteShellSpecialCharacters("hello<world"));
        assertFalse(containsSingleQuoteShellSpecialCharacters("hello(world"));
        assertFalse(containsSingleQuoteShellSpecialCharacters("hello)world"));
        assertFalse(containsSingleQuoteShellSpecialCharacters("hello^world"));
        assertFalse(containsSingleQuoteShellSpecialCharacters("hello$world"));
        assertFalse(containsSingleQuoteShellSpecialCharacters("hello&world"));
        assertFalse(containsSingleQuoteShellSpecialCharacters("hello@world"));
        assertFalse(containsSingleQuoteShellSpecialCharacters("hello`world"));
    }
}
