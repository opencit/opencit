package com.intel.mountwilson.as.common;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import com.intel.mtwilson.datatypes.ErrorCode;
import com.intel.mtwilson.util.MWException;
/**
 *
 * @author dsmagadx
 */
public class ASException extends MWException {
    
    public ASException(ErrorCode errorCode, Object... params) {
    	super(errorCode,params);
    }
    public ASException(Throwable e,ErrorCode code,Object... msg  ){
        super(e,code,msg);
    }
    
    public ASException(Throwable e){
        super(e);
    }
    
    public ASException(ErrorCode errorCode){
        super(errorCode);
    }
}
