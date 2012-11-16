/**
 * 
 */
package com.intel.mountwilson.common;

/**
 * @author yuvrajsx
 *
 */
public class WLMPortalException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 993696L;
	
	public WLMPortalException(String message)
    {
        super(message);
    }

    public WLMPortalException(String message, Exception e)
    {
        super(message, e);
    }

}
