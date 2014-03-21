/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mountwilson.common;


/**
 *
 * @author dsmagadX
 */
public class TAException extends Exception {
    

	ErrorCode errorCode = null;

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }
    
    private TAException(){
        
    }
    
    public TAException(ErrorCode errorCode, String message){
        super(message);
        this.errorCode = errorCode;
    }

	public TAException(ErrorCode errorCode, String message, Exception e) {
        super(message,e);
        this.errorCode = errorCode;
		
	}
    
}
