package com.intel.mtwilson.datatypes;


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
    
    public HostResponse(Throwable e) {
        super(ErrorCode.SYSTEM_ERROR, e.getMessage());
    }
}
