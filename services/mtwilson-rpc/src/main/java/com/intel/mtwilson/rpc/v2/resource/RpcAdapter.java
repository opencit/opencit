/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.rpc.v2.resource;

/**
 *
 * @author jbuhacoff
 */
public interface RpcAdapter<T,U> {
    Class<? extends T> getInputClass();
    Class<? extends U> getOutputClass();
    void setInput(T input);
    void invoke();
    U getOutput();
}
