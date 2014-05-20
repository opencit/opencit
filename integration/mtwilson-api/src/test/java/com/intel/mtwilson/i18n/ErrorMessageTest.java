/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.i18n;

import com.intel.mtwilson.datatypes.ErrorCode;
import com.intel.mtwilson.i18n.*;
import org.slf4j.LoggerFactory;
import org.junit.Test;

/**
 *
 * @author dsmagadx
 */
public class ErrorMessageTest {
    
    private static org.slf4j.Logger log = LoggerFactory.getLogger(ErrorMessageTest.class);
    
    @Test
    public void testErrorMessage(){
        ErrorMessage message = new ErrorMessage(ErrorCode.UNKNOWN_ERROR, "deliberate localizable exception");
        System.out.println(message);
    }
}
