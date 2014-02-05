/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.security.core;

import java.util.Date;
import java.util.List;

/**
 * @since 1.2
 * @author jbuhacoff
 */
public interface RequestLog {
//    RequestInfo findRequestWithMd5Hash(byte[] md5_hash);
//    RequestInfo findRequestWithMd5HashAfter(byte[] md5_hash, Date after); // finds requests received AFTER this date (not inclusive)
    List<RequestInfo> findRequestFromSourceWithMd5HashAfter(String source, byte[] md5_hash, Date after);
    void logRequestInfo(RequestInfo request);
}
