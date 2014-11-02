/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.module;

/**
 *
 * @author jbuhacoff
 */
public class NoticeHolder {
    private Object wrappedObject;
    private ComponentHolder component;

    public NoticeHolder(Object wrappedObject, ComponentHolder component) {
        this.wrappedObject = wrappedObject;
        this.component = component;
    }

    public ComponentHolder getComponent() {
        return component;
    }

    public Object getWrappedObject() {
        return wrappedObject;
    }
    
    
}
