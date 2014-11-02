/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.util.jdbc.retry;

/**
 *
 * @author jbuhacoff
 */
public class RetryUtil {
    public static boolean isCommunicationFailure(String message) {
        if (message == null) {
            return false;
        }
        // mysql: com.mysql.jdbc.exceptions.jdbc4.CommunicationsException: Communications link failure \n The last packet successfully received from the server was 6,745,809 milliseconds ago.  The last packet sent successfully to the server was 0 milliseconds ago.
        if (message.contains("Communications link failure")) {
            return true;
        } 
        return false;
    }

    public static boolean isCommunicationFailure(Throwable e) {
        Throwable cause = e;
        while (cause != null) {
            if (isCommunicationFailure(cause.getMessage())) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
    
}
