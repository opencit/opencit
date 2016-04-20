/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package test.regex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class RegexTest {
    @Test
    public void testSqlFileTimestamp() {
        Pattern p = Pattern.compile("^([0-9]+).*");
        Matcher m1 = p.matcher("20120101000000_bootstrap.sql");
        assertEquals(true,m1.matches());
        assertEquals("20120101000000", m1.group(1));
        Matcher m2 = p.matcher("20121226000000_remove_created_by_patch_rc3.sql");
        assertEquals(true,m2.matches());
        assertEquals("20121226000000", m2.group(1));
    }
}
