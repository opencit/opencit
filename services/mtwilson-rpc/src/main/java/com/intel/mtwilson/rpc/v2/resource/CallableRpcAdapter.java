/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.rpc.v2.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.intel.dcsg.cpg.validation.Fault;
import com.intel.mtwilson.patch.PatchException;
import com.intel.mtwilson.patch.PatchUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Given an @RPC instance that implements Callable, this class organizes
 * how that instance will be handled for input and output, 
 * how the intermediate output will
 * be stored, etc. 
 * 
 * A callable instance itself is the input type - its setters and
 * getters are used as the available attributes. The type of the return value
 * from the call() method is the output type. This means a Callable has a
 * precise specification of what are inputs and what is the output compared
 * with the Runnable RPC interface. 
 * 
 * For example:
 * 
 * Input:   <add_integers><x>1</x><y>1</y></add_integers>
 * 
 * Output: <integer>2</integer>
 * 
 * @author jbuhacoff
 */
public class CallableRpcAdapter implements RpcAdapter<Object,Object> {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CallableRpcAdapter.class);
    private ObjectMapper mapper;
    private Callable rpcInstance;
    private Object result;
    private ArrayList<Fault> faults = new ArrayList<Fault>();
    public CallableRpcAdapter(Callable rpcInstance) {
        this.rpcInstance = rpcInstance;
        mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
    }
    // these declare how we want to interpret the client's request body
    // and how we want to store the intermediate output
    @Override
    public Class getInputClass() {
        return rpcInstance.getClass();
    }
    @Override
    public Class/*<T>*/ getOutputClass() {
        try {
            return rpcInstance.getClass().getMethod("call").getReturnType();
        }
        catch(NoSuchMethodException e) {
            log.error("Cannot determine return type of Callable RPC", e); // should never happen since the rpcInstance must implement Callable
            throw new RuntimeException(e);
        }
    }
    // this method will receive the input object which was deserialized from
    // the client's request body - and is the same type we returned in 
    // getInputClass
    @Override
    public void setInput(Object/*T*/ input) {
        // use beanutils to copy the properties we need
        try {
//            Map<String,Object> diff = PatchUtil.diff(input, rpcInstance);
//            log.debug("Going to copy input to rpc instance: {}", mapper.writeValueAsString(input)); // throws 
//            PatchUtil.apply(diff, rpcInstance); // throws JsonProcessingException
            PatchUtil.copy(input, rpcInstance);
//            log.debug("RPC instance is now: {}", mapper.writeValueAsString(rpcInstance));
        }
        catch(Exception e) {
            log.error("Error while setting task input: {}", e.getMessage());
            faults.add(new Fault(e, "Error while preparing task")); 
        }
    }
    // this method is called to invoke the rpc
    @Override
    public void invoke() {
        try {
            result = rpcInstance.call();
        }
        catch(Exception e) {
            log.error("Error while executing task: {}", e.getMessage());
            faults.add(new Fault(e, "Error while executing task")); 
        }
    }
    // this method is called to get the output from the rpc,
    // which is also called the intermediate output because
    // it will later be serialized according to the client's
    // preference at delivery time
    @Override
    public Object getOutput() {
        return result;
    }
    
    public List<Fault> getFaults() { return faults; }
}
