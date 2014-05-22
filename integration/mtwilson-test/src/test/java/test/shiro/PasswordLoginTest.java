/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package test.shiro;

import com.intel.mtwilson.crypto.password.PasswordUtil;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginPassword;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import org.apache.commons.codec.binary.Hex;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class PasswordLoginTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PasswordLoginTest.class);
    
    @Test
    public void createAuthorizationHeader() {
        
    }
    
    /**
     * text.getBytes() and charset.forname(...).encode(charbuffer.wrap(text)).array() are not the same
     * because the byte buffer method adds a null terminator:
     * 
2014-03-23 17:23:21,576 DEBUG [main] c.i.m.s.PasswordLoginTest [PasswordLoginTest.java:31] getBytes: [102, 112, 65, 110, 85, 65, 118, 69, 54, 113, 77, 95]
2014-03-23 17:23:21,581 DEBUG [main] c.i.m.s.PasswordLoginTest [PasswordLoginTest.java:32] ByteBuffer: [102, 112, 65, 110, 85, 65, 118, 69, 54, 113, 77, 95, 0]
     * 
     * 
2014-03-23 17:29:04,605 DEBUG [main] c.i.m.s.PasswordLoginTest [PasswordLoginTest.java:43] toCharArray length: 12
2014-03-23 17:29:04,606 DEBUG [main] c.i.m.s.PasswordLoginTest [PasswordLoginTest.java:45] CharBuffer length: 12     * 
* 
*   It's the Charset.forName(...).encode(text)   that appends the null terminator:
2014-03-23 17:30:25,815 DEBUG [main] c.i.m.s.PasswordLoginTest [PasswordLoginTest.java:51] charset [102, 112, 65, 110, 85, 65, 118, 69, 54, 113, 77, 95, 0]
* 
* 
     */
    @Test
    public void testGetBytesFromString() {
        String text = "fpAnUAvE6qM_";
        log.debug("getBytes: {}", text.getBytes(Charset.forName("UTF-8")));
        log.debug("ByteBuffer: {}", Charset.forName("UTF-8").encode(CharBuffer.wrap(text)).array());
        log.debug("valueof: {}", String.valueOf(text.toCharArray()).getBytes(Charset.forName("UTF-8")));
        
        // chars
        log.debug("toCharArray length: {}", text.toCharArray().length);
//        log.debug("CharBuffer.wrap: {}", CharBuffer.wrap(text).array()); // throws UnsupportedOperationException at array()
        log.debug("CharBuffer length: {}", CharBuffer.wrap(text).length());
        
        // encode
        log.debug("charset {}", Charset.forName("UTF-8").encode(text).array());
    }

    @Test
    public void verifyAuthorizationHeader()throws Exception {
    }
    
    @Test
    public void testHashFunction()throws Exception {
//        char[] password = "fpAnUAvE6qM_".toCharArray();
        // to bytes:
//        ByteBuffer bytebuffer = Charset.forName("UTF-8").encode(CharBuffer.wrap(password));
//        byte[] bytes = bytebuffer.array();
        byte[] bytes = "fpAnUAvE6qM_".getBytes(Charset.forName("UTF-8"));
        UserLoginPassword loginPasswordInfo = new UserLoginPassword();
        loginPasswordInfo.setAlgorithm("SHA256");
        loginPasswordInfo.setIterations(1);
        // "01350d1c-b7e2-464a-8999-6b816a70904c";"7034e288f6b7066343def75417878c2d18c1abecf46c2c9bd21ca3335862ddaf";"a5abcfdea3ca6860"
//        loginPasswordInfo.setPasswordHash(Hex.decodeHex("7034e288f6b7066343def75417878c2d18c1abecf46c2c9bd21ca3335862ddaf".toCharArray()));
        loginPasswordInfo.setSalt(Hex.decodeHex("cb1be6f69c713d9f".toCharArray()));
        byte[] hashed = PasswordUtil.hash(bytes, loginPasswordInfo);
        log.debug("hashed: {}", Hex.encodeHexString(hashed));
        
        assertEquals("82dd31c5e03f0a94c84dd478f6a5264d0eff9af46af30d1b49ad02e6a17caebc", Hex.encodeHexString(hashed));
    }
}
