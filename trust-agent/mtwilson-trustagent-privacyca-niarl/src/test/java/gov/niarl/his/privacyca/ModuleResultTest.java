/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package gov.niarl.his.privacyca;

import java.util.StringTokenizer;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class ModuleResultTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ModuleResultTest.class);

    @Test
    public void testGetEndorsementKeyModulusOutput() {
        // normally output for get ek is a long hex string followed by a space " "  
        // if the ek is missing the output may be a single space " "
        // executeVer2Command accepts as a parameter the number of lines of output to return, which is 1 when calling getEndorsementKeyModulus
        int returnCount = 1;
        String line = " ";
        StringTokenizer st = new StringTokenizer(line);
        log.debug("available tokens: {}",  st.countTokens());
        if( st.countTokens() < returnCount ) {
            log.debug("executeVer2Command mode {} with return count {} but only {} tokens are available; expect java.util.NoSuchElementException", 10, returnCount, st.countTokens());
        }
        for (int i = 0; i < returnCount; i++) {
            log.debug("line {} token {}", i, st.nextToken());
        }
        
    }
}
