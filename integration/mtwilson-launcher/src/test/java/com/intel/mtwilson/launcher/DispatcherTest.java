/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.launcher;

import com.intel.mtwilson.pipe.TransformerPipe;
import com.intel.mtwilson.text.transform.CamelCaseToHyphenated;
import com.intel.mtwilson.text.transform.RegexTransformer;
import java.util.HashMap;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class DispatcherTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DispatcherTest.class);

    @Test
    public void testNameConversion() {
        HashMap<String,String> replacements = new HashMap<>();
        replacements.put("mt-wilson", "mtwilson");
        RegexTransformer replacing = new RegexTransformer(replacements);
        CamelCaseToHyphenated hyphenating = new CamelCaseToHyphenated();
//        PascalCaseToHyphenated hyphenating = new PascalCaseToHyphenated();
        TransformerPipe<String> pipe = new TransformerPipe<>(hyphenating, replacing);

        assertEquals("mtwilson-test", pipe.transform("MtWilsonTest"));
        assertEquals("test-mtwilson", pipe.transform("TestMtWilson"));
        assertEquals("test-ca", pipe.transform("TestCA"));
        assertEquals("foo-ca-test", pipe.transform("FooCATest")); // fails because it becomes foo-catest  ... need an abbreviation handler in the transformer so FooCATest will be treated like foo-ca-test (?:[A-Z]+?)[A-Z][a-z]*
        assertEquals("ca-test", pipe.transform("CATest"));
    }
}
