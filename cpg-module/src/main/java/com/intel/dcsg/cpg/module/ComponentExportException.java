/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.module;

/**
 * TODO :  use cpg-i18n message instead of string.
 * @author jbuhacoff
 */
public class ComponentExportException extends Exception {
    public ComponentExportException() {
        super();
    }
    public ComponentExportException(Throwable cause) {
        super(cause);
    }
    public ComponentExportException(String message) {
        super(message);
    }
    public ComponentExportException(String message, Throwable cause) {
        super(message, cause);
    }
}
