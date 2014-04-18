/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package test.validation;

import com.intel.dcsg.cpg.console.Command;
import com.intel.mtwilson.setup.cmd.TestDataValidation;
import org.junit.Test;

/**
 * The code that needs to be tested MUST be in the "main" classes directory.
 * This is because aspectj weaves main aspects into main code, and test aspects
 * into test code. So if we were to instantiate a Pcr here and pass it to a 
 * method, the aspects would not be woven into it. 
 * Therefore, the test is implemented as a test command in the main source
 * and only invoked from here.
 * @author jbuhacoff
 */
public class ValidationTest {
 
    @Test
    public void testDataValidation() throws Exception {
        Command cmd = new TestDataValidation();
        cmd.execute(null);
    }
}
