/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.rpc.v2.resource;

import com.intel.dcsg.cpg.validation.Fault;
import java.util.List;

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
    List<Fault> getFaults();
}
