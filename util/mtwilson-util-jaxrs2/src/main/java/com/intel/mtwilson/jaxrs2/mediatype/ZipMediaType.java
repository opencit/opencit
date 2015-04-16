/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jaxrs2.mediatype;

/**
 *
 * @author jbuhacoff
 */
public class ZipMediaType {
    // compression formats
    public static final String APPLICATION_GZIP = "application/gzip"; // replaces application/x-gzip, application/x-gzip-compressed, etc. http://www.ietf.org/rfc/rfc6713.txt
    public static final String APPLICATION_ZIP = "application/zip";
    public static final String APPLICATION_X_BZIP2 = "application/x-bzip2";
    public static final String APPLICATION_ZLIB = "application/zlib"; // replaces application/x-zlib, application/x-zlib-compressed, etc. http://www.ietf.org/rfc/rfc6713.txt
    public static final String APPLICATION_X_TAR = "application/x-tar";
    public static final String ARCHIVE_TAR_GZ = "archive/tar+gzip";
}
