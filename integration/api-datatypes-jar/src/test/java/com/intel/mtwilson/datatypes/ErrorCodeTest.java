/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.datatypes;

import org.junit.Test;

/**
 *
 * @author dsmagadx
 */
public class ErrorCodeTest {
    
    @Test
    public void testErrorCodeWithFormataAndMultipleConcat(){
        AuthResponse authResponse = new AuthResponse(ErrorCode.WS_MLE_ASSOCIATION_EXISTS,"hello","how",1111);
        if(authResponse.getErrorCodeEnum() == ErrorCode.WS_MLE_ASSOCIATION_EXISTS)
            System.out.println(authResponse.getErrorMessage());
    }

    @Test
    public void testErrorCodeWithFormataAndSingleConcat(){
        AuthResponse authResponse = new AuthResponse(ErrorCode.WS_MLE_DATA_MISSING,"MLNAME");
        if(authResponse.getErrorCodeEnum() == ErrorCode.WS_MLE_DATA_MISSING)
            System.out.println(authResponse.getErrorMessage());
    }


        @Test
    public void testErrorCodeWithNoFormat(){
        AuthResponse authResponse = new AuthResponse(ErrorCode.WS_MLE_DATA_MISSING);
         if(authResponse.getErrorCodeEnum() == ErrorCode.WS_MLE_DATA_MISSING)
            System.out.println(authResponse.getErrorMessage());
    }

    
}
