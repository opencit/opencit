/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package test.jaxrs;

import javax.ws.rs.core.Response;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class HttpErrorFamilyTest {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HttpErrorFamilyTest.class);

    /**
     * Output:
     * <pre>
     * INFORMATIONAL
     * SUCCESSFUL
     * REDIRECTION
     * CLIENT_ERROR
     * SERVER_ERROR
     * OTHER
     * </pre>
     */
    @Test
    public void testPrintErrorFamilyNames() {
        for (Response.Status.Family c : Response.Status.Family.values()) {
            System.out.println(c);
        }
    }
    
    @Test
    public void testPrintErrorCodes() {
        assertTrue(Response.Status.Family.familyOf(400) == Response.Status.Family.CLIENT_ERROR);
        assertTrue(Response.Status.Family.familyOf(401) == Response.Status.Family.CLIENT_ERROR);
        assertTrue(Response.Status.Family.familyOf(402) == Response.Status.Family.CLIENT_ERROR);
        assertTrue(Response.Status.Family.familyOf(499) == Response.Status.Family.CLIENT_ERROR);
        assertTrue(Response.Status.Family.familyOf(500) == Response.Status.Family.SERVER_ERROR);
        assertTrue(Response.Status.Family.familyOf(501) == Response.Status.Family.SERVER_ERROR);
        assertTrue(Response.Status.Family.familyOf(502) == Response.Status.Family.SERVER_ERROR);
        assertTrue(Response.Status.Family.familyOf(599) == Response.Status.Family.SERVER_ERROR);
    }
    
}
