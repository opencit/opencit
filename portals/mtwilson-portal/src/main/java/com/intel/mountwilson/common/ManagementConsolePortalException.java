/**
 * 
 */
package com.intel.mountwilson.common;

/**
 * @author yuvrajsx
 *
 */
public class ManagementConsolePortalException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 993696L;
	
	public ManagementConsolePortalException(String message)
    {
        super(message);
    }

    public ManagementConsolePortalException(String message, Exception e)
    {
        super(message, e);
    }

}
