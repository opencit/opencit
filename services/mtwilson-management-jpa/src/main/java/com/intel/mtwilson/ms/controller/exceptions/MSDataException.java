/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.ms.controller.exceptions;

/**
 *
 * @author dsmagadx
 */
public class MSDataException extends Exception {

    public MSDataException(String errorMessage, Exception e) {
        super(errorMessage,e);
    }
    public MSDataException(Exception e) {
        super(e);
    }
}
