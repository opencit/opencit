/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package backport.test;

//import com.intel.backport.java.lang.String;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class BackportTest {
    private static Logger log = LoggerFactory.getLogger(BackportTest.class);
    
    @Test
    public void testIsEmpty() {
        assert(com.intel.backport.java.lang.String.isEmpty("")==true); 
    }

}
