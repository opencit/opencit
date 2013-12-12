/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.module;

/**
 * TODO :  use cpg-i18n message instead of string.
 * @author jbuhacoff
 */
public class ModuleActivationException extends Exception {
    public ModuleActivationException() {
        super();
    }
    public ModuleActivationException(Throwable cause) {
        super(cause);
    }
    public ModuleActivationException(String message) {
        super(message);
    }
    public ModuleActivationException(String message, Throwable cause) {
        super(message, cause);
    }
}
