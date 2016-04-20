/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.agent.vmware;

/**
 *
 * @author jbuhacoff
 */
public class VMwareConnectionException extends Exception {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public VMwareConnectionException() {
        super();
    }
    public VMwareConnectionException(Throwable cause) {
        super(cause);
    }
    public VMwareConnectionException(String message) {
        super(message);
    }
    public VMwareConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
