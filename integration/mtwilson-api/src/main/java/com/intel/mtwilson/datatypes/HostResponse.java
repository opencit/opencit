package com.intel.mtwilson.datatypes;

import com.intel.mtwilson.i18n.ErrorCode;


/**
 *
 * @author dsmagadx
 */
public class HostResponse extends AuthResponse {
    
    public HostResponse() {
        super();
    }
    
    public HostResponse(ErrorCode errorCode){
        super(errorCode);
    }
    
    public HostResponse(ErrorCode errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }
}
