/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.authz.token;

/**
 * A helpful indicator that the token is legitimate but has expired. 
 * The client application should obtain a new token to continue.
 * Specific expiration time or other information can be found in 
 * the server log and should not be provided to clients - for them
 * it doesn't matter anyway, they just need to get a new token.
 * 
 * @since 0.1
 * @author jbuhacoff
 */
public class ExpiredTokenException extends Exception {
    public ExpiredTokenException() {
        super();
    }
    public ExpiredTokenException(Throwable e) {
        super(e);
    }
}
