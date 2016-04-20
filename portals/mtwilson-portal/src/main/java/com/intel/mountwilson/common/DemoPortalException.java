/**
 * This class used for common Exception Handling. 
 */
package com.intel.mountwilson.common;

/**
 * @author yuvrajsx
 *
 */
public class DemoPortalException extends Exception {

	private static final long serialVersionUID = 993696L;
	
	public DemoPortalException(String message)
    {
        super(message);
    }

    public DemoPortalException(String message, Exception e)
    {
        super(message, e);
    }

}
