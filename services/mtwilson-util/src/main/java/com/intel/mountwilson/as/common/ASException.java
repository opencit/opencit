package com.intel.mountwilson.as.common;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import com.intel.mtwilson.i18n.ErrorCode;
import com.intel.mtwilson.util.MWException;
/**
 *
 * @author dsmagadx
 */
public class ASException extends MWException {
    private String message;
    public ASException(ErrorCode errorCode, Object... params) {
    	super(errorCode,params);
        message = String.format(errorCode.getMessage(), params);
    }
    public ASException(Throwable e,ErrorCode errorCode,Object... params  ){
        super(e,errorCode,params);
        message = String.format("%s: "+errorCode.getMessage(),e.toString(), params);
    }
    
    public ASException(Throwable e){
        super(e);
        message = e.toString();
    }
    
    public ASException(ErrorCode errorCode){
        super(errorCode);
        message = errorCode.getMessage();
    }
    
    @Override
    public String toString() {
        return message;
    }
}
