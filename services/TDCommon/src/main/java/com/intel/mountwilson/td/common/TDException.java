/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mountwilson.td.common;

import com.intel.mtwilson.datatypes.ErrorCode;
import com.intel.mtwilson.util.MWException;

/**
 *
 * @author ssbangal
 */
public class TDException extends MWException {
    
    	public TDException(ErrorCode errorCode, String msg) {
		super(errorCode, msg);
	}
    

	public TDException(ErrorCode errorCode, String msg, Throwable e) {
		super(errorCode, msg,e);
	}
}
