/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.ms.common;

import com.intel.mtwilson.datatypes.ErrorCode;
import com.intel.mtwilson.util.MWException;

/**
 *
 * @author dsmagadx
 */
public class MSException extends MWException {

    public MSException(ErrorCode errorCode, Object... params) {
    	super(errorCode,params);
    }
    public MSException(Throwable e,ErrorCode errorCode,Object... params  ){
        super(e,errorCode,params);
    }
    
    public MSException(Throwable e){
        super(e);
    }
    
    public MSException(ErrorCode errorCode){
        super(errorCode);
    }

}
