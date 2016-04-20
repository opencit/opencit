/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package test.console;

import com.intel.dcsg.cpg.console.ExtendedOptions;
import java.util.HashMap;
import java.util.Set;
import org.apache.commons.configuration.Configuration;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class TestParseOptions {
    @Test
    public void testParseExtendedOptions() {
        String[] args = new String[] { "arg1", "arg2", "--option1=value1", "arg3", "--option2=value2", "arg4", "--option3", "--no-option4", "arg5", "--", "verbatim-arg6", "--verbatim-arg7", "--verbatim=arg8" };
        ExtendedOptions getopt = new ExtendedOptions(args);
        String[] expectedArgs = new String[] { "arg1", "arg2", "arg3", "arg4", "arg5", "verbatim-arg6", "--verbatim-arg7", "--verbatim=arg8" };
        HashMap<String,String> expectedOpts = new HashMap<String,String>();
        expectedOpts.put("option1", "value1");
        expectedOpts.put("option2", "value2");
        expectedOpts.put("option3", "true");
        expectedOpts.put("option4", "false");
        String[] actualArgs = getopt.getArguments();
        assertEquals(expectedArgs.length, actualArgs.length);
        for(int i=0; i<expectedArgs.length; i++) {
            assertEquals(expectedArgs[i], actualArgs[i]);
        }
        Set<String> optNames = expectedOpts.keySet(); 
        Configuration actualOpts = getopt.getOptions();
        for(String optName : optNames) {
            assertEquals(expectedOpts.get(optName), actualOpts.getString(optName));
        }
    }
    
    @Test
    public void testParseExtendedOptionsErrors() {
        String[] args = new String[] { "arg1", "--option1=", "--=value1", "--=", "arg2" };
        ExtendedOptions getopt = new ExtendedOptions(args);
        String[] expectedArgs = new String[] { "arg1", "arg2" };
        HashMap<String,String> expectedOpts = new HashMap<String,String>();
        expectedOpts.put("option1", "");
        String[] actualArgs = getopt.getArguments();
        assertEquals(expectedArgs.length, actualArgs.length);
        for(int i=0; i<expectedArgs.length; i++) {
            assertEquals(expectedArgs[i], actualArgs[i]);
        }
        Set<String> optNames = expectedOpts.keySet(); 
        Configuration actualOpts = getopt.getOptions();
        for(String optName : optNames) {
            assertEquals(expectedOpts.get(optName), actualOpts.getString(optName));
        }
    }
}
