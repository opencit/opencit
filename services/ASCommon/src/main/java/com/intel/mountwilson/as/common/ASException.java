package com.intel.mountwilson.as.common;

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
    public ASException(Throwable e,ErrorCode errorCode,Object... params  ){
        super(e,errorCode,params);
    }
    
    public ASException(Throwable e){
        super(e);
    }
    
    public ASException(ErrorCode errorCode){
        super(errorCode);
    }
}
